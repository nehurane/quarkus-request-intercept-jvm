package uk.sky.sas.exception;

public class GlobalExceptionHandler extends Exception {
    
    int status;

    public GlobalExceptionHandler() { 
        this.status = status;
    }

    public GlobalExceptionHandler(String message, int status) {
        super(message);
        this.status = status;
    }
}
