package com.gt.ssm.exception;

public class SecretFileTooLargeException extends SecretFileException {

    public SecretFileTooLargeException(String errMsg)  {
        super(errMsg);
    }

    public SecretFileTooLargeException(String errMsg, Exception ex) {
        super(errMsg, ex);
    }
}
