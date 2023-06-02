package com.polynomjavafx;

public class UtilityClasses {
    public static Double roundToSecondDecimalPoint(double num) {
        return Math.round(num*100.0)/100.0;
    }
}
