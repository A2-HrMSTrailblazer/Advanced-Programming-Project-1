package se233.audioconverterapp1.controller;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;
import se233.audioconverterapp1.util.FFmpegManager;
import se233.audioconverterapp1.view.ThemeController;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

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
    private ThemeController themeController;

    // ==== Initialization ====
    @FXML
    public void initialize() {
        setupTable();
        setupFormatChoiceBox();
        setupButtons();
        setupFileImport();
        setupActionColumn();
        setupAudioSettings();

        fileData.addListener((ListChangeListener<FileInfo>) _ -> {
            if (fileData.isEmpty()) {
                resetDropContainer();
            }
        });

        overallProgress.setProgress(0);
    }

    // Reset Drop Container if there is no file
    private void resetDropContainer() {
        dropContainer.setMinHeight(180);
        dropContainer.setMaxHeight(200);
        dropZone.setText("Drop your audio files here or double click to select");
        configPanel.setVisible(false);
        configPanel.setManaged(false);
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
        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // ---- Format choice box ----
    private void setupFormatChoiceBox() {
        formatChoiceBox.setItems(FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");
    }

    // ---- Buttons ----
    private void setupButtons() {
        convertButton.setOnAction(_ -> handleConvert());
        clearButton.setOnAction(_ -> handleClear());
        cancelButton.setOnAction(_ -> handleCancel());
        applyFormatButton.setOnAction(_ -> applyGlobalFormat());
    }

    // ---- File import ----
    private void setupFileImport() {
        // Drag & drop
        dropContainer.setOnDragOver(event -> {
            if (event.getGestureSource() != dropContainer && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropContainer.setOnDragDropped(event -> {
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
        dropContainer.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Audio Files");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.flac"));
                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dropContainer.getScene().getWindow());
                if (selectedFiles != null) {
                    selectedFiles.stream().filter(this::isAudioFile).forEach(this::addFileToTable);
                }
            }
        });
    }

    // ---- Action column (per-file Cancel/Clear) ----
    private void setupActionColumn() {
        actionColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            private final Button clearBtn = new Button("Delete");

            {
                cancelBtn.setOnAction(_ -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    conversionManager.cancelConversion(file);
                    file.setStatus("Cancelled");
                });

                clearBtn.setOnAction(_ -> {
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

        sampleRateChoiceBox.setItems(FXCollections.observableArrayList("44100Hz", "48000Hz", "96000Hz"));
        sampleRateChoiceBox.setValue("44100Hz");

        channelChoiceBox.setItems(FXCollections.observableArrayList("Mono", "Stereo"));
        channelChoiceBox.setValue("Stereo");
    }

    // ---- Conversion handling ----
    private void handleConvert() {
        try {
            if (!FFmpegManager.isFFmpegAvailable()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("FFmpeg Required");
                alert.setHeaderText("FFmpeg is not configured");
                alert.setContentText("You need to set the FFmpeg path before converting.");

                ButtonType okButton = new ButtonType("Set Path", ButtonBar.ButtonData.OK_DONE);
                alert.getButtonTypes().setAll(okButton, ButtonType.CANCEL);

                alert.showAndWait().ifPresent(response -> {
                    if (response == okButton)
                        chooseFFmpegPath();
                });
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

    private void handleClear() {
        fileData.clear();
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

        int percent = (int) ((sum / fileData.size()) * 100);
        overallProgressText.setText(percent + "%");
    }

    // ---- File helpers ----
    private void addFileToTable(File file) {
        fileData.add(new FileInfo(
                file.getAbsolutePath(),
                getExtension(file),
                formatSize(file.length() / 1024)));

        if (!fileData.isEmpty()) {
            dropContainer.setMinHeight(60);
            dropContainer.setMaxHeight(80);
            dropZone.setText("Add more files by dropping here or double clicking");
            showConfigPanel();
        }
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

    // ---- Utility ----
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
