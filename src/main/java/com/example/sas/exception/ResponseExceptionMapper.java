package com.example.sas.exception;

public class ResponseExceptionMapper extends Exception {
    
    int status;

    public ResponseExceptionMapper() { 
        this.status = status;
    }

    public ResponseExceptionMapper(String message, int status) {
        super(message);
        this.status = status;
    }
}
