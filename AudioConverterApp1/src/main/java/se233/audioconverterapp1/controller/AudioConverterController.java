package se233.audioconverterapp1.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AudioConverterController {

    // ==== UI Elements ====
    @FXML private TableView<FileInfo> fileTable;
    @FXML private TableColumn<FileInfo, String> fileNameColumn;
    @FXML private TableColumn<FileInfo, String> formatColumn;
    @FXML private TableColumn<FileInfo, String> sizeColumn;
    @FXML private TableColumn<FileInfo, Double> progressColumn;
    @FXML private TableColumn<FileInfo, String> statusColumn;
    @FXML private TableColumn<FileInfo, String> targetFormatColumn;

    @FXML private ChoiceBox<String> formatChoiceBox;
    @FXML private Button convertButton;
    @FXML private Button clearButton;
    @FXML private Button cancelButton;
    @FXML private Button applyFormatButton;
    @FXML private Label dropZone;
    @FXML private ProgressBar overallProgress;

    // ==== Data + Manager ====
    private final ObservableList<FileInfo> fileData = FXCollections.observableArrayList();
    private final ConversionManager conversionManager = new ConversionManager();

    // ==== Initialization ====
    @FXML
    public void initialize() {
        setupTable();
        setupFormatChoiceBox();
        setupButtons();
        setupFileImport();
        targetFormatColumn.setCellValueFactory(cell -> cell.getValue().targetFormatProperty());
        targetFormatColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn("mp3", "wav", "m4a", "flac"));
        targetFormatColumn.setEditable(true);
        fileTable.setEditable(true);
        overallProgress.setProgress(0);
    }

    // ---- Table setup ----
    private void setupTable() {
        fileNameColumn.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        formatColumn.setCellValueFactory(cell -> cell.getValue().formatProperty());
        sizeColumn.setCellValueFactory(cell -> cell.getValue().sizeProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());

        progressColumn.setCellValueFactory(cell -> cell.getValue().progressProperty().asObject());
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

        fileTable.setItems(fileData);
    }

    // ---- Format choices ----
    private void setupFormatChoiceBox() {
        formatChoiceBox.setItems(FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");
    }

    // ---- Buttons ----
    private void setupButtons() {
        convertButton.setOnAction(e -> handleConvert());
        clearButton.setOnAction(e -> handleClear());
        cancelButton.setOnAction(e -> handleCancel());
        applyFormatButton.setOnAction(_ -> applyGlobalFormat());
    }

    // ---- File import (drag & drop + double click) ----
    private void setupFileImport() {
        // Drag-and-drop
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                db.getFiles().stream().filter(this::isAudioFile).forEach(this::addFileToTable);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        // Double click = open file chooser
        dropZone.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Audio Files");
                fileChooser.getExtensionFilters().add(
                        new ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.flac")
                );
                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dropZone.getScene().getWindow());
                if (selectedFiles != null) {
                    selectedFiles.stream().filter(this::isAudioFile).forEach(this::addFileToTable);
                }
            }
        });
    }

    // ---- File handling helpers ----
    private void addFileToTable(File file) {
        fileData.add(new FileInfo(
                file.getAbsolutePath(),   // full path (FFmpeg needs this)
                getExtension(file),
                formatSize(file.length() / 1024)
        ));
    }

    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav")
                || name.endsWith(".m4a") || name.endsWith(".flac");
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? "" : name.substring(dot + 1);
    }

    private String formatSize(long sizeKB) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        return nf.format(sizeKB) + " KB";
    }

    // ---- Conversion handling ----
    private void handleConvert() {
        if (fileData.isEmpty()) {
            showAlert("No files to convert!");
            return;
        }

        String outputFormat = formatChoiceBox.getValue();
        conversionManager.startConversions(fileData, outputFormat, this::updateGlobalProgress);

        showAlert("Started conversion of " + fileData.size() + " file(s) to " + outputFormat);
    }

    private void handleClear() {
        fileData.clear();
        overallProgress.setProgress(0);
        showAlert("File list cleared.");
    }

    private void handleCancel() {
        conversionManager.cancelConversions();
        updateGlobalProgress();
        showAlert("Conversions cancelled.");
    }

    private void applyGlobalFormat() {
        String globalFormat = formatChoiceBox.getValue();
        for (FileInfo file : fileData) {
            file.setTargetFormat(globalFormat);
        }
        showAlert("Applied global format (" + globalFormat + ") to all files.");
    }

    private void updateGlobalProgress() {
        if (fileData.isEmpty()) {
            overallProgress.setProgress(0);
            return;
        }
        double sum = fileData.stream().mapToDouble(f -> f.progressProperty().get()).sum();
        overallProgress.setProgress(sum / fileData.size());
    }

    // ---- Utility ----
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
