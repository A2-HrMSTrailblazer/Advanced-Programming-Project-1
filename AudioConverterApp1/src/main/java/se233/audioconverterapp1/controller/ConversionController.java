package se233.audioconverterapp1.controller;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;
import se233.audioconverterapp1.util.FFmpegManager;

import java.io.File;

public class ConversionController {
    private final TableView<FileInfo> fileTable;
    private final ChoiceBox<String> formatChoiceBox;
    private final ChoiceBox<String> bitrateChoiceBox;
    private final ChoiceBox<String> sampleRateChoiceBox;
    private final ChoiceBox<String> channelChoiceBox;
    private final Button convertButton;
    private final Button clearButton;
    private final Button cancelButton;
    private final Button applyFormatButton;
    private final ProgressBar overallProgress;
    private final Label overallProgressText;
    private final VBox configPanel;
    private final TableController tableController;

    private final ConversionManager conversionManager = new ConversionManager();

    public ConversionController(TableView<FileInfo> fileTable,
                                ChoiceBox<String> formatChoiceBox,
                                ChoiceBox<String> bitrateChoiceBox,
                                ChoiceBox<String> sampleRateChoiceBox,
                                ChoiceBox<String> channelChoiceBox,
                                Button convertButton,
                                Button clearButton,
                                Button cancelButton,
                                Button applyFormatButton,
                                ProgressBar overallProgress,
                                Label overallProgressText,
                                VBox configPanel,
                                TableController tableController) {
        this.fileTable = fileTable;
        this.formatChoiceBox = formatChoiceBox;
        this.bitrateChoiceBox = bitrateChoiceBox;
        this.sampleRateChoiceBox = sampleRateChoiceBox;
        this.channelChoiceBox = channelChoiceBox;
        this.convertButton = convertButton;
        this.clearButton = clearButton;
        this.cancelButton = cancelButton;
        this.applyFormatButton = applyFormatButton;
        this.overallProgress = overallProgress;
        this.overallProgressText = overallProgressText;
        this.configPanel = configPanel;
        this.tableController = tableController;
    }

    public void setup() {
        setupAudioSettings();

        convertButton.setOnAction(_ -> handleConvert());
        clearButton.setOnAction(_ -> handleClear());
        cancelButton.setOnAction(_ -> handleCancel());
        applyFormatButton.setOnAction(_ -> applyGlobalFormat());
    }

    private void setupAudioSettings() {
        bitrateChoiceBox.setItems(javafx.collections.FXCollections.observableArrayList("128k", "192k", "256k", "320k"));
        bitrateChoiceBox.setValue("192k");

        sampleRateChoiceBox.setItems(javafx.collections.FXCollections.observableArrayList("44100", "48000", "96000"));
        sampleRateChoiceBox.setValue("44100");

        channelChoiceBox.setItems(javafx.collections.FXCollections.observableArrayList("Mono", "Stereo"));
        channelChoiceBox.setValue("Stereo");

        formatChoiceBox.setItems(javafx.collections.FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");
    }

    // ---- Conversion Handling ----
    private void handleConvert() {
        try {
            if (!FFmpegManager.isFFmpegAvailable()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("FFmpeg Required");
                alert.setHeaderText("FFmpeg is not configured");
                alert.setContentText("You need to set the FFmpeg path before converting.");
                alert.showAndWait();
                return;
            }

            if (tableController.getFileData().isEmpty()) {
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

            conversionManager.startConversions(
                    tableController.getFileData(),
                    outputFormat,
                    this::updateGlobalProgress,
                    bitrate,
                    sampleRate,
                    channel,
                    outputDir
            );

            showAlert("Started conversion of " + tableController.getFileData().size() + " file(s).");

        } catch (Exception e) {
            showAlert("Conversion failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClear() {
        tableController.getFileData().clear();
        overallProgress.setProgress(0);
        configPanel.setVisible(false);
        configPanel.setManaged(false);
    }

    private void handleCancel() {
        conversionManager.cancelConversions();
        updateGlobalProgress();
    }

    private void applyGlobalFormat() {
        String globalFormat = formatChoiceBox.getValue();
        for (FileInfo file : tableController.getFileData()) {
            file.setTargetFormat(globalFormat);
        }
        showAlert("Applied global format (" + globalFormat + ") to all files.");
    }

    private void updateGlobalProgress() {
        if (tableController.getFileData().isEmpty()) {
            overallProgress.setProgress(0);
            return;
        }
        double sum = tableController.getFileData().stream().mapToDouble(f -> f.progressProperty().get()).sum();
        overallProgress.setProgress(sum / tableController.getFileData().size());

        int percent = (int) ((sum / tableController.getFileData().size()) * 100);
        overallProgressText.setText(percent + "%");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
