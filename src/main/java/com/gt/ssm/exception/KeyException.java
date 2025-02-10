package com.gt.ssm.exception;

public class KeyException extends SsmException {
    public KeyException(String msg) {
        super(msg);
    }

    public KeyException(String msg, Exception ex) {
        super(msg, ex);
    }
}
