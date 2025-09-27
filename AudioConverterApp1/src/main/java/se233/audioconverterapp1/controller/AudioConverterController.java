package se233.audioconverterapp1.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;

public class AudioConverterController {

    // Table
    @FXML private TableView<FileInfo> fileTable;
    @FXML private TableColumn<FileInfo, String> fileNameColumn;
    @FXML private TableColumn<FileInfo, String> formatColumn;
    @FXML private TableColumn<FileInfo, String> sizeColumn;
    @FXML private TableColumn<FileInfo, Double> progressColumn;

    // Controls
    @FXML private ChoiceBox<String> formatChoiceBox;
    @FXML private Button convertButton;
    @FXML private Button clearButton;
    @FXML private Label dropZone;
    @FXML private ProgressBar globalProgressBar;

    // Data + Manager
    private final ObservableList<FileInfo> fileData = FXCollections.observableArrayList();
    private final ConversionManager conversionManager = new ConversionManager();

    @FXML
    public void initialize() {
        // Table bindings
        fileNameColumn.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        formatColumn.setCellValueFactory(cell -> cell.getValue().formatProperty());
        sizeColumn.setCellValueFactory(cell -> cell.getValue().sizeProperty());

        progressColumn.setCellValueFactory(cell -> cell.getValue().progressProperty().asObject());
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

        fileTable.setItems(fileData);

        // Format choices
        formatChoiceBox.setItems(FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");

        // Buttons
        convertButton.setOnAction(e -> handleConvert());
        clearButton.setOnAction(e -> handleClear());

        // Drag & Drop
        setupDragAndDrop();

        globalProgressBar.setProgress(0);
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != dropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                db.getFiles().stream()
                        .filter(this::isAudioFile)
                        .forEach(file -> fileData.add(new FileInfo(
                                file.getName(),
                                getExtension(file),
                                formatSize(file.length() / 1024)
                        )));
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
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
        globalProgressBar.setProgress(0);
        showAlert("File list cleared.");
    }

    private void updateGlobalProgress() {
        if (fileData.isEmpty()) {
            globalProgressBar.setProgress(0);
            return;
        }
        double sum = fileData.stream().mapToDouble(f -> f.progressProperty().get()).sum();
        globalProgressBar.setProgress(sum / fileData.size());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
