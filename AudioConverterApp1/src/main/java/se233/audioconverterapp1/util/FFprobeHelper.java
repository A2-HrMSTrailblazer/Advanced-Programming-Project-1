package se233.audioconverterapp1.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class FFprobeHelper {

    public static double getDurationSeconds(File file) {
        try {
            String ffmpegPath = FFmpegManager.getFFmpegPath();
            if (ffmpegPath == null) return -1;

            File ffmpegFile = new File(ffmpegPath);
            File ffprobeFile = new File(ffmpegFile.getParent(), "ffprobe.exe");

            ProcessBuilder pb = new ProcessBuilder(ffprobeFile.getAbsolutePath(), "-v", "error", "-show_entries", "format=duration", "-of", "default=noprint_wrappers=1:nokey=1");
            file.getAbsolutePath();

            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    return Double.parseDouble(line);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }
    
}
