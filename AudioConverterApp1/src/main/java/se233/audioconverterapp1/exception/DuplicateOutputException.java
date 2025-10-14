package se233.audioconverterapp1.exception;

public class DuplicateOutputException extends RuntimeException{
    public DuplicateOutputException(String message) {
        super(message);
    }
}
