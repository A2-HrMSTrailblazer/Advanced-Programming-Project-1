package se233.audioconverterapp1.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;

public class MainContentController {

    // องค์ประกอบ UI ที่ดึงมาจาก main_content_view.fxml
    @FXML private VBox mainContentRoot; // กล่องหลักสำหรับเนื้อหาหลัก
    @FXML private TableView<FileInfo> fileTable; // ตารางไฟล์เสียง
    @FXML private TableColumn<FileInfo, String> fileNameColumn, formatColumn, sizeColumn, statusColumn, targetFormatColumn; // คอลัมน์ต่าง ๆ
    @FXML private TableColumn<FileInfo, Double> progressColumn; // คอลัมน์ความคืบหน้าของแต่ละไฟล์
    @FXML private TableColumn<FileInfo, Void> actionColumn; // คอลัมน์ปุ่มการกระทำ
    @FXML private ChoiceBox<String> formatChoiceBox, bitrateChoiceBox, sampleRateChoiceBox, channelChoiceBox; // กล่องเลือกการตั้งค่าเสียง
    @FXML private Button applyFormatButton, convertButton, cancelButton, clearButton; // ปุ่มควบคุมการทำงาน
    @FXML private ProgressBar overallProgress; // แถบความคืบหน้ารวม
    @FXML private Label overallProgressText; // ข้อความแสดงเปอร์เซ็นต์ความคืบหน้ารวม

    private ObservableList<FileInfo> fileData; // รายการไฟล์เสียง
    private ConversionManager conversionManager; // ตัวจัดการการแปลงไฟล์

    // เมธอดเรียกใช้งานเมื่อโหลด FXML
    @FXML
    public void initialize() {
        setupChoiceBoxes();      // ตั้งค่ากล่องเลือกต่าง ๆ
        setupTableColumns();     // ตั้งค่าคอลัมน์ของตาราง
        setupActionColumn();     // ตั้งค่าคอลัมน์ปุ่มการกระทำ
        applyFormatButton.setOnAction(e -> applyGlobalFormat()); // เมื่อนำฟอร์แมตรูปแบบไปใช้
    }

    /**
     * เมธอดสำหรับรับข้อมูลไฟล์และ ConversionManager จากคลาสหลัก
     */
    public void initData(ObservableList<FileInfo> fileData, ConversionManager manager) {
        this.fileData = fileData;
        this.conversionManager = manager;
        fileTable.setItems(this.fileData); // ตั้งตารางให้ใช้ข้อมูลไฟล์ร่วมกัน
    }

    // --- เมธอด getter เพื่อให้คลาสแม่เรียกใช้งานปุ่มหรือค่า ---
    public Button getConvertButton() { return convertButton; }
    public Button getCancelButton() { return cancelButton; }
    public Button getClearButton() { return clearButton; }
    public String getSelectedFormat() { return formatChoiceBox.getValue(); }
    public String getSelectedBitrate() { return bitrateChoiceBox.getValue(); }
    public String getSelectedSampleRate() { return sampleRateChoiceBox.getValue(); }
    public String getSelectedChannel() { return channelChoiceBox.getValue(); }

    // เมธอดอัปเดตความคืบหน้าโดยรวม (Global Progress)
    public void updateGlobalProgress() {
        if (fileData == null || fileData.isEmpty()) {
            overallProgress.setProgress(0);
            overallProgressText.setText("0%");
            return;
        }
        double sum = fileData.stream().mapToDouble(f -> f.progressProperty().get()).sum();
        double progress = sum / fileData.size();
        overallProgress.setProgress(progress);
        overallProgressText.setText((int) (progress * 100) + "%");
    }

    // เมธอดตั้งค่าตัวเลือกใน ChoiceBox ต่าง ๆ
    private void setupChoiceBoxes() {
        formatChoiceBox.setItems(FXCollections.observableArrayList("mp3", "wav", "m4a", "flac"));
        formatChoiceBox.setValue("mp3");
        bitrateChoiceBox.setItems(FXCollections.observableArrayList("128k", "192k", "256k", "320k"));
        bitrateChoiceBox.setValue("192k");
        sampleRateChoiceBox.setItems(FXCollections.observableArrayList("44100", "48000", "96000"));
        sampleRateChoiceBox.setValue("44100");
        channelChoiceBox.setItems(FXCollections.observableArrayList("Mono", "Stereo"));
        channelChoiceBox.setValue("Stereo");
    }

    // เมธอดตั้งค่าคอลัมน์ของตารางไฟล์
    private void setupTableColumns() {
        fileNameColumn.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        formatColumn.setCellValueFactory(cell -> cell.getValue().formatProperty());
        sizeColumn.setCellValueFactory(cell -> cell.getValue().sizeProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
        progressColumn.setCellValueFactory(cell -> cell.getValue().progressProperty().asObject());
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn()); // แสดงแถบ progress
        targetFormatColumn.setCellValueFactory(cell -> cell.getValue().targetFormatProperty());
        targetFormatColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn("mp3", "wav", "m4a", "flac")); // ให้เปลี่ยน format ได้ในตาราง
        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // ปรับขนาดคอลัมน์ให้เหมาะสม
    }

    // เมธอดตั้งค่าคอลัมน์ของปุ่ม Cancel และ Clear ในแต่ละแถว
    private void setupActionColumn() {
        actionColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel"); // ปุ่มยกเลิกการแปลง
            private final Button clearBtn = new Button("Clear");   // ปุ่มลบไฟล์ออกจากรายการ

            {
                cancelBtn.setOnAction(_ -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    conversionManager.cancelConversion(file);
                });
                clearBtn.setOnAction(_ -> getTableView().getItems().remove(getIndex()));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, cancelBtn, clearBtn));
            }
        });
        cancelButton.getStyleClass().addAll("btn", "btn-secondary"); // ปุ่ม cancel ใส่คลาสตกแต่ง
        clearButton.getStyleClass().addAll("btn", "btn-danger");     // ปุ่ม clear ใส่คลาสตกแต่ง
    }

    // เมธอดนำฟอร์แมตรูปแบบจาก ChoiceBox ไปใช้กับไฟล์ทุกไฟล์ในรายการ
    private void applyGlobalFormat() {
        String globalFormat = formatChoiceBox.getValue();
        for (FileInfo file : fileData) {
            file.setTargetFormat(globalFormat);
        }
    }
}
