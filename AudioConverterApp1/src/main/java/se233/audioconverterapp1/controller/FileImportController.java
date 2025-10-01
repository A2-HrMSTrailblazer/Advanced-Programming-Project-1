package se233.audioconverterapp1.controller;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import se233.audioconverterapp1.model.FileInfo;

import java.io.File;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class FileImportController {
    private final StackPane dropContainer;
    private final Label dropZone;
    private final TableController tableController;

    public FileImportController(StackPane dropContainer, Label dropZone, TableController tableController) {
        this.dropContainer = dropContainer;
        this.dropZone = dropZone;
        this.tableController = tableController;
    }

    public void setupFileImport() {
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
                        new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.flac")
                );
                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dropContainer.getScene().getWindow());
                if (selectedFiles != null) {
                    selectedFiles.stream().filter(this::isAudioFile).forEach(this::addFileToTable);
                }
            }
        });
    }

    private void addFileToTable(File file) {
        FileInfo fileInfo = new FileInfo(
                file.getAbsolutePath(),
                getExtension(file),
                formatSize(file.length() / 1024)
        );
        tableController.getFileData().add(fileInfo);

        dropContainer.setMinHeight(60);
        dropContainer.setMaxHeight(80);
        dropZone.setText("Add more files by dropping here or double clicking");
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
}
