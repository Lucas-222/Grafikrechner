package com.polynomjavafx;

public class InvalidRangeException extends Exception{
    public InvalidRangeException() {
    }

    @Override
    public String getMessage() {
        return "Invalid range input. Range start and end cannot be equal.";
    }
}
