package se233.audioconverterapp1.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Task;
import se233.audioconverterapp1.util.FFmpegManager;

/**
 * A background task to convert a single audio file.
 * For now it simulates work by updating progress in a loop.
 * Later we can integrate FFmpeg command execution here.
 */
public class ConversionTask extends Task<Void> {

    private final FileInfo fileInfo;
    private final String targetFormat;
    private final String bitrate;
    private final String sampleRate;
    private final String channel;

    public ConversionTask(FileInfo fileInfo, String targetFormat, String bitrate, String sampleRate, String channel) {
        this.fileInfo = fileInfo;
        this.targetFormat = targetFormat;
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channel = channel;
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
        File outputFile = new File(inputFile.getParentFile(), baseName + "." + outputFormat);

        try {
            List<String> command = new ArrayList<>();
            command.add(ffmpegPath);
            command.add("-y");
            command.add("-i");
            command.add(inputFile.getAbsolutePath());

            if (bitrate != null && !bitrate.isBlank()) {
                command.add("-b:a");
                command.add(bitrate);
            }

            if (sampleRate != null && !sampleRate.isBlank()) {
                command.add("-ar");
                command.add(sampleRate);
            }

            if (channel != null && !channel.isBlank()) {
                command.add("-ac");
                command.add(channel);
            }

            if ("m4a".equalsIgnoreCase(targetFormat)) {
                command.add("-c:a");
                command.add("aac");
            }

            command.add(outputFile.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);
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
