package se233.audioconverterapp1.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;
import se233.audioconverterapp1.util.FFmpegManager;

import java.io.File;

public class ConversionController {
    private final TableView<FileInfo> fileTable;
    private final ObservableList<FileInfo> fileData;
    private final ConversionManager conversionManager;

    private final ChoiceBox<String> formatChoiceBox;
    private final ChoiceBox<String> bitrateChoiceBox;
    private final ChoiceBox<String> sampleRateChoiceBox;
    private final ChoiceBox<String> channelChoiceBox;

    private final ProgressBar overallProgress;
    private final Label overallProgressText;
    private final VBox configPanel;

    public ConversionController(TableView<FileInfo> fileTable,
            ObservableList<FileInfo> fileData,
            ConversionManager conversionManager,
            ChoiceBox<String> formatChoiceBox,
            ChoiceBox<String> bitrateChoiceBox,
            ChoiceBox<String> sampleRateChoiceBox,
            ChoiceBox<String> channelChoiceBox,
            ProgressBar overallProgress,
            Label overallProgressText,
            VBox configPanel) {
        this.fileTable = fileTable;
        this.fileData = fileData;
        this.conversionManager = conversionManager;
        this.formatChoiceBox = formatChoiceBox;
        this.bitrateChoiceBox = bitrateChoiceBox;
        this.sampleRateChoiceBox = sampleRateChoiceBox;
        this.channelChoiceBox = channelChoiceBox;
        this.overallProgress = overallProgress;
        this.overallProgressText = overallProgressText;
        this.configPanel = configPanel;
    }

    // ---- Conversion Handling ----
    public void handleConvert() {
        try {
            if (!FFmpegManager.isFFmpegAvailable()) {
                showFFmpegAlert();
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
            String bitrate = bitrateChoiceBox.getValue().replace(" kbps", "k");
            String sampleRate = sampleRateChoiceBox.getValue().replace(" Hz", "");
            String channel = channelChoiceBox.getValue().equals("Mono") ? "1" : "2";

            for (FileInfo file : fileData) {
                String inputExt = getExtension(new File(file.getFilePath()));
                if (inputExt.equalsIgnoreCase(outputFormat)) {
                    showAlert("File \"" + file.getFileName() + "\" is already in ." + outputFormat
                            + " format. Skipping to avoid duplicate output.");
                    continue;
                }
            }

            conversionManager.startConversions(
                    fileData,
                    outputFormat,
                    this::updateGlobalProgress,
                    bitrate,
                    sampleRate,
                    channel,
                    outputDir);

            showAlert("Started conversion of " + fileData.size() + " file(s).");

        } catch (Exception e) {
            showAlert("Conversion failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showFFmpegAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("FFmpeg Required");
        alert.setHeaderText("FFmpeg is not configured");
        alert.setContentText("You need to set the FFmpeg path before converting.");
        
        ButtonType okButton = new ButtonType("Set Path", ButtonBar.ButtonData.OK_DONE);

        alert.getButtonTypes().setAll(okButton, ButtonType.CANCEL);
        alert.showAndWait().ifPresent(response -> {
            if (response == okButton) chooseFFmpegPath();
        });
    }

    private String getExtension(File file) {
        int dot = file.getName().lastIndexOf('.');
        return (dot == -1) ? "" : file.getName().substring(dot + 1);
    }

    public void handleClear() {
        fileData.clear();
        overallProgress.setProgress(0);
        configPanel.setVisible(false);
        configPanel.setManaged(false);
    }

    public void handleCancel() {
        conversionManager.cancelConversions();
        updateGlobalProgress();
    }

    public void applyGlobalFormat() {
        String globalFormat = formatChoiceBox.getValue();
        for (FileInfo file : fileData) {
            file.setTargetFormat(globalFormat);
        }
        showAlert("Applied global format (" + globalFormat + ") to all files.");
    }

    public void updateGlobalProgress() {
        if (fileData.isEmpty()) {
            overallProgress.setProgress(0);
            overallProgressText.setText("0%");
            return;
        }

        double sum = fileData.stream().mapToDouble(f -> f.progressProperty().get()).sum();
        overallProgress.setProgress(sum / fileData.size());

        int percent = (int) ((sum / fileData.size()) * 100);
        overallProgressText.setText(percent + "%");
    }

    private void chooseFFmpegPath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select FFmpeg Executable");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("FFmpeg Executable", "ffmpeg.exe", "ffmpeg"));
        File file = chooser.showOpenDialog(fileTable.getScene().getWindow());
        if (file != null) {
            FFmpegManager.setFFmpegPath(file.getAbsolutePath());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
