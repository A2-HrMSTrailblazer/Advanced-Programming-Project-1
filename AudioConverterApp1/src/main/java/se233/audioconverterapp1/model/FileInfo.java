package se233.audioconverterapp1.model;

import javafx.beans.property.*;
import java.io.File;

// คลาสสำหรับเก็บข้อมูลไฟล์เสียงแต่ละไฟล์ในโปรแกรม
public class FileInfo {
    // ประกาศ property สำหรับผูกกับ UI ได้โดยตรง
    private final StringProperty fileName;       // ชื่อไฟล์ (เฉพาะชื่อ ไม่รวม path)
    private final StringProperty filePath;       // ที่อยู่ไฟล์แบบเต็ม
    private final StringProperty format;         // นามสกุลไฟล์ เช่น mp3, wav
    private final StringProperty size;           // ขนาดไฟล์เป็นข้อความ
    private final DoubleProperty progress;       // ความคืบหน้าในการแปลงไฟล์ (0.0 - 1.0)
    private final StringProperty status;         // สถานะของไฟล์ (Pending, Converting, Done, Error)
    private final StringProperty targetFormat;   // ฟอร์แมตเป้าหมายสำหรับแปลงไฟล์

    // คอนสตรัคเตอร์ รับ path, format, ขนาดไฟล์ตอนสร้าง object
    public FileInfo(String filePath, String format, String size) {
        File file = new File(filePath);
        this.filePath = new SimpleStringProperty(file.getAbsolutePath());    // กำหนด path ของไฟล์
        this.fileName = new SimpleStringProperty(file.getName());            // กำหนดชื่อไฟล์
        this.format = new SimpleStringProperty(format);                      // นามสกุลไฟล์
        this.size = new SimpleStringProperty(size);                          // ขนาดไฟล์
        this.progress = new SimpleDoubleProperty(0.0);                       // เริ่มต้น progress ที่ 0
        this.status = new SimpleStringProperty("Pending");                   // สถานะเริ่มต้น Pending
        this.targetFormat = new SimpleStringProperty("mp3");                 // ฟอร์แมตเป้าหมายเริ่มต้นเป็น mp3
    }

    // --- Getter/Setter และ Property สำหรับเชื่อมต่อ UI ---

    public String getFilePath() { return filePath.get(); }
    @SuppressWarnings("exports")
    public StringProperty filePathProperty() { return filePath; }

    public String getFileName() { return fileName.get(); }
    @SuppressWarnings("exports")
    public StringProperty fileNameProperty() { return fileName; }

    public String getFormat() { return format.get(); }
    @SuppressWarnings("exports")
    public StringProperty formatProperty() { return format; }

    public String getSize() { return size.get(); }
    @SuppressWarnings("exports")
    public StringProperty sizeProperty() { return size; }

    public double getProgress() { return progress.get(); }
    @SuppressWarnings("exports")
    public DoubleProperty progressProperty() { return progress; }
    public void setProgress(double value) { progress.set(value); }

    public String getStatus() { return status.get(); }
    @SuppressWarnings("exports")
    public StringProperty statusProperty() { return status; }
    public void setStatus(String value) { status.set(value); }

    public String getTargetFormat() { return targetFormat.get(); }
    public void setTargetFormat(String format) { targetFormat.set(format); }
    @SuppressWarnings("exports")
    public StringProperty targetFormatProperty() { return targetFormat; }
}
