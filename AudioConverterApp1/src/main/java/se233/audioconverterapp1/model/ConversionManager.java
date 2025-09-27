package se233.audioconverterapp1.model;

import javafx.application.Platform;
import javafx.collections.ObservableList;

public class ConversionManager {

    public void startConversions(ObservableList<FileInfo> files, String outputFormat, Runnable onProgressUpdate) {
        for (FileInfo info : files) {
            ConversionTask task = new ConversionTask(info, outputFormat);

            // Update global progress after each progress update
            task.progressProperty().addListener((obs, oldVal, newVal) ->
                    Platform.runLater(onProgressUpdate)
            );

            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        }
    }
}
