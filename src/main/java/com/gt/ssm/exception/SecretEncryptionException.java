package com.gt.ssm.exception;

public class SecretEncryptionException extends SsmException {
    public SecretEncryptionException(String msg) {
        super(msg);
    }

    public SecretEncryptionException(String msg, Exception ex) {
        super(msg, ex);
    }
}
