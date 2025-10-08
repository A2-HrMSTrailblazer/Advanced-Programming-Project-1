// ConversionManager.java
package se233.audioconverterapp1.model;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import se233.audioconverterapp1.exception.DuplicateOutputException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConversionManager {

    private final Map<FileInfo, ConversionTask> activeTasks = new HashMap<>();

    public void startConversions(ObservableList<FileInfo> files, String defaultFormat, Runnable onProgressUpdate,
                                 String bitrate, String sampleRate, String channel, File outputDirectory) {
        cancelConversions();
        for (FileInfo info : files) {
            try {
                // Determine the format actually used
                String targetFormat = (info.getTargetFormat() != null && !info.getTargetFormat().isBlank())
                        ? info.getTargetFormat()
                        : defaultFormat;

                // Skip conversion if same format as input
                String inputExt = info.getFormat().toLowerCase();
                if (inputExt.equals(targetFormat.toLowerCase())) {
                    throw new DuplicateOutputException(
                            "File \"" + info.getFileName() + "\" is already in ." + targetFormat + " format."
                    );
                }

                // Prepare output file name
                String baseName = info.getFileName().replaceFirst("[.][^.]+$", ""); // remove extension
                File outputFile = new File(outputDirectory, baseName + "." + targetFormat);

                // Check if output file already exists
                if (outputFile.exists()) {
                    throw new DuplicateOutputException(
                            "Output file already exists: " + outputFile.getAbsolutePath()
                    );
                }

                // Otherwise start the conversion
                ConversionTask task = new ConversionTask(info, targetFormat, bitrate, sampleRate, channel, outputDirectory);

                task.progressProperty().addListener((_, _, _) -> Platform.runLater(onProgressUpdate));
                activeTasks.put(info, task);

                Thread t = new Thread(task);
                t.setDaemon(true);
                t.start();

            } catch (DuplicateOutputException e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Duplicate Output Detected");
                    alert.setHeaderText(null);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
                info.setStatus("Skipped (Duplicate)");
            } catch (Exception e) {
                e.printStackTrace();
                info.setStatus("Error");
            }
        }
    }

    public void cancelConversions() {
        for (ConversionTask task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear();
    }

    public void cancelConversion(FileInfo file) {
        ConversionTask task = activeTasks.get(file);
        if (task != null) {
            task.cancel();
            activeTasks.remove(file);
        }
    }
}
