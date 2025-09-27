package se233.audioconverterapp1.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileInfo {
    private final StringProperty fileName;
    private final StringProperty format;
    private final StringProperty size;
    private final DoubleProperty progress;
    private final StringProperty status;

    public FileInfo(String fileName, String format, String size) {
        this.fileName = new SimpleStringProperty(fileName);
        this.format = new SimpleStringProperty(format);
        this.size = new SimpleStringProperty(size);
        this.progress = new SimpleDoubleProperty(0.0);
        this.status = new SimpleStringProperty("Pending");
    }

    public StringProperty fileNameProperty() { return fileName; }
    public StringProperty formatProperty() { return format; }
    public StringProperty sizeProperty() { return size; }
    public DoubleProperty progressProperty() { return progress; }
    public StringProperty statusProperty() { return status; }

    public void setProgress(double value) { this.progress.set(value); }
    public void setStatus(String value) { this.status.set(value); }
}
