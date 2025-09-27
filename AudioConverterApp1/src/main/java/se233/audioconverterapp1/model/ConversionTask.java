package se233.audioconverterapp1.model;

import javafx.concurrent.Task;

/**
 * A background task to convert a single audio file.
 * For now it simulates work by updating progress in a loop.
 * Later we can integrate FFmpeg command execution here.
 */
public class ConversionTask extends Task<Void> {

    private final FileInfo fileInfo;
    private final String targetFormat;

    public ConversionTask(FileInfo fileInfo, String targetFormat) {
        this.fileInfo = fileInfo;
        this.targetFormat = targetFormat;
    }

    @Override
    protected Void call() throws Exception {
        fileInfo.fileNameProperty().set(fileInfo.fileNameProperty().get() + " → " + targetFormat);
        fileInfo.setProgress(0.0);

        // Simulate conversion (later replace with FFmpeg)
        for (int i = 1; i <= 100; i++) {
            if (isCancelled()) break; // allow cancellation
            Thread.sleep(30); // simulate work
            updateProgress(i, 100);
            double currentProgress = (double) i / 100;
            fileInfo.setProgress(currentProgress);
        }

        return null;
    }
}
