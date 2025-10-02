package se233.audioconverterapp1.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.concurrent.Task;
import se233.audioconverterapp1.util.FFmpegManager;
import se233.audioconverterapp1.util.FFprobeHelper;

/**
 * A background task to convert a single audio file.
 */
public class ConversionTask extends Task<Void> {

    private final FileInfo fileInfo;
    private final String targetFormat;
    private final String bitrate;
    private final String sampleRate;
    private final String channel;
    private final File outputDirectory;

    private final Pattern TIME_PATTERN = Pattern.compile("time=(\\d+):(\\d+):(\\d+).(\\d+)");

    public ConversionTask(FileInfo fileInfo, String targetFormat, String bitrate, String sampleRate, String channel, File outputDirectory) {
        this.fileInfo = fileInfo;
        this.targetFormat = targetFormat;
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.outputDirectory = outputDirectory;
    }

    @Override
    protected Void call() throws Exception {
        fileInfo.setProgress(0.0);
        fileInfo.setStatus("Converting...");

        String ffmpegPath = FFmpegManager.getFFmpegPath();
        if (ffmpegPath == null || ffmpegPath.isBlank()) {
            fileInfo.setStatus("FFmpeg not set");
            return null;
        }

        // Input file
        String inputPath = fileInfo.getFilePath();
        File inputFile = new File(inputPath);

        // Output file
        String baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
        String outputFormat = fileInfo.getTargetFormat();
        File outputFile = new File(outputDirectory, baseName + "." + outputFormat);

        try {
            List<String> command = new ArrayList<>();
            command.add(ffmpegPath);
            command.add("-y");
            command.add("-hide_banner");
            command.add("-stats");
            command.add("-i");
            command.add(inputFile.getAbsolutePath());
            command.add("-vn");

            if (bitrate != null && !bitrate.isBlank()) {
                command.add("-b:a");
                command.add(bitrate);
            }

            if (sampleRate != null && !sampleRate.isBlank()) {
                command.add("-ar");
                command.add(sampleRate);
            }

            if (channel != null && !channel.isBlank()) {
                String channelValue = channel.equalsIgnoreCase("Mono") ? "1" : "2";
                command.add("-ac");
                command.add(channelValue);
            }

            if ("m4a".equalsIgnoreCase(targetFormat)) {
                command.add("-c:a");
                command.add("aac");
            }

            command.add(outputFile.getAbsolutePath());

            double totalSeconds = FFprobeHelper.getDurationSeconds(inputFile);
            if (totalSeconds <= 0) {
                // System.out.println("[FFmpeg] Could not detect duration, progress will be fake.");
                totalSeconds = 1.0;
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);

                    // Real time progress
                    Matcher matcher = TIME_PATTERN.matcher(line);
                    if (matcher.find()) {
                        int h = Integer.parseInt(matcher.group(1));
                        int m = Integer.parseInt(matcher.group(2));
                        int s = Integer.parseInt(matcher.group(3));
                        int ms = Integer.parseInt(matcher.group(4));

                        double current = (h * 3600) + (m * 60) + s + (ms / 100.0);
                        double progress = Math.min(1.0, current / totalSeconds);

                        updateProgress(progress, 1.0);
                        fileInfo.setProgress(progress);
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                fileInfo.setStatus("Success");
                fileInfo.setProgress(1.0);
            }
            else {
                fileInfo.setStatus("Failed (exit " + exitCode + ")");
            }
        }
        catch (Exception e){
            fileInfo.setStatus("Failed");
            throw e;
        }
        return null;
    }
}
