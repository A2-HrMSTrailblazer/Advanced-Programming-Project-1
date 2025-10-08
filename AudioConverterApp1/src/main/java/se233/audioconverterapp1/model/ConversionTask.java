package se233.audioconverterapp1.model;

import javafx.concurrent.Task;
import se233.audioconverterapp1.util.FFmpegManager;
import se233.audioconverterapp1.util.FFprobeHelper;

import java.io.*;

public class ConversionTask extends Task<Void> {
    private final FileInfo fileInfo;
    private final String outputFormat;
    private final String bitrate;
    private final String sampleRate;
    private final String channel;
    private final File outputDir;

    public ConversionTask(FileInfo fileInfo, String outputFormat, String bitrate, String sampleRate, String channel, File outputDir) {
        this.fileInfo = fileInfo;
        this.outputFormat = outputFormat;
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.outputDir = outputDir;
    }

    @Override
    protected Void call() {
        try {
            fileInfo.setStatus("Converting...");

            String ffmpegPath = FFmpegManager.getFFmpegPath();
            if (ffmpegPath == null) {
                fileInfo.setStatus("FFmpeg not found");
                return null;
            }

            File inputFile = new File(fileInfo.getFilePath());
            File outputFile = new File(outputDir, getOutputName(inputFile, outputFormat));

            double totalDuration = FFprobeHelper.getDurationSeconds(inputFile);
            if (totalDuration <= 0) {
                System.err.println("[FFmpeg] Could not detect duration, using fake progress.");
                totalDuration = 1.0; // fallback to fake progress
            }

            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-i", inputFile.getAbsolutePath(),
                    "-b:a", bitrate,
                    "-ar", sampleRate,
                    "-ac", channel.equalsIgnoreCase("mono") ? "1" : "2",
                    outputFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                double lastProgress = 0.0;

                while ((line = reader.readLine()) != null) {
                    if (isCancelled()) {
                        process.destroyForcibly();
                        fileInfo.setStatus("Cancelled");
                        return null;
                    }

                    if (line.contains("time=")) {
                        double currentTime = parseCurrentTime(line);
                        double progress = Math.min(currentTime / totalDuration, 1.0);
                        lastProgress = progress;
                        updateProgress(progress, 1.0);
                        fileInfo.setProgress(progress);
                    }
                }

                process.waitFor();

                if (process.exitValue() == 0) {
                    fileInfo.setStatus("Done");
                    updateProgress(1.0, 1.0);
                    fileInfo.setProgress(1.0);
                } else {
                    fileInfo.setStatus("Failed");
                    updateProgress(lastProgress, 1.0);
                }
            }

        } catch (Exception e) {
            fileInfo.setStatus("Error");
            e.printStackTrace();
        }
        return null;
    }

    private String getOutputName(File inputFile, String format) {
        String base = inputFile.getName().replaceFirst("[.][^.]+$", "");
        return base + "." + format;
    }

    private double parseCurrentTime(String line) {
        // Example: time=00:01:23.45
        try {
            int index = line.indexOf("time=");
            if (index != -1) {
                String timeStr = line.substring(index + 5, Math.min(index + 16, line.length()));
                String[] parts = timeStr.split(":");
                double hours = Double.parseDouble(parts[0]);
                double minutes = Double.parseDouble(parts[1]);
                double seconds = Double.parseDouble(parts[2]);
                return hours * 3600 + minutes * 60 + seconds;
            }
        } catch (Exception ignored) {}
        return 0.0;
    }
}
