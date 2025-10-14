package se233.audioconverterapp1.controller;

import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import se233.audioconverterapp1.exception.AppExceptionHandler;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;
import se233.audioconverterapp1.view.ThemeController;

public class AudioConverterController {

    // ==== ส่วนประกาศ UI Elements (องค์ประกอบในหน้าจอ) ====
    @FXML
    private TableView<FileInfo> fileTable; // ตารางสำหรับแสดงรายการไฟล์เสียง
    @FXML
    private TableColumn<FileInfo, String> fileNameColumn; // คอลัมน์ชื่อไฟล์
    @FXML
    private TableColumn<FileInfo, String> formatColumn; // คอลัมน์รูปแบบไฟล์
    @FXML
    private TableColumn<FileInfo, String> sizeColumn; // คอลัมน์ขนาดไฟล์
    @FXML
    private TableColumn<FileInfo, Double> progressColumn; // คอลัมน์แสดงความคืบหน้า
    @FXML
    private TableColumn<FileInfo, String> statusColumn; // คอลัมน์สถานะการแปลง
    @FXML
    private TableColumn<FileInfo, String> targetFormatColumn; // คอลัมน์รูปแบบเป้าหมาย
    @FXML
    private TableColumn<FileInfo, Void> actionColumn; // คอลัมน์ปุ่มการกระทำ (ลบ/ยกเลิก)

    @FXML
    private ChoiceBox<String> formatChoiceBox; // กล่องเลือกฟอร์แมตรูปแบบไฟล์
    @FXML
    private ChoiceBox<String> bitrateChoiceBox; // กล่องเลือกบิตเรตเสียง
    @FXML
    private ChoiceBox<String> sampleRateChoiceBox; // กล่องเลือกอัตราการสุ่มตัวอย่าง
    @FXML
    private ChoiceBox<String> channelChoiceBox; // กล่องเลือกช่องเสียง (โมโน/สเตอริโอ)
    @FXML
    private Button convertButton; // ปุ่มเริ่มแปลงไฟล์
    @FXML
    private Button clearButton; // ปุ่มล้างรายการไฟล์
    @FXML
    private Button cancelButton; // ปุ่มยกเลิกการแปลง
    @FXML
    private Button applyFormatButton; // ปุ่มนำค่าฟอร์แมตไปใช้กับทุกไฟล์
    @FXML
    private StackPane dropContainer; // พื้นที่ลากและวางไฟล์
    @FXML
    private Label dropZone; // ข้อความในพื้นที่วางไฟล์
    @FXML
    private ProgressBar overallProgress; // แถบแสดงความคืบหน้าโดยรวม
    @FXML
    private Label overallProgressText; // ตัวเลขเปอร์เซ็นต์ความคืบหน้าโดยรวม
    @FXML
    private MenuItem setFFmpegPathMenu; // เมนูตั้งค่า FFmpeg
    @FXML
    private CheckBox darkModeToggle; // เช็คบ็อกซ์เปิด/ปิดโหมดมืด
    @FXML
    private VBox configPanel; // ส่วนของแผงการตั้งค่า

    // ==== ส่วนข้อมูลและตัวจัดการการแปลง ====
    private final ObservableList<FileInfo> fileData = FXCollections.observableArrayList(); // รายการไฟล์เสียง
    private final ConversionManager conversionManager = new ConversionManager(); // ตัวจัดการการแปลงไฟล์

    // ==== ตัวควบคุมย่อย (Sub-controllers) ====
    private TableController tableController;
    private FileImportController fileImportController;
    private ConversionController conversionController;

    // ==== ตัวควบคุมธีม ====
    private ThemeController themeController;

