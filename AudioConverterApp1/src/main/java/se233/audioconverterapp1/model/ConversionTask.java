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
        fileInfo.setProgress(0.0);
        fileInfo.setStatus("Converting...");

        try {
            for (int i = 1; i <= 100; i++) {
                if (isCancelled()) {
                    fileInfo.setStatus("Cancelled");
                    break;
                }
                Thread.sleep(30);
                updateProgress(i, 100);
                fileInfo.setProgress(i / 100.0);
            }

            if(!isCancelled()) {
                fileInfo.setStatus("Success");
            }
        }
        catch (Exception e){
            fileInfo.setStatus("Failed");
            throw e;
        }
        return null;
    }
}
