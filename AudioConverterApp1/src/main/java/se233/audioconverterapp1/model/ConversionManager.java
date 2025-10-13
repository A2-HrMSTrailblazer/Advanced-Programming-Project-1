// ConversionManager.java
package se233.audioconverterapp1.model;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import se233.audioconverterapp1.exception.DuplicateOutputException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConversionManager {

    // แผนที่สำหรับเก็บ task ของแต่ละไฟล์ที่กำลังถูกแปลง
    private final Map<FileInfo, ConversionTask> activeTasks = new HashMap<>();

    // เมธอดสำหรับเริ่มการแปลงไฟล์เสียง (วนลูปแปลงทุกไฟล์ในรายการ)
    public void startConversions(ObservableList<FileInfo> files, String defaultFormat, Runnable onProgressUpdate,
                                 String bitrate, String sampleRate, String channel, File outputDirectory) {
        cancelConversions(); // ยกเลิกการแปลงที่กำลังดำเนินอยู่ก่อน

        for (FileInfo info : files) {
            try {
                // กำหนดฟอร์แมตเป้าหมาย ถ้าไม่ได้เลือกจะใช้ค่าส่วนกลาง
                String targetFormat = (info.getTargetFormat() != null && !info.getTargetFormat().isBlank())
                        ? info.getTargetFormat()
                        : defaultFormat;

                // ถ้าไฟล์ต้นฉบับเป็นฟอร์แมตเดียวกับเป้าหมาย ไม่ต้องแปลง
                String inputExt = info.getFormat().toLowerCase();
                if (inputExt.equals(targetFormat.toLowerCase())) {
                    throw new DuplicateOutputException(
                            "File \"" + info.getFileName() + "\" is already in ." + targetFormat + " format."
                    );
                }

                // สร้างชื่อไฟล์ผลลัพธ์ (เอานามสกุลเก่าออกแล้วใส่นามสกุลใหม่)
                String baseName = info.getFileName().replaceFirst("[.][^.]+$", ""); // ลบ extension เก่า
                File outputFile = new File(outputDirectory, baseName + "." + targetFormat);

                // เช็คถ้ามีไฟล์เดียวกันอยู่ในโฟลเดอร์ปลายทางอยู่แล้ว
                if (outputFile.exists()) {
                    throw new DuplicateOutputException(
                            "Output file already exists: " + outputFile.getAbsolutePath()
                    );
                }

                // สร้าง ConversionTask เพื่อแปลงไฟล์
                ConversionTask task = new ConversionTask(info, targetFormat, bitrate, sampleRate, channel, outputDirectory);

                // เมื่อ Task มี progress เปลี่ยน ให้ run callback (อัปเดต UI)
                task.progressProperty().addListener((_, _, _) -> Platform.runLater(onProgressUpdate));
                activeTasks.put(info, task);

                // เริ่มแปลงไฟล์ (รันเป็น thread เบื้องหลัง)
                Thread t = new Thread(task);
                t.setDaemon(true);
                t.start();

            } catch (DuplicateOutputException e) {
                // แจ้งเตือนถ้ามีไฟล์ซ้ำหรือฟอร์แมตซ้ำ
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Duplicate Output Detected");
                    alert.setHeaderText(null);
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
                info.setStatus("Skipped (Duplicate)"); // เปลี่ยนสถานะไฟล์เป็นข้าม
            } catch (Exception e) {
                // กรณีแปลงไฟล์ผิดพลาด
                e.printStackTrace();
                info.setStatus("Error");
            }
        }
    }

    // เมธอดยกเลิกการแปลงทุกไฟล์ที่กำลังดำเนินการอยู่
    public void cancelConversions() {
        for (ConversionTask task : activeTasks.values()) {
            task.cancel();
        }
        activeTasks.clear(); // ล้างข้อมูล task ที่ active อยู่
    }

    // เมธอดยกเลิกการแปลงเฉพาะไฟล์เดียว
    public void cancelConversion(FileInfo file) {
        ConversionTask task = activeTasks.get(file);
        if (task != null) {
            task.cancel();
            activeTasks.remove(file); // เอาไฟล์ออกจากรายการ active
        }
    }
}
