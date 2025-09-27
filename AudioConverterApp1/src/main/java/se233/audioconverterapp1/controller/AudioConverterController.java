package se233.audioconverterapp1.controller;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class AudioConverterController {

    // Table
    @FXML private TableView<FileInfo> fileTable;
    @FXML private TableColumn<FileInfo, String> fileNameColumn;
    @FXML private TableColumn<FileInfo, String> formatColumn;
    @FXML private TableColumn<FileInfo, String> sizeColumn;

    // Controls
    @FXML private ChoiceBox<String> formatChoiceBox;
    @FXML private Button convertButton;
    @FXML private Button clearButton;
    @FXML private Label dropZone;

    private final ObservableList<FileInfo> fileData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Bind columns to FileInfo properties
        fileNameColumn.setCellValueFactory(cellData -> cellData.getValue().fileNameProperty());
        formatColumn.setCellValueFactory(cellData -> cellData.getValue().formatProperty());
        sizeColumn.setCellValueFactory(cellData -> cellData.getValue().sizeProperty());

        // Bind data
        fileTable.setItems(fileData);

        // Populate choice box
        formatChoiceBox.setItems(FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");

        // Button actions
        convertButton.setOnAction(_ -> handleConvert());
        clearButton.setOnAction(_ -> handleClear());

        // Enable drag and drop
        setupDragAndDrop();
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
            boolean success = false;
            if (db.hasFiles()) {
                for (File file : db.getFiles()) {
                    if (isAudioFile(file)) {
                        fileData.add(new FileInfo(file.getName(), getExtension(file), String.valueOf(file.length() / 1024)));
                    }
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a") || name.endsWith(".flac");
    }

    private String getExtension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return (dot == -1) ? "" : name.substring(dot + 1);
    }

    private void handleConvert() {
        String outputFormat = formatChoiceBox.getValue();
        if (fileData.isEmpty()) {
            showAlert("No files to convert!");
        } else {
            showAlert("Pretend converting " + fileData.size() + " file(s) to " + outputFormat);
        }
    }

    private void handleClear() {
        fileData.clear();
        showAlert("File list cleared.");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Stub model class for table rows
    public static class FileInfo {
        private final javafx.beans.property.SimpleStringProperty fileName;
        private final javafx.beans.property.SimpleStringProperty format;
        private final javafx.beans.property.SimpleStringProperty size;

        public FileInfo(String fileName, String format, String size) {
            this.fileName = new javafx.beans.property.SimpleStringProperty(fileName);
            this.format = new javafx.beans.property.SimpleStringProperty(format);
            this.size = new javafx.beans.property.SimpleStringProperty(size);
        }

        public javafx.beans.property.StringProperty fileNameProperty() { return fileName; }
        public javafx.beans.property.StringProperty formatProperty() { return format; }
        public javafx.beans.property.StringProperty sizeProperty() { return size; }
    }
}
