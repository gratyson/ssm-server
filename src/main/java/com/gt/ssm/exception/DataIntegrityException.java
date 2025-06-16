package com.gt.ssm.exception;

public class DataIntegrityException extends SsmException {
    public DataIntegrityException(String msg) {
        super(msg);
    }

    public DataIntegrityException(String msg, Exception ex) {
        super(msg, ex);
    }
}
