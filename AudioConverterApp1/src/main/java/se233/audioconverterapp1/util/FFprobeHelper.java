package se233.audioconverterapp1.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class FFprobeHelper {

    public static double getDurationSeconds(File file) {
        try {
            String ffmpegPath = FFmpegManager.getFFmpegPath();
            if (ffmpegPath == null || file == null || !file.exists()) return -1;

            File ffmpegFile = new File(ffmpegPath);
            File ffprobeFile = new File(ffmpegFile.getParent(), "ffprobe.exe");

            if (!ffprobeFile.exists()) {
                System.err.println("[FFprobeHelper] ffprobe.exe not found in same folder as ffmpeg.");
                return -1;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    ffprobeFile.getAbsolutePath(),
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.isBlank()) {
                    double duration = Double.parseDouble(line.trim());
                    process.waitFor();
                    return duration;
                }
            }

            process.waitFor();
        } catch (Exception e) {
            System.err.println("[FFprobeHelper] Failed to get duration for file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return -1;
    }
}
