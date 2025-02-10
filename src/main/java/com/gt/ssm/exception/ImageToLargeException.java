package com.gt.ssm.exception;

public class ImageToLargeException extends ImageException {

    public ImageToLargeException(String errMsg)  {
        super(errMsg);
    }

    public ImageToLargeException(String errMsg, Exception ex) {
        super(errMsg, ex);
    }
}