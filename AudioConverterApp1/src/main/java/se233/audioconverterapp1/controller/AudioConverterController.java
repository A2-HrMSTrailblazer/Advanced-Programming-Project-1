package se233.audioconverterapp1.controller;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;
import se233.audioconverterapp1.view.ThemeController;

public class AudioConverterController {

    // ==== UI Elements ====
    @FXML
    private TableView<FileInfo> fileTable;
    @FXML
    private TableColumn<FileInfo, String> fileNameColumn;
    @FXML
    private TableColumn<FileInfo, String> formatColumn;
    @FXML
    private TableColumn<FileInfo, String> sizeColumn;
    @FXML
    private TableColumn<FileInfo, Double> progressColumn;
    @FXML
    private TableColumn<FileInfo, String> statusColumn;
    @FXML
    private TableColumn<FileInfo, String> targetFormatColumn;
    @FXML
    private TableColumn<FileInfo, Void> actionColumn;

    @FXML
    private ChoiceBox<String> formatChoiceBox;
    @FXML
    private ChoiceBox<String> bitrateChoiceBox;
    @FXML
    private ChoiceBox<String> sampleRateChoiceBox;
    @FXML
    private ChoiceBox<String> channelChoiceBox;
    @FXML
    private Button convertButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button applyFormatButton;
    @FXML
    private StackPane dropContainer;
    @FXML
    private Label dropZone;
    @FXML
    private ProgressBar overallProgress;
    @FXML
    private Label overallProgressText;
    @FXML
    private MenuItem setFFmpegPathMenu;
    @FXML
    private CheckBox darkModeToggle;

    @FXML
    private VBox configPanel;

    // ==== Data + Manager ====
    private final ObservableList<FileInfo> fileData = FXCollections.observableArrayList();
    private final ConversionManager conversionManager = new ConversionManager();

    // ==== Subcontrollers ====
    private TableController tableController;
    private FileImportController fileImportController;
    private ConversionController conversionController;

    // ==== View ====
    private ThemeController themeController;

    // ==== Initialization ====
    @FXML
    public void initialize() {
        tableController = new TableController(fileTable, fileNameColumn, formatColumn, sizeColumn, progressColumn, statusColumn, targetFormatColumn, actionColumn, conversionManager);
        fileImportController = new FileImportController(dropContainer, dropZone, fileData::add, this::showConfigPanel);
        tableController.setupTable(fileData);
        fileImportController.setupFileImport();
        setupFormatChoiceBox();
        conversionController = new ConversionController(fileTable, fileData, conversionManager, formatChoiceBox, bitrateChoiceBox, sampleRateChoiceBox, channelChoiceBox, overallProgress, overallProgressText, configPanel);
        setupButtons();
        setupAudioSettings();

        fileData.addListener((ListChangeListener<FileInfo>) _ -> {
            if (fileData.isEmpty()) {
                resetDropContainer();
            }
        });

        overallProgress.setProgress(0);
        overallProgressText.setText("0%");
    }

    // Reset Drop Container if there is no file
    private void resetDropContainer() {
        dropContainer.setMinHeight(180);
        dropContainer.setMaxHeight(200);
        dropZone.setText("Drop your audio files here or double click to select");
        configPanel.setVisible(false);
        configPanel.setManaged(false);
    }

    // ---- Format choice box ----
    private void setupFormatChoiceBox() {
        formatChoiceBox.setItems(FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");
    }

    // ---- Buttons ----
    private void setupButtons() {
        convertButton.setOnAction(_ -> conversionController.handleConvert());
        clearButton.setOnAction(_ -> conversionController.handleClear());
        cancelButton.setOnAction(_ -> conversionController.handleCancel());
        applyFormatButton.setOnAction(_ -> conversionController.applyGlobalFormat());
    }

    private void setupAudioSettings() {
        bitrateChoiceBox.setItems(FXCollections.observableArrayList("128 kbps", "192 kbps", "256 kbps", "320 kbps"));
        bitrateChoiceBox.setValue("192 kbps");

        sampleRateChoiceBox.setItems(FXCollections.observableArrayList("44100 Hz", "48000 Hz", "96000 Hz"));
        sampleRateChoiceBox.setValue("44100 Hz");

        channelChoiceBox.setItems(FXCollections.observableArrayList("Mono", "Stereo"));
        channelChoiceBox.setValue("Stereo");
    }

    private void showConfigPanel() {
        configPanel.setVisible(true);
        configPanel.setManaged(true);

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), configPanel);
        tt.setFromY(-20);
        tt.setToY(0);
        tt.play();
    }

    public void setThemeController(ThemeController themeController) {
        this.themeController = themeController;
        darkModeToggle.setOnAction(_ -> this.themeController.toggleDarkMode(darkModeToggle.isSelected()));
    }
}
