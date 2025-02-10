package com.gt.ssm.exception;

public class BlobException extends DaoException {

    public BlobException(String errMsg)  {
        super(errMsg);
    }

    public BlobException(String errMsg, Exception ex) {
        super(errMsg, ex);
    }
}
