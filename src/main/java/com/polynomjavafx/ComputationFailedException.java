package com.polynomjavafx;

public class ComputationFailedException extends Exception {

    String errorType;
    String cause;

    public ComputationFailedException(String errorType, String cause) {
        this.errorType = errorType;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return "failed to compute the " + errorType + " of the function: " + cause;
    }
}
