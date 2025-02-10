package com.gt.ssm.exception;

// Base exception class for all exceptions thrown by the application
// The message should be safe to pass back to the client-side to display to the user
public class SsmException extends RuntimeException {
    public SsmException(String msg) {
        super(msg);
    }

    public SsmException(String msg, Exception ex) {
        super(msg, ex);
    }
}
