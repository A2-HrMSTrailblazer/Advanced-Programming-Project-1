package se233.audioconverterapp1.controller;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import se233.audioconverterapp1.model.FileInfo;

import java.io.File;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

public class FileImportController {
    // ประกาศตัวแปรใช้ควบคุม UI
    private final StackPane dropContainer; // กล่องรับไฟล์ (ลาก/วาง หรือคลิก)
    private final Label dropZone; // ป้ายข้อความบอกวิธีการใช้งาน
    private final Consumer<FileInfo> fileConsumer; // ฟังก์ชันรับ FileInfo เพื่อเพิ่มไปในรายการ
    private final Runnable showConfigPanelCallBack; // ฟังก์ชันโชว์ panel ตั้งค่าหลังเพิ่มไฟล์

    // คอนสตรัคเตอร์ รับค่าควบคุมจากคลาสแม่
    public FileImportController(StackPane dropContainer, Label dropZone, Consumer<FileInfo> fileConsumer, Runnable showConfigPanelCallBack) {
        this.dropContainer = dropContainer;
        this.dropZone = dropZone;
        this.fileConsumer = fileConsumer;
        this.showConfigPanelCallBack = showConfigPanelCallBack;
    }

    // เมธอดสำหรับตั้งค่าการนำเข้าไฟล์ (ลากวาง หรือดับเบิลคลิก)
    public void setupFileImport() {
        // ตั้งค่า drag & drop - เมื่อมีข้อมูลแฟ้มเข้ามาให้รับเฉพาะการคัดลอกไฟล์
        dropContainer.setOnDragOver(event -> {
            if (event.getGestureSource() != dropContainer && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        // เมื่อมีการวางไฟล์ลงในกล่อง (DragDropped)
        dropContainer.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                // กรองและเพิ่มเฉพาะไฟล์เสียงเท่านั้น
                db.getFiles().stream().filter(this::isAudioFile).forEach(this::addFile);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        // ดับเบิลคลิกที่กล่องเพื่อเลือกไฟล์ผ่าน FileChooser
        dropContainer.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Select Audio Files");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.flac")
                );
                List<File> selectedFiles = fileChooser.showOpenMultipleDialog(dropContainer.getScene().getWindow());
                if (selectedFiles != null) {
                    selectedFiles.stream().filter(this::isAudioFile).forEach(this::addFile);
                }
            }
        });
    }

    // เมธอดเพิ่มไฟล์เข้าในระบบ พร้อมปรับ UI และเรียกแสดง panel ตั้งค่า
    private void addFile(File file) {
        FileInfo fileInfo = new FileInfo(
                file.getAbsolutePath(),         // เส้นทางไฟล์
                getExtension(file),             // นามสกุลไฟล์
                formatSize(file.length() / 1024) // ขนาดไฟล์ (แสดงหน่วย KB)
        );
        fileConsumer.accept(fileInfo);   // ส่งข้อมูลไฟล์ไประบบหลัก

        // ปรับขนาด dropContainer และเปลี่ยนข้อความแนะนำ
        dropContainer.setMinHeight(60);
        dropContainer.setMaxHeight(80);
        dropZone.setText("Add more files by dropping here or double clicking");
        showConfigPanelCallBack.run();   // โชว์ panel ตั้งค่าหลังมีการเพิ่มไฟล์
    }

    // ตรวจสอบว่าไฟล์นั้นเป็นไฟล์เสียงหรือไม่ (โดยดูนามสกุล)
    private boolean isAudioFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp3") || name.endsWith(".wav")
                || name.endsWith(".m4a") || name.endsWith(".flac");
    }

    // ดึงนามสกุลไฟล์จากชื่อไฟล์ เพื่อนำไปใช้สร้าง FileInfo
    private String getExtension(File file) {
        int dot = file.getName().lastIndexOf('.');
        return (dot == -1) ? "" : file.getName().substring(dot + 1);
    }

    // แปลงขนาดไฟล์เป็นจำนวน KB พร้อม format ให้ดูอ่านง่าย
    private String formatSize(long sizeKB) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        return nf.format(sizeKB) + " KB";
    }
}
