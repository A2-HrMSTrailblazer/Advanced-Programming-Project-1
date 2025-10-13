package se233.audioconverterapp1.util;

import java.util.prefs.Preferences;

// คลาสนี้ดูแลการจัดเก็บและเรียกใช้งาน path ของ FFmpeg ที่ผู้ใช้ตั้งไว้
public class FFmpegManager {
    // คีย์สำหรับเก็บค่า path ใน Preferences
    private static final String PREF_KEY = "ffmpegPath";
    // ตัวแปร Preferences สำหรับเก็บค่าของ FFmpegManager
    private static final Preferences prefs = Preferences.userNodeForPackage(FFmpegManager.class);

    // เมธอดสำหรับดึง path ของ FFmpeg จาก Preferences (หรือคืนค่า default "ffmpeg" หากยังไม่ได้ตั้ง)
    public static String getFFmpegPath(){
        return prefs.get(PREF_KEY, "ffmpeg");
    }

    // เมธอดสำหรับบันทึก path ของ FFmpeg ลง Preferences
    public static void setFFmpegPath(String path) {
        prefs.put(PREF_KEY, path);
    }

    // เมธอดตรวจสอบความพร้อมใช้งานของ FFmpeg โดยการรันคำสั่ง ffmpeg -version
    public static boolean isFFmpegAvailable(){
        try {
            Process process = new ProcessBuilder(getFFmpegPath(), "-version").start();
            int exitCode = process.waitFor();
            return exitCode == 0; // ถ้าออกปกติ แสดงว่า FFmpeg ใช้งานได้
        } catch (Exception e) {
            return false; // ถ้าเกิดข้อผิดพลาดหรือหา FFmpeg ไม่เจอ จะคืนค่า false
        }
    }
}
