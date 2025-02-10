package com.gt.ssm.exception;

public class ImageException extends SsmException {

    public ImageException(String errMsg)  {
        super(errMsg);
    }

    public ImageException(String errMsg, Exception ex) {
        super(errMsg, ex);
    }
}