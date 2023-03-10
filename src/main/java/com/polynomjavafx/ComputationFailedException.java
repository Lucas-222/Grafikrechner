package com.polynomjavafx;

public class ComputationFailedException extends Exception {

    String errorType;
    String cause;
    String polynomial;

    public ComputationFailedException(String errorType, String polynomial, String cause) {
        this.errorType = errorType;
        this.polynomial = polynomial;
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return "failed to compute the " + errorType + " of the function " + polynomial + " :" + cause;
    }
}
