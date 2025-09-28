package se233.audioconverterapp1.model;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class ConversionManager {

    private final List<ConversionTask> activeTasks = new ArrayList<>();

    public void startConversions(ObservableList<FileInfo> files, String outputFormat, Runnable onProgressUpdate) {
        cancelConversions();
        for (FileInfo info : files) {
            ConversionTask task = new ConversionTask(info, outputFormat);

            // Update global progress after each progress update
            task.progressProperty().addListener((obs, oldVal, newVal) ->
                    Platform.runLater(onProgressUpdate)
            );

            activeTasks.add(task);

            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        }
    }

    public void cancelConversions() {
        for (ConversionTask task : activeTasks) {
            task.cancel();
        }
        activeTasks.clear();
    }
}
