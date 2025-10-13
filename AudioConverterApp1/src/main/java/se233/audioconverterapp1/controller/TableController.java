package se233.audioconverterapp1.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.layout.HBox;
import se233.audioconverterapp1.model.ConversionManager;
import se233.audioconverterapp1.model.FileInfo;

public class TableController {
    // ประกาศตัวแปรหมายถึงคอลัมน์และตารางแสดงไฟล์
    private final TableView<FileInfo> fileTable;           // ตารางแสดงไฟล์เสียง
    private final TableColumn<FileInfo, String> fileNameColumn;      // คอลัมน์ชื่อไฟล์
    private final TableColumn<FileInfo, String> formatColumn;        // คอลัมน์ฟอร์แมตไฟล์
    private final TableColumn<FileInfo, String> sizeColumn;          // คอลัมน์ขนาดไฟล์
    private final TableColumn<FileInfo, Double> progressColumn;      // คอลัมน์แสดงแถบสถานะความคืบหน้า
    private final TableColumn<FileInfo, String> statusColumn;        // คอลัมน์สถานะการแปลง
    private final TableColumn<FileInfo, String> targetFormatColumn;  // คอลัมน์ฟอร์แมตเป้าหมาย
    private final TableColumn<FileInfo, Void> actionColumn;          // คอลัมน์ปุ่มการกระทำ (Cancel, Delete)

    private final ObservableList<FileInfo> fileData = FXCollections.observableArrayList(); // รายการข้อมูลของไฟล์สำหรับตาราง
    private final ConversionManager conversionManager; // ตัวจัดการการแปลงไฟล์

    // คอนสตรัคเตอร์รับค่าต่าง ๆ จากคลาสแม่
    public TableController(TableView<FileInfo> fileTable,
                           TableColumn<FileInfo, String> fileNameColumn,
                           TableColumn<FileInfo, String> formatColumn,
                           TableColumn<FileInfo, String> sizeColumn,
                           TableColumn<FileInfo, Double> progressColumn,
                           TableColumn<FileInfo, String> statusColumn,
                           TableColumn<FileInfo, String> targetFormatColumn,
                           TableColumn<FileInfo, Void> actionColumn,
                           ConversionManager conversionManager) {
        this.fileTable = fileTable;
        this.fileNameColumn = fileNameColumn;
        this.formatColumn = formatColumn;
        this.sizeColumn = sizeColumn;
        this.progressColumn = progressColumn;
        this.statusColumn = statusColumn;
        this.targetFormatColumn = targetFormatColumn;
        this.actionColumn = actionColumn;
        this.conversionManager = conversionManager;
    }

    // เมธอดสำหรับตั้งค่าตารางหลัก ครอบคลุมทุกคอลัมน์และการใช้งาน
    public void setupTable(ObservableList<FileInfo> fileData) {
        setupGeneralInfoColumn();      // ตั้งค่าคอลัมน์ข้อมูลทั่วไป
        setupProgressColumn();         // ตั้งค่าคอลัมน์แถบ progress
        setupTargetFormatColumn();     // ตั้งค่าคอลัมน์ฟอร์แมตเป้าหมาย
        setupActionColumn();           // ตั้งค่าคอลัมน์ปุ่ม Cancel และ Delete

        fileTable.setItems(fileData);  // ให้ตารางแสดงรายการไฟล์ที่รับมา
        fileTable.setEditable(true);   // เปิดให้แก้ไขข้อมูลในตารางได้
        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // ปรับขนาดคอลัมน์อัตโนมัติ
    }

    // เมธอดตั้งค่าคอลัมน์ข้อมูลไฟล์ทั่วไป
    private void setupGeneralInfoColumn() {
        fileNameColumn.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        formatColumn.setCellValueFactory(cell -> cell.getValue().formatProperty());
        sizeColumn.setCellValueFactory(cell -> cell.getValue().sizeProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());
    }

    // เมธอดตั้งค่าคอลัมน์แถบ progress ของแต่ละไฟล์
    private void setupProgressColumn() {
        progressColumn.setCellValueFactory(cell -> cell.getValue().progressProperty().asObject());
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
    }

    // เมธอดตั้งค่าคอลัมน์ฟอร์แมตเป้าหมายให้เลือกอัตโนมัติจาก ChoiceBox ในตาราง
    private void setupTargetFormatColumn() {
        targetFormatColumn.setCellValueFactory(cell -> cell.getValue().targetFormatProperty());
        targetFormatColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn("mp3", "wav", "m4a", "flac"));
        targetFormatColumn.setEditable(true); // เปิดให้แก้ไขค่าในตาราง
    }

    // เมธอดตั้งค่าคอลัมน์ของปุ่ม Cancel และ Delete ในแต่ละแถวของไฟล์
    private void setupActionColumn() {
        actionColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel"); // ปุ่มยกเลิกการแปลง
            private final Button clearBtn = new Button("Delete");  // ปุ่มลบไฟล์ออกจากตาราง

            {
                // เมื่อกด Cancel จะยกเลิกการแปลงไฟล์และเปลี่ยนสถานะ
                cancelBtn.setOnAction(_ -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    conversionManager.cancelConversion(file);
                    file.setStatus("Cancelled");
                });

                // เมื่อกด Delete จะลบไฟล์ออกจากรายการในตาราง
                clearBtn.setOnAction(_ -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(file);
                });

                // เพิ่มคลาสตกแต่ง style ให้ปุ่ม Cancel และ Delete
                cancelBtn.getStyleClass().addAll("button", "button-secondary");
                clearBtn.getStyleClass().addAll("button", "button-danger");
            }

            // อัปเดต cell การแสดงผล (ถ้าไม่ว่างแสดงปุ่ม)
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, cancelBtn, clearBtn));
            }
        });
    }

    // เมธอด getter สำหรับข้อมูลไฟล์
    public ObservableList<FileInfo> getFileData() {
        return fileData;
    }
}