    // ==== เมธอดเริ่มต้นเมื่อโหลด FXML ====
    @FXML
    public void initialize() {
        // สร้างตัวควบคุมย่อยเพื่อแบ่งหน้าที่แต่ละส่วน
        tableController = new TableController(fileTable, fileNameColumn, formatColumn, sizeColumn, progressColumn,
                statusColumn, targetFormatColumn, actionColumn, conversionManager);
        fileImportController = new FileImportController(dropContainer, dropZone, fileData::add, this::showConfigPanel);
        tableController.setupTable(fileData); // กำหนดตารางให้แสดงรายการไฟล์
        fileImportController.setupFileImport(); // ตั้งค่าการลากวางหรือนำเข้าไฟล์
        setupFormatChoiceBox(); // ตั้งค่าเมนูเลือกฟอร์แมตเบื้องต้น
        conversionController = new ConversionController(fileTable, fileData, conversionManager, formatChoiceBox,
                bitrateChoiceBox, sampleRateChoiceBox, channelChoiceBox, overallProgress, overallProgressText,
                configPanel);
        setupButtons(); // ตั้งค่าพฤติกรรมของปุ่มต่าง ๆ
        setupAudioSettings(); // ตั้งค่าเสียงเช่นบิตเรตและแซมเปิลเรต

        // ตรวจสอบว่ามีไฟล์ในรายการหรือไม่ ถ้าไม่มีให้รีเซ็ตกล่องวางไฟล์
        fileData.addListener((ListChangeListener<FileInfo>) _ -> {
            if (fileData.isEmpty()) {
                resetDropContainer();
            }
        });

        overallProgress.setProgress(0); // ตั้งค่าความคืบหน้าเริ่มต้น
        overallProgressText.setText("0%");
    }

    // รีเซ็ตกล่องวางไฟล์เมื่อไม่มีไฟล์ในรายการ
    private void resetDropContainer() {
        dropContainer.setMinHeight(180);
        dropContainer.setMaxHeight(200);
        dropZone.setText("Drop your audio files here or double click to select"); // ข้อความแนะนำ
        configPanel.setVisible(false);
        configPanel.setManaged(false);
    }

    // ตั้งค่ากล่องเลือกฟอร์แมตรูปแบบไฟล์
    private void setupFormatChoiceBox() {
        formatChoiceBox.setItems(FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");
    }

    // ตั้งค่าการทำงานของปุ่มต่าง ๆ
    private void setupButtons() {
        convertButton.setOnAction(_ -> conversionController.handleConvert());
        // เมื่อกดเริ่มแปลง
        clearButton.setOnAction(_ -> conversionController.handleClear()); // เมื่อล้างรายการ
        cancelButton.setOnAction(_ -> conversionController.handleCancel()); // เมื่อต้องการยกเลิก
        applyFormatButton.setOnAction(_ -> conversionController.applyGlobalFormat()); // นำฟอร์แมตไปใช้กับทุกไฟล์

        // เพิ่มคลาสสไตล์ถ้ายังไม่มี เพื่อใช้ตกแต่งปุ่ม
        if (!cancelButton.getStyleClass().contains("button"))
            cancelButton.getStyleClass().addAll("button", "button-secondary");
        if (!clearButton.getStyleClass().contains("button"))
            clearButton.getStyleClass().addAll("button", "button-danger");
    }

    // ตั้งค่าตัวเลือกเสียง เช่น บิตเรตและแซมเปิลเรต
    private void setupAudioSettings() {
        bitrateChoiceBox.setItems(FXCollections.observableArrayList("128 kbps", "192 kbps", "256 kbps", "320 kbps"));
        bitrateChoiceBox.setValue("192 kbps");

        sampleRateChoiceBox.setItems(FXCollections.observableArrayList("44100 Hz", "48000 Hz", "96000 Hz"));
        sampleRateChoiceBox.setValue("44100 Hz");

        channelChoiceBox.setItems(FXCollections.observableArrayList("Mono", "Stereo"));
        channelChoiceBox.setValue("Stereo");
    }

    // แสดงแผงการตั้งค่าด้วยแอนิเมชันเลื่อนลง
    private void showConfigPanel() {
        configPanel.setVisible(true);
        configPanel.setManaged(true);

        TranslateTransition tt = new TranslateTransition(Duration.millis(300), configPanel);
        tt.setFromY(-20);
        tt.setToY(0);
        tt.play();
    }

    // เชื่อมต่อกับ ThemeController สำหรับสลับโหมดมืด
    public void setThemeController(ThemeController themeController) {
        this.themeController = themeController;
        darkModeToggle.setOnAction(_ -> this.themeController.toggleDarkMode(darkModeToggle.isSelected()));
    }
}
