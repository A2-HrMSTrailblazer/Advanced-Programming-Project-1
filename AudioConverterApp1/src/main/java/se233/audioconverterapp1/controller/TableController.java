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
    private final TableView<FileInfo> fileTable;
    private final TableColumn<FileInfo, String> fileNameColumn;
    private final TableColumn<FileInfo, String> formatColumn;
    private final TableColumn<FileInfo, String> sizeColumn;
    private final TableColumn<FileInfo, Double> progressColumn;
    private final TableColumn<FileInfo, String> statusColumn;
    private final TableColumn<FileInfo, String> targetFormatColumn;
    private final TableColumn<FileInfo, Void> actionColumn;

    private final ObservableList<FileInfo> fileData = FXCollections.observableArrayList();
    private final ConversionManager conversionManager = new ConversionManager();

    public TableController(TableView<FileInfo> fileTable,
                           TableColumn<FileInfo, String> fileNameColumn,
                           TableColumn<FileInfo, String> formatColumn,
                           TableColumn<FileInfo, String> sizeColumn,
                           TableColumn<FileInfo, Double> progressColumn,
                           TableColumn<FileInfo, String> statusColumn,
                           TableColumn<FileInfo, String> targetFormatColumn,
                           TableColumn<FileInfo, Void> actionColumn) {
        this.fileTable = fileTable;
        this.fileNameColumn = fileNameColumn;
        this.formatColumn = formatColumn;
        this.sizeColumn = sizeColumn;
        this.progressColumn = progressColumn;
        this.statusColumn = statusColumn;
        this.targetFormatColumn = targetFormatColumn;
        this.actionColumn = actionColumn;
    }

    public void setupTable() {
        fileNameColumn.setCellValueFactory(cell -> cell.getValue().fileNameProperty());
        formatColumn.setCellValueFactory(cell -> cell.getValue().formatProperty());
        sizeColumn.setCellValueFactory(cell -> cell.getValue().sizeProperty());
        statusColumn.setCellValueFactory(cell -> cell.getValue().statusProperty());

        progressColumn.setCellValueFactory(cell -> cell.getValue().progressProperty().asObject());
        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

        targetFormatColumn.setCellValueFactory(cell -> cell.getValue().targetFormatProperty());
        targetFormatColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn("mp3", "wav", "m4a", "flac"));
        targetFormatColumn.setEditable(true);

        setupActionColumn();

        fileTable.setItems(fileData);
        fileTable.setEditable(true);
        fileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void setupActionColumn() {
        actionColumn.setCellFactory(_ -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            private final Button clearBtn = new Button("Delete");

            {
                cancelBtn.setOnAction(_ -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    conversionManager.cancelConversion(file);
                    file.setStatus("Cancelled");
                });

                clearBtn.setOnAction(_ -> {
                    FileInfo file = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(file);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(5, cancelBtn, clearBtn));
            }
        });
    }

    public ObservableList<FileInfo> getFileData() {
        return fileData;
    }
}
