package com.polynomjavafx;

import java.util.ArrayList;
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

    public ArrayList<Double> getNull(double[] Polynomial) {
        // If function is linear or quadratic (degree 1 or 2), use the quadratic formula else return a new ArrayList
        if (Polynomial != null) {
            return this.getNullQuadratic(Polynomial);
        } else {
            return this.getDegree() == 1 ? this.getNullLinear() : this.getDegree() == 2 ? this.getNullQuadratic(this.coefficients) : this.getDegree() == 3 ? this.getNullCubic() : new ArrayList<>();
        }
    }

    private ArrayList<Double> getNullLinear() {
        // Multiply the value with the lowest exponent by -1 and divide it by the value with the exponent 1
        return new ArrayList<>(List.of((this.coefficients[0] * -1) / this.coefficients[1]));
    }

    private ArrayList<Double> getNullQuadratic(double[] Polynomial) {
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

    private ArrayList<Double> getNullCubic() {
        ArrayList<Double> list = new ArrayList<>();

        // Initial guesses for the list
        double x1 = 1.0;
        double x2 = -0.5;
        double x3 = -1.0;

        // Number of iterations to perform
        int iterations = 100;

        // Tolerance for convergence
        double tolerance = 1e-10;

        // Perform Newton-Raphson iterations to refine the list
        for (int i = 0; i < iterations; i++) {
            // Evaluate the function and its first and second derivatives at each root
            double f1 = coefficients[0] * x1 * x1 * x1 + coefficients[1] * x1 * x1 + coefficients[2] * x1 + coefficients[3];
            double f2 = coefficients[0] * x2 * x2 * x2 + coefficients[1] * x2 * x2 + coefficients[3] * x2 + coefficients[3];
            double f3 = coefficients[0] * x3 * x3 * x3 + coefficients[1] * x3 * x3 + coefficients[3] * x3 + coefficients[3];
            double f1prime = 3 * coefficients[0] * x1 * x1 + 2 * coefficients[1] * x1 + coefficients[3];
            double f2prime = 3 * coefficients[0] * x2 * x2 + 2 * coefficients[1] * x2 + coefficients[3];
            double f3prime = 3 * coefficients[0] * x3 * x3 + 2 * coefficients[1] * x3 + coefficients[3];
            double f1primeprime = 6 * coefficients[0] * x1 + 2 * coefficients[1];
            double f2primeprime = 6 * coefficients[0] * x2 + 2 * coefficients[1];
            double f3primeprime = 6 * coefficients[0] * x3 + 2 * coefficients[1];

            // Update each root using the Newton-Raphson formula
            x1 = x1 - f1 / f1prime + 0.5 * f1 * f1prime / (f1prime * f1prime - f1 * f1primeprime);
            x2 = x2 - f2 / f2prime + 0.5 * f2 * f2prime / (f2prime * f2prime - f2 * f2primeprime);
            x3 = x3 - f3 / f3prime + 0.5 * f3 * f3prime / (f3prime * f3prime - f3 * f3primeprime);

            // Check for convergence
            if (Math.abs(f1) < tolerance && Math.abs(f2) < tolerance && Math.abs(f3) < tolerance) {
                break;
            }
        }

        // Assign the refined list to the output array
        list.add(x1);
        list.add(x2);
        list.add(x3);

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

   public ArrayList<Double> getExtremaQuadratic() {
        // first, get the derivative of the polynomial
        double[] derivCoeff = this.derivationCoefficients();
        // then, get the roots of the derivative
        ArrayList<Double> nulls = this.getNull(derivCoeff);
        // plug the roots into the initial function to get the values
       ArrayList<Double> funcValues = new ArrayList<Double>();
       funcValues.add(this.functionValue(nulls.get(0)));
       funcValues.add(this.functionValue(nulls.get(1)));
       // lastly, create an Array of points and return it
       ArrayList<Double> returnList = new ArrayList<>();
       for (int i = 0; i < funcValues.size(); i++) {
           returnList.add(nulls.get(i));
           returnList.add(funcValues.get(i));
       }
       return returnList;
   }

}
