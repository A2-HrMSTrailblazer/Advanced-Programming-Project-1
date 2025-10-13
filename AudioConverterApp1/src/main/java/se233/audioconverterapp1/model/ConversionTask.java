package se233.audioconverterapp1.model;

import javafx.concurrent.Task;
import se233.audioconverterapp1.util.FFmpegManager;
import se233.audioconverterapp1.util.FFprobeHelper;

import java.io.*;

public class ConversionTask extends Task<Void> {
    // ข้อมูลของไฟล์ที่จะทำการแปลงและค่าต่าง ๆ ที่ใช้ตั้งค่าการแปลง
    private final FileInfo fileInfo;         // ข้อมูลไฟล์ต้นฉบับ
    private final String outputFormat;       // ฟอร์แมตเป้าหมาย เช่น mp3, wav
    private final String bitrate;            // บิตเรตสำหรับไฟล์เป้าหมาย
    private final String sampleRate;         // sample rate สำหรับไฟล์เป้าหมาย
    private final String channel;            // จำนวนช่องเสียง (mono/stereo)
    private final File outputDir;            // โฟลเดอร์ปลายทางสำหรับไฟล์แปลง

    // คอนสตรัคเตอร์รับค่าตั้งต้นทั้งหมดที่ใช้ในการแปลงไฟล์
    public ConversionTask(FileInfo fileInfo, String outputFormat, String bitrate, String sampleRate, String channel, File outputDir) {
        this.fileInfo = fileInfo;
        this.outputFormat = outputFormat;
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.outputDir = outputDir;
    }

    // เมธอดหลักที่รันเมื่อเริ่ม task ใน thread เบื้องหลัง
    @Override
    protected Void call() {
        try {
            fileInfo.setStatus("Converting..."); // เปลี่ยนสถานะไฟล์เป็นกำลังแปลง

            String ffmpegPath = FFmpegManager.getFFmpegPath(); // เรียก path ของ FFmpeg
            if (ffmpegPath == null) {
                fileInfo.setStatus("FFmpeg not found");
                return null; // ถ้าไม่เจอ FFmpeg ให้หยุดทำงาน
            }

            File inputFile = new File(fileInfo.getFilePath());           // ไฟล์ต้นฉบับ
            File outputFile = new File(outputDir, getOutputName(inputFile, outputFormat)); // สร้างไฟล์ปลายทาง

            double totalDuration = FFprobeHelper.getDurationSeconds(inputFile); // ตรวจสอบระยะเวลาของไฟล์เสียง
            if (totalDuration <= 0) {
                System.err.println("[FFmpeg] Could not detect duration, using fake progress.");
                totalDuration = 1.0; // ถ้าตรวจสอบนานไม่ได้ ใช้ค่า default
            }

            // สร้างคำสั่ง ffmpeg สำหรับแปลงไฟล์พร้อมระบุค่าต่าง ๆ
            ProcessBuilder pb = new ProcessBuilder(
                    ffmpegPath,
                    "-y",
                    "-i", inputFile.getAbsolutePath(),
                    "-b:a", bitrate,
                    "-ar", sampleRate,
                    "-ac", channel.equalsIgnoreCase("mono") ? "1" : "2",
                    outputFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true); // รวม error กับ output

            Process process = pb.start(); // เริ่ม process

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                double lastProgress = 0.0;

                // อ่านผลลัพธ์จาก process เพื่ออัปเดต progress ใน UI
                while ((line = reader.readLine()) != null) {
                    if (isCancelled()) { // ถ้าถูกยกเลิก ให้ออกจากลูปและเปลี่ยนสถานะ
                        process.destroyForcibly();
                        fileInfo.setStatus("Cancelled");
                        return null;
                    }

                    // ค้นหาบรรทัดที่มีข้อมูลเวลา (เช่น time=00:01:23.45) เพื่อคำนวณเปอร์เซ็นต์
                    if (line.contains("time=")) {
                        double currentTime = parseCurrentTime(line);
                        double progress = Math.min(currentTime / totalDuration, 1.0);
                        lastProgress = progress;
                        updateProgress(progress, 1.0); // อัปเดต progress ใน Task
                        fileInfo.setProgress(progress); // อัปเดต progress ในข้อมูลไฟล์
                    }
                }

                process.waitFor(); // รอ process ทำงานเสร็จ

                // ถ้าสำเร็จ เปลี่ยนสถานะและอัปเดต progress
                if (process.exitValue() == 0) {
                    fileInfo.setStatus("Done");
                    updateProgress(1.0, 1.0);
                    fileInfo.setProgress(1.0);
                } else { // ถ้าไม่สำเร็จ เปลี่ยนสถานะว่าสำเร็จไม่ได้
                    fileInfo.setStatus("Failed");
                    updateProgress(lastProgress, 1.0);
                }
            }

        } catch (Exception e) { // ยกเว้นที่เกิดจากการแปลงไฟล์
            fileInfo.setStatus("Error");
            e.printStackTrace();
        }
        return null;
    }

    // สร้างชื่อไฟล์ผลลัพธ์โดยเอานามสกุลเก่าออกแล้วใส่นามสกุลใหม่
    private String getOutputName(File inputFile, String format) {
        String base = inputFile.getName().replaceFirst("[.][^.]+$", "");
        return base + "." + format;
    }

    // แปลงเวลาจากบรรทัดที่อ่านได้ของ ffmpeg ให้เป็นวินาที
    private double parseCurrentTime(String line) {
        // ตัวอย่าง: time=00:01:23.45
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
