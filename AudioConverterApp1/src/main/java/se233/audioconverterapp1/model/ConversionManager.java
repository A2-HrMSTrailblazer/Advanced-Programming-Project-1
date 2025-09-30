package se233.audioconverterapp1.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class ConversionManager {

    private final Map<FileInfo, ConversionTask> activeTasks = new HashMap<>();

    public void startConversions(ObservableList<FileInfo> files, String outputFormat, Runnable onProgressUpdate, String bitrate, String sampleRate, String channel, File outputDirectory) {
        cancelConversions();
        for (FileInfo info : files) {
            ConversionTask task = new ConversionTask(info, info.getTargetFormat() != null ? info.getTargetFormat() : outputFormat, bitrate, sampleRate, channel, outputDirectory);

            // Update global progress after each progress update
            task.progressProperty().addListener((_, _, _) ->
                    Platform.runLater(onProgressUpdate)
            );

            activeTasks.put(info, task);

            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
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
