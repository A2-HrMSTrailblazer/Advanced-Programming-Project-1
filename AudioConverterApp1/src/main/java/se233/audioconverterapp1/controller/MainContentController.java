package se233.audioconverterapp1.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;

public class MainContentController {

    // UI Elements from main_content_view.fxml
    @FXML private VBox mainContentRoot;
    @FXML private TableView<FileInfo> fileTable;
    @FXML private TableColumn<FileInfo, String> fileNameColumn, formatColumn, sizeColumn, statusColumn, targetFormatColumn;
    @FXML private TableColumn<FileInfo, Double> progressColumn;
    @FXML private TableColumn<FileInfo, Void> actionColumn;
    @FXML private ChoiceBox<String> formatChoiceBox, bitrateChoiceBox, sampleRateChoiceBox, channelChoiceBox;
    @FXML private Button applyFormatButton, convertButton, cancelButton, clearButton;
    @FXML private ProgressBar overallProgress;
    @FXML private Label overallProgressText;

    private ObservableList<FileInfo> fileData;
    private ConversionManager conversionManager;

    @FXML
    public void initialize() {
        setupChoiceBoxes();
        setupTableColumns();
        setupActionColumn();
        applyFormatButton.setOnAction(e -> applyGlobalFormat());
    }

    /**
     * Initializes the controller with shared data from the main controller.
     */
    public void initData(ObservableList<FileInfo> fileData, ConversionManager manager) {
        this.fileData = fileData;
        this.conversionManager = manager;
        fileTable.setItems(this.fileData);
    }

    // --- Getters for the main controller ---
    public Button getConvertButton() { return convertButton; }
    public Button getCancelButton() { return cancelButton; }
    public Button getClearButton() { return clearButton; }
    public String getSelectedFormat() { return formatChoiceBox.getValue(); }
    public String getSelectedBitrate() { return bitrateChoiceBox.getValue(); }
    public String getSelectedSampleRate() { return sampleRateChoiceBox.getValue(); }
    public String getSelectedChannel() { return channelChoiceBox.getValue(); }

    public void updateGlobalProgress() {
        if (fileData == null || fileData.isEmpty()) {
            overallProgress.setProgress(0);
            overallProgressText.setText("0%");
            return;
        }
        double sum = fileData.stream().mapToDouble(f -> f.progressProperty().get()).sum();
        double progress = sum / fileData.size();
        overallProgress.setProgress(progress);
        overallProgressText.setText((int) (progress * 100) + "%");
    }

    private void setupChoiceBoxes() {
        formatChoiceBox.setItems(FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");
        bitrateChoiceBox.setItems(FXCollections.observableArrayList("128k", "192k", "256k", "320k"));
        bitrateChoiceBox.setValue("192k");
        sampleRateChoiceBox.setItems(FXCollections.observableArrayList("44100", "48000", "96000"));
        sampleRateChoiceBox.setValue("44100");
        channelChoiceBox.setItems(FXCollections.observableArrayList("Mono", "Stereo"));
        channelChoiceBox.setValue("Stereo");
    }

    private void setupTableColumns() {
        fileNameColumn.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        formatColumn.setCellValueFactory(cell -> cell.getValue().formatProperty());
        sizeColumn.setCellValueFactory(cell -> cell.getValue().sizeProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        progressColumn.setCellValueFactory(cell -> cell.getValue().progressProperty().asObject());
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        targetFormatColumn.setCellValueFactory(cell -> cell.getValue().targetFormatProperty());
        targetFormatColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn("mp3", "wav", "m4a", "flac"));
        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    
    private void setupActionColumn() {
        actionColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            private final Button clearBtn = new Button("Clear");

            {
                cancelBtn.setOnAction(_ -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    conversionManager.cancelConversion(file);
                });
                clearBtn.setOnAction(_ -> getTableView().getItems().remove(getIndex()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, cancelBtn, clearBtn));
            }
        });
    }

    private void applyGlobalFormat() {
        String globalFormat = formatChoiceBox.getValue();
        for (FileInfo file : fileData) {
            file.setTargetFormat(globalFormat);
        }
    }
}