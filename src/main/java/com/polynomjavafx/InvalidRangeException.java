package com.polynomjavafx;

public class InvalidRangeException extends Exception{
    double start;
    double end;
    public InvalidRangeException(double start, double end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String getMessage() {
        return "Invalid range input. End of range cannot equal or be lesser than start.  Your range was: " + start + " to " + end;
    }
}
