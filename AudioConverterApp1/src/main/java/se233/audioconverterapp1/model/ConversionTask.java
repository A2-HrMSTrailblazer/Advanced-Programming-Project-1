package se233.audioconverterapp1.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

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

        // Input file
        String inputPath = fileInfo.getFilePath();
        File inputFile = new File(inputPath);

        // Output file
        String baseName = inputFile.getName().replaceFirst("[.][^.]+$", "");
        String outputFormat = fileInfo.getTargetFormat();
        File outputFile = new File(inputFile.getParentFile(), baseName + "." + outputFormat);

        try {
            String ffmpegPath = "C:\\Users\\M S I\\Downloads\\ffmpeg-8.0-essentials_build\\ffmpeg-8.0-essentials_build\\bin\\ffmpeg.exe";
            List<String> command;
            if (targetFormat.equalsIgnoreCase("m4a")) {
                command = Arrays.asList(
                    ffmpegPath, "-y", "-i", inputPath, "-vn", "-c:a", "aac", outputFile.getAbsolutePath()
                );
            }
            else {
                command = Arrays.asList(
                    ffmpegPath, "-y", "-i", inputPath, outputFile.getAbsolutePath()
                );
            }
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);
                }
            }

            for (int i = 1; i <= 100; i++) {
                if (isCancelled()) {
                    fileInfo.setStatus("Cancelled");
                    break;
                }
                Thread.sleep(30);
                updateProgress(i, 100);
                fileInfo.setProgress(i / 100.0);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                fileInfo.setStatus("Success");
                fileInfo.setProgress(1.0);
            }
            else {
                fileInfo.setStatus("Failed");
            }
        }
        catch (Exception e){
            fileInfo.setStatus("Failed");
            throw e;
        }
        return null;
    }
}
