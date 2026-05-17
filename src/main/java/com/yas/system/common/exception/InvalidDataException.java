package com.yas.system.common.exception;

public class InvalidDataException extends RuntimeException {

    private ErrorCode errorCode;

    public InvalidDataException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
