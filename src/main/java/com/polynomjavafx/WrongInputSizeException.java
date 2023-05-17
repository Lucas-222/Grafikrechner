package com.polynomjavafx;

public class WrongInputSizeException extends Exception {
    int length;

    public WrongInputSizeException(int length) {
        this.length = length;
    }

    @Override
    public String getMessage() {
        return "Wrong input length, input must be an array with the length of 6, your length was: " + length;
    }

}
