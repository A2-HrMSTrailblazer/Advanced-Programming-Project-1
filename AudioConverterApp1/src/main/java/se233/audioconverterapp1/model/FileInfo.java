package se233.audioconverterapp1.model;

import javafx.beans.property.*;
import java.io.File;

public class FileInfo {
    private final StringProperty fileName;
    private final StringProperty filePath;
    private final StringProperty format;
    private final StringProperty size;
    private final DoubleProperty progress;
    private final StringProperty status;

    public FileInfo(String filePath, String format, String size) {
        File file = new File(filePath);
        this.filePath = new SimpleStringProperty(file.getAbsolutePath());
        this.fileName = new SimpleStringProperty(file.getName()); // just display name
        this.format = new SimpleStringProperty(format);
        this.size = new SimpleStringProperty(size);
        this.progress = new SimpleDoubleProperty(0.0);
        this.status = new SimpleStringProperty("Pending");
    }

    public String getFilePath() { return filePath.get(); }
    public StringProperty filePathProperty() { return filePath; }

    public String getFileName() { return fileName.get(); }
    public StringProperty fileNameProperty() { return fileName; }

    public String getFormat() { return format.get(); }
    public StringProperty formatProperty() { return format; }

    public String getSize() { return size.get(); }
    public StringProperty sizeProperty() { return size; }

    public double getProgress() { return progress.get(); }
    public DoubleProperty progressProperty() { return progress; }
    public void setProgress(double value) { progress.set(value); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String value) { status.set(value); }
}
