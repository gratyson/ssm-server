package com.gt.ssm.exception;

public class DaoException extends SsmException {

    public DaoException(String errMsg)  {
        super(errMsg);
    }

    public DaoException(String errMsg, Exception ex) {
        super(errMsg, ex);
    }
}
