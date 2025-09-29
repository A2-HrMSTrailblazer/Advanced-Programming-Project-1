package se233.audioconverterapp1.util;

import java.util.prefs.Preferences;

public class FFmpegManager {
    private static final String PREF_KEY = "ffmpegPath";
    private static final Preferences prefs = Preferences.userNodeForPackage(FFmpegManager.class);

    public static String getFFmpegPath(){
        return prefs.get(PREF_KEY, "ffmpeg");
    }

    public static void setFFmpegPath(String path) {
        prefs.put(PREF_KEY, path);
    }

    public static boolean isFFmpegAvailable(){
        try {
            Process process = new ProcessBuilder(getFFmpegPath(), "-version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
