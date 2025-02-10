package com.gt.ssm.exception;

public class SecurityException extends SsmException {
    public SecurityException(String msg) {
        super(msg);
    }

    public SecurityException(String msg, Exception ex) {
        super(msg, ex);
    }
}
