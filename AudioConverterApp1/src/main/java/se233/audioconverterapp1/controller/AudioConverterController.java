package se233.audioconverterapp1.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;
import se233.audioconverterapp1.util.FFmpegManager;

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
    @FXML private TableColumn<FileInfo, Void> actionColumn;

    @FXML private ChoiceBox<String> formatChoiceBox;
    @FXML private ChoiceBox<String> bitrateChoiceBox;
    @FXML private ChoiceBox<String> sampleRateChoiceBox;
    @FXML private ChoiceBox<String> channelChoiceBox;
    @FXML private Button convertButton;
    @FXML private Button clearButton;
    @FXML private Button cancelButton;
    @FXML private Button applyFormatButton;
    @FXML private Label dropZone;
    @FXML private ProgressBar overallProgress;
    @FXML private Label overallProgressText;
    @FXML private Label ffmpegWarningLabel;
    @FXML private MenuItem setFFmpegPathMenu;

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
        setupActionColumn();
        setupAudioSettings();

        // FFmpeg setup
        setFFmpegPathMenu.setOnAction(_ -> chooseFFmpegPath());
        updateFFmpegWarning();

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

        targetFormatColumn.setCellValueFactory(cell -> cell.getValue().targetFormatProperty());
        targetFormatColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn("mp3", "wav", "m4a", "flac"));
        targetFormatColumn.setEditable(true);

        fileTable.setItems(fileData);
        fileTable.setEditable(true);
    }

    // ---- Format choice box ----
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

    // ---- File import ----
    private void setupFileImport() {
        // Drag & drop
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

        // Double click → FileChooser
        dropZone.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Audio Files");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.flac")
                );
                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dropZone.getScene().getWindow());
                if (selectedFiles != null) {
                    selectedFiles.stream().filter(this::isAudioFile).forEach(this::addFileToTable);
                }
            }
        });
    }

    // ---- Action column (per-file Cancel/Clear) ----
    private void setupActionColumn() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            private final Button clearBtn = new Button("Clear");

            {
                cancelBtn.setOnAction(e -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    conversionManager.cancelConversion(file);
                    file.setStatus("Cancelled");
                });

                clearBtn.setOnAction(e -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(file);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, cancelBtn, clearBtn));
            }
        });
    }

    private void setupAudioSettings() {
        bitrateChoiceBox.setItems(FXCollections.observableArrayList("128k", "192k", "256k", "320k"));
        bitrateChoiceBox.setValue("192k");

        sampleRateChoiceBox.setItems(FXCollections.observableArrayList("44100", "48000", "96000"));
        sampleRateChoiceBox.setValue("44100");

        channelChoiceBox.setItems(FXCollections.observableArrayList("1", "2"));
        channelChoiceBox.setValue("2");
    }

    // ---- Conversion handling ----
    private void handleConvert() {
        if (!FFmpegManager.isFFmpegAvailable()) {
            showAlert("FFmpeg is not set. Please go to Settings → Set FFmpeg Path.");
            return;
        }
        if (fileData.isEmpty()) {
            showAlert("No files to convert!");
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Output Folder");
        File outputDir = chooser.showDialog(fileTable.getScene().getWindow());
        if (outputDir == null) {
            showAlert("Conversion cancelled (no output folder selected).");
            return;
        }

        String outputFormat = formatChoiceBox.getValue();
        String bitrate = bitrateChoiceBox.getValue();
        String sampleRate = sampleRateChoiceBox.getValue();
        String channel = channelChoiceBox.getValue();
        conversionManager.startConversions(fileData, outputFormat, this::updateGlobalProgress, bitrate, sampleRate, channel, outputDir);

        showAlert("Started conversion of " + fileData.size() + " file(s) to" + outputFormat + " in " + outputDir.getAbsolutePath());
    }

    private void handleClear() {
        fileData.clear();
        overallProgress.setProgress(0);
    }

    private void handleCancel() {
        conversionManager.cancelConversions();
        updateGlobalProgress();
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

    // ---- File helpers ----
    private void addFileToTable(File file) {
        fileData.add(new FileInfo(
                file.getAbsolutePath(),
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
        int dot = file.getName().lastIndexOf('.');
        return (dot == -1) ? "" : file.getName().substring(dot + 1);
    }

    private String formatSize(long sizeKB) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        return nf.format(sizeKB) + " KB";
    }

    // ---- FFmpeg setup ----
    private void updateFFmpegWarning() {
        if (FFmpegManager.isFFmpegAvailable()) {
            ffmpegWarningLabel.setText("");
        } else {
            ffmpegWarningLabel.setText("FFmpeg not set! Go to Settings → Set Path");
        }
    }

    private void chooseFFmpegPath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select FFmpeg Executable");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("FFmpeg Executable", "ffmpeg.exe", "ffmpeg")
        );
        File file = chooser.showOpenDialog(fileTable.getScene().getWindow());
        if (file != null) {
            FFmpegManager.setFFmpegPath(file.getAbsolutePath());
            updateFFmpegWarning();
        }
    }

    // ---- Utility ----
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
