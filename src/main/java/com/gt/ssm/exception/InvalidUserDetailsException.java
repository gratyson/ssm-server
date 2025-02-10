package com.gt.ssm.exception;

public class InvalidUserDetailsException extends Exception {

    public InvalidUserDetailsException(String msg) {
        super(msg);
    }

    public InvalidUserDetailsException(String msg, Exception ex) {
        super(msg, ex);
    }
}
