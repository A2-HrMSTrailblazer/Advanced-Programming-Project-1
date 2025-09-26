package se233.audioconverterapp1.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

/**
 * Model class representing a single audio file conversion.
 * It uses JavaFX Properties to allow the UI to bind to its values.
 */
public class ConversionTask {

    private final File file;
    private final StringProperty status;
    private final DoubleProperty progress;

    public ConversionTask(File file) {
        this.file = file;
        this.status = new SimpleStringProperty("Pending");
        this.progress = new SimpleDoubleProperty(0.0);
    }

    public File getFile() {
        return file;
    }
    
    // --- JavaFX Property Methods ---

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public double getProgress() {
        return progress.get();
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress.set(progress);
    }
}
