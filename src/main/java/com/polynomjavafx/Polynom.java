package com.polynomjavafx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polynom {
    private final double[] coefficients;

    public Polynom(double[] coefficients) throws WrongInputSizeException {
        // Test if input is the wrong size
        if (coefficients.length != 5) {
            throw new WrongInputSizeException(coefficients.length);
        }
        this.coefficients = coefficients;
    }

    public double[] getCoefficients() {
        return this.coefficients;
    }

    public int getDegree() {
        // Loop through the array and return the first value which isn't 0
        for (int i = this.coefficients.length-1; i >= 0; i--) {
            if (this.coefficients[i] != 0) {
                return i;
            }
        }
        return 0;
    }

    public boolean isAxissymmetric() {
        // If all exponents are even, the polynom is axis symmetric, else point symmetric
        for (int i = 0; i < this.coefficients.length; i++) {
            // If the exponent is odd, return false
            if (this.coefficients[i] != 0 && i % 2 != 0) {
                return false;
            }
        }
        // If every exponent where the value is not 0, is even
        return this.getDegree() != 0;
    }

    public boolean isPointsymmetric() {
        // If all exponents are odd, the polynom is point symmetric, else axis symmetric
        for (int i = 0; i < this.coefficients.length; i++) {
            // If the exponent is even, return false
            if (this.coefficients[i] != 0 && i % 2 == 0) {
                return false;
            }
        }
        // If every exponent where the value is not 0, is odd
        return this.getDegree() != 0;
    }

    public ArrayList<Double> getNull() {
        return this.getDegree() == 1 ? this.getNullLinear() : this.getDegree() == 2 ? this.getNullQuadratic() : new ArrayList<>();
    }

    private ArrayList<Double> getNullLinear() {
        // Multiply the value with the lowest exponent by -1 and divide it by the value with the exponent 1
        return new ArrayList<>(List.of((this.coefficients[0] * -1) / this.coefficients[1]));
    }

    private ArrayList<Double> getNullQuadratic() {
        // divide p and q by the value with the exponent 2
        double p = this.coefficients[1] / this.coefficients[2];
        double q = this.coefficients[0] / this.coefficients[2];

        double sqrt = Math.sqrt(Math.pow((p / 2), 2) - q);
        double x1 = -(p / 2) + sqrt;
        double x2 = -(p / 2) - sqrt;

        // Check if nulls are real numbers
        ArrayList<Double> list = new ArrayList<>();

        if (!Double.isNaN(x1)) {
            list.add(x1);
        }
        if (!Double.isNaN(x2) && x1 != x2) {
            list.add(x2);
        }
        return list;
    }

    public double functionValue(double x) {
        // Get the sum of all coefficients multiplied by x to the power of the exponent
        double functionValue = 0.0;

        for (int i = 0; i < this.coefficients.length; i++) {
            functionValue += this.coefficients[i] * Math.pow(x, i);
        }

        return functionValue;
    }

    public double[] derivationCoefficients() {
        // Example: (6x^4 - 12x^3 + 3x^2 + 4x + 8) --> (0 + 24x^3 - 36x^2 + 6x + 4)
        double[] derivation = { 0.0, 0.0, 0.0, 0.0, 0.0 };

        for (int i = 0; i < this.coefficients.length-1; i++) {
            // Multiply the coefficient with the exponent and subtract 1 from the exponent
            derivation[i] = (i+1) * this.coefficients[i+1];
        }
        return derivation;
    }

    public Polynom derivationPolynom() throws WrongInputSizeException {
        return new Polynom(this.derivationCoefficients());
    }

   public ArrayList<double[]> getExtrema() throws WrongInputSizeException, ArithmeticException {
        // first, get the derivative of the polynomial
        Polynom firstDerivative = this.derivationPolynom();
        // don't forget to handle cases where no extrema exist
       if (firstDerivative.getDegree() < 1) {
           throw new ArithmeticException("Can't compute the extrema of a polynomial below the second degree");
       }
       // then, get the roots of the derivative and their function values
       ArrayList<Double> nulls = firstDerivative.getNull();
       ArrayList<double[]> returnList = new ArrayList<>();
       for (int i = 0; i < firstDerivative.getDegree(); i++){
           returnList.add(new double[]{nulls.get(i), this.functionValue(nulls.get(i))});
       }
       // return the array of null-value pairs
       return returnList;
   }

   public ArrayList<double[]> getInflectionPoints() throws WrongInputSizeException, ArithmeticException {
        // get the first and second derivatives of current function
        Polynom secondDerivative = this.derivationPolynom().derivationPolynom();
       if (secondDerivative.getDegree() < 2) {
           throw new ArithmeticException("Can't compute the inflections of a polynomial below the third degree");
       }
        ArrayList<Double> secDerivNulls = secondDerivative.getNull();
        ArrayList<double[]> returnList = new ArrayList<>();
        for (double secDerivNull : secDerivNulls) {
            returnList.add(new double[]{secDerivNull, this.functionValue(secDerivNull)});
        }
        // return an array of the inflection points
        return returnList;
   }

}
