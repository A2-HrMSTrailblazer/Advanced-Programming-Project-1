package se233.audioconverterapp1.model;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import se233.audioconverterapp1.exception.*;
import se233.audioconverterapp1.util.FFmpegManager;

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
                // ✅ 1. Check FFmpeg setup
                if (!FFmpegManager.isFFmpegAvailable()) {
                    throw new MissingFFmpegException("Please configure FFmpeg before starting a conversion.");
                }

                // ✅ 2. Validate format
                String targetFormat = (info.getTargetFormat() != null && !info.getTargetFormat().isBlank())
                        ? info.getTargetFormat()
                        : defaultFormat;

                String inputExt = info.getFormat().toLowerCase();
                if (inputExt.equalsIgnoreCase(targetFormat)) {
                    throw new InvalidAudioFormatException("File is already in ." + targetFormat + " format. \nIt will be re-encoded.");
                }

                // ✅ 3. Check for duplicate output
                String baseName = info.getFileName().replaceFirst("[.][^.]+$", "");
                File outputFile = new File(outputDirectory, baseName + "." + targetFormat);
                if (outputFile.exists()) {
                    throw new DuplicateOutputException("Output file already exists: " + outputFile.getName());
                }

                // ✅ 4. Start conversion
                ConversionTask task = new ConversionTask(info, targetFormat, bitrate, sampleRate, channel, outputDirectory);
                task.progressProperty().addListener((_, _, _) -> Platform.runLater(onProgressUpdate));
                activeTasks.put(info, task);

                Thread t = new Thread(task);
                t.setDaemon(true);
                t.start();

            } catch (Exception e) {
                // ✅ Centralized exception handler
                AppExceptionHandler.handle(e);

                // Mark the file status appropriately
                if (e instanceof DuplicateOutputException)
                    info.setStatus("Skipped (Duplicate)");
                else if (e instanceof InvalidAudioFormatException)
                    info.setStatus("Skipped (Same Format)");
                else
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
