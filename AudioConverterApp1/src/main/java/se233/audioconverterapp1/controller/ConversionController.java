package se233.audioconverterapp1.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import se233.audioconverterapp1.exception.AppExceptionHandler;
import se233.audioconverterapp1.exception.MissingFFmpegException;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;
import se233.audioconverterapp1.util.FFmpegManager;

import java.io.File;

public class ConversionController {
    // ประกาศตัวแปรสำหรับควบคุม UI และข้อมูลไฟล์
    private final TableView<FileInfo> fileTable; // ตารางแสดงไฟล์
    private final ObservableList<FileInfo> fileData; // ข้อมูลไฟล์ทั้งหมด
    private final ConversionManager conversionManager; // ตัวจัดการการแปลงไฟล์

    // ตัวเลือกตั้งค่าเสียง
    private final ChoiceBox<String> formatChoiceBox; // กล่องเลือกฟอร์แมตไฟล์
    private final ChoiceBox<String> bitrateChoiceBox; // กล่องเลือกบิตเรต
    private final ChoiceBox<String> sampleRateChoiceBox; // กล่องเลือก sample rate
    private final ChoiceBox<String> channelChoiceBox; // กล่องเลือกช่องเสียง

    private final ProgressBar overallProgress; // แถบความคืบหน้าโดยรวม
    private final Label overallProgressText; // ข้อความเปอร์เซ็นต์ความคืบหน้า
    private final VBox configPanel; // ส่วนแผงตั้งค่าการแปลงไฟล์

    // คอนสตรัคเตอร์ รับค่าควบคุมต่างๆจากคลาสแม่
    public ConversionController(TableView<FileInfo> fileTable,
            ObservableList<FileInfo> fileData,
            ConversionManager conversionManager,
            ChoiceBox<String> formatChoiceBox,
            ChoiceBox<String> bitrateChoiceBox,
            ChoiceBox<String> sampleRateChoiceBox,
            ChoiceBox<String> channelChoiceBox,
            ProgressBar overallProgress,
            Label overallProgressText,
            VBox configPanel) {
        this.fileTable = fileTable;
        this.fileData = fileData;
        this.conversionManager = conversionManager;
        this.formatChoiceBox = formatChoiceBox;
        this.bitrateChoiceBox = bitrateChoiceBox;
        this.sampleRateChoiceBox = sampleRateChoiceBox;
        this.channelChoiceBox = channelChoiceBox;
        this.overallProgress = overallProgress;
        this.overallProgressText = overallProgressText;
        this.configPanel = configPanel;
    }

    // ---- เมธอดจัดการการแปลงไฟล์ ----
    public void handleConvert() {
        try {
            // ตรวจสอบก่อนว่า FFmpeg พร้อมใช้งานหรือยัง
            if (!FFmpegManager.isFFmpegAvailable()) {
                throw new MissingFFmpegException("FFmpeg is not configured. Please set the FFmpeg path.");
            }

            // ถ้าไม่มีไฟล์ให้ขึ้นแจ้งเตือน
            if (fileData.isEmpty()) {
                showAlert("No files to convert!");
                return;
            }

            // ให้ผู้ใช้เลือกโฟลเดอร์เอาไฟล์ออก
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Output Folder");
            File outputDir = chooser.showDialog(fileTable.getScene().getWindow());
            if (outputDir == null) {
                showAlert("Conversion cancelled (no output folder selected).");
                return;
            }

            // ดึงค่าการตั้งค่าเสียง/ฟอร์แมตจาก UI เพื่อใช้ในการแปลงไฟล์
            String outputFormat = formatChoiceBox.getValue();
            String bitrate = bitrateChoiceBox.getValue().replace(" kbps", "k");
            String sampleRate = sampleRateChoiceBox.getValue().replace(" Hz", "");
            String channel = channelChoiceBox.getValue().equals("Mono") ? "1" : "2";

            // ตรวจสอบไฟล์ที่อยู่ในฟอร์แมตเดียวกันแล้วไม่ต้องแปลงซ้ำ
            for (FileInfo file : fileData) {
                String inputExt = getExtension(new File(file.getFilePath()));
                if (inputExt.equalsIgnoreCase(outputFormat)) {
                    showAlert("File \"" + file.getFileName() + "\" is already in ." + outputFormat
                            + " format. Skipping to avoid duplicate output.");
                    continue;
                }
            }

            // เรียกใช้งาน conversionManager เพื่อเริ่มแปลงไฟล์
            conversionManager.startConversions(
                    fileData,
                    outputFormat,
                    this::updateGlobalProgress,
                    bitrate,
                    sampleRate,
                    channel,
                    outputDir);

            showAlert("Started conversion of " + fileData.size() + " file(s).");
        } catch (Exception e) {
            // กรณีผิดพลาดจะแจ้งเตือน
            AppExceptionHandler.handle(e);
        }
    }

