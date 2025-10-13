package se233.audioconverterapp1.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

// คลาสนี้ใช้ช่วยดึงข้อมูลระยะเวลา (duration) ของไฟล์เสียง/วีดีโอโดยใช้โปรแกรม ffprobe
public class FFprobeHelper {

    // เมธอดอ่านความยาวไฟล์ (หน่วยวินาที) ถ้าหาไม่เจอจะคืนค่า -1
    public static double getDurationSeconds(File file) {
        try {
            String ffmpegPath = FFmpegManager.getFFmpegPath(); // ดึง path ของ ffmpeg
            if (ffmpegPath == null || file == null || !file.exists()) return -1;

            // หา ffprobe.exe ในโฟลเดอร์เดียวกับ ffmpeg
            File ffmpegFile = new File(ffmpegPath);
            File ffprobeFile = new File(ffmpegFile.getParent(), "ffprobe.exe");

            if (!ffprobeFile.exists()) {
                System.err.println("[FFprobeHelper] ffprobe.exe not found in same folder as ffmpeg.");
                return -1; // ถ้าไม่เจอ ffprobe ให้หยุด
            }

            // สร้างคำสั่ง ffprobe เพื่อดึงข้อมูล duration ของไฟล์
            ProcessBuilder pb = new ProcessBuilder(
                    ffprobeFile.getAbsolutePath(),
                    "-v", "error",
                    "-show_entries", "format=duration",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    file.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // อ่านผลลัพธ์ที่ได้จาก ffprobe โดยดูแค่บรรทัดแรก
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.isBlank()) {
                    double duration = Double.parseDouble(line.trim());
                    process.waitFor();
                    return duration; // คืนค่า duration (หน่วยวินาที)
                }
            }

            process.waitFor();
        } catch (Exception e) {
            System.err.println("[FFprobeHelper] Failed to get duration for file: " + file.getAbsolutePath());
            e.printStackTrace();
        }
        return -1; // ถ้าไม่สามารถแปลงได้หรือเกิดข้อผิดพลาด
    }
}
