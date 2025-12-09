package com.hospital.delegates.coding.exceptions;

/**
 * Custom exception for medical coding errors
 */
public class CodingException extends RuntimeException {

    private final String errorCode;
    private final String codeType;
    private final String failedCode;

    public CodingException(String message) {
        super(message);
        this.errorCode = "CODING_ERROR";
        this.codeType = null;
        this.failedCode = null;
    }

    public CodingException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CODING_ERROR";
        this.codeType = null;
        this.failedCode = null;
    }

    public CodingException(String message, String errorCode, String codeType, String failedCode) {
        super(message);
        this.errorCode = errorCode;
        this.codeType = codeType;
        this.failedCode = failedCode;
    }

    public CodingException(String message, String errorCode, String codeType, String failedCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.codeType = codeType;
        this.failedCode = failedCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getCodeType() {
        return codeType;
    }

    public String getFailedCode() {
        return failedCode;
    }

    @Override
    public String toString() {
        return String.format("CodingException[errorCode=%s, codeType=%s, failedCode=%s, message=%s]",
            errorCode, codeType, failedCode, getMessage());
    }
}