    // ฟังก์ชันแสดงแจ้งเตือนหากยังไม่ได้ตั้งค่า FFmpeg
    // private void showFFmpegAlert() {
    //     Alert alert = new Alert(Alert.AlertType.WARNING);
    //     alert.setTitle("FFmpeg Required");
    //     alert.setHeaderText("FFmpeg is not configured");
    //     alert.setContentText("You need to set the FFmpeg path before converting.");
        
    //     ButtonType okButton = new ButtonType("Set Path", ButtonBar.ButtonData.OK_DONE);

    //     alert.getButtonTypes().setAll(okButton, ButtonType.CANCEL);
    //     alert.showAndWait().ifPresent(response -> {
    //         if (response == okButton) chooseFFmpegPath();
    //     });
    // }

    // ดึงนามสกุลไฟล์จากชื่อไฟล์
    private String getExtension(File file) {
        int dot = file.getName().lastIndexOf('.');
        return (dot == -1) ? "" : file.getName().substring(dot + 1);
    }

    // เมธอดล้างรายการไฟล์ทั้งหมด และรีเซ็ตแผงตั้งค่า
    public void handleClear() {
        fileData.clear();
        overallProgress.setProgress(0);
        configPanel.setVisible(false);
        configPanel.setManaged(false);
    }

    // เมธอดยกเลิกการแปลงไฟล์ (กรณีแปลงหลายไฟล์พร้อมกัน)
    public void handleCancel() {
        conversionManager.cancelConversions();
        updateGlobalProgress();
    }

    // นำค่าฟอร์แมตรูปแบบไปใช้กับไฟล์ทั้งหมด
    public void applyGlobalFormat() {
        String globalFormat = formatChoiceBox.getValue();
        for (FileInfo file : fileData) {
            file.setTargetFormat(globalFormat);
        }
        showAlert("Applied global format (" + globalFormat + ") to all files.");
    }

    // เมธอดอัปเดตค่าแถบความคืบหน้าโดยรวมและตัวเลขเปอร์เซ็นต์
    public void updateGlobalProgress() {
        if (fileData.isEmpty()) {
            overallProgress.setProgress(0);
            overallProgressText.setText("0%");
            return;
        }

        double sum = fileData.stream().mapToDouble(f -> f.progressProperty().get()).sum();
        overallProgress.setProgress(sum / fileData.size());

        int percent = (int) ((sum / fileData.size()) * 100);
        overallProgressText.setText(percent + "%");
    }

    // เมธอดเลือกไฟล์ Exe สำหรับตั้งค่า FFmpeg
    // private void chooseFFmpegPath() {
    //     FileChooser chooser = new FileChooser();
    //     chooser.setTitle("Select FFmpeg Executable");
    //     chooser.getExtensionFilters().add(
    //             new FileChooser.ExtensionFilter("FFmpeg Executable", "ffmpeg.exe", "ffmpeg"));
    //     File file = chooser.showOpenDialog(fileTable.getScene().getWindow());
    //     if (file != null) {
    //         FFmpegManager.setFFmpegPath(file.getAbsolutePath());
    //     }
    // }

    // เมธอดแสดงแจ้งเตือนทั่วไปจากข้อความ
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
