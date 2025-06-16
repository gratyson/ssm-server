package com.gt.ssm.exception;

public class SecretFileException extends SsmException {

    public SecretFileException(String errMsg)  {
        super(errMsg);
    }

    public SecretFileException(String errMsg, Exception ex) {
        super(errMsg, ex);
    }
}
