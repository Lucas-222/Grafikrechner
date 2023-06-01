package com.polynomjavafx;

import java.util.*;

import javafx.scene.paint.Color;

public class Polynomial {
    private double[] coefficients;
    private int derivationCounter = 0;
    ArrayList<double[]> extrema = new ArrayList<>();
    ArrayList<double[]> inflections = new ArrayList<>();
    ArrayList<double[]> saddles = new ArrayList<>();

    private ArrayList<Double> roots = new ArrayList<>();
    Color polyColor;


    public Polynomial(double[] coefficients, Color... color) throws WrongInputSizeException {
        // Test if input is the wrong size
        if (coefficients.length != 6) {
            throw new WrongInputSizeException(coefficients.length);
        }
        this.coefficients = coefficients;
        this.polyColor = color.length != 0 ? color[0] : generateColor();
        try {
            this.roots = calculateRoots();
            this.extrema = calculateExtrema();
            this.inflections = calculateInflectionPoints();
            this.saddles = calculateSaddlePoints();
        } catch (ComputationFailedException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Double> getRoots() {
        return roots;
    }


    private static Color generateColor() {
        Random genRandom = new Random();
        int r = genRandom.nextInt(0, 256);
        int b = genRandom.nextInt(0, 256);
        int g = genRandom.nextInt(0, 256);

        return Color.rgb(r, g, b, 1.0);
    }

    private Polynomial(double[] coefficients, int derivationCounter, Color... color) {
        // Private constructor, that's why no exception check needs to be performed
        this.coefficients = coefficients;
        this.derivationCounter = derivationCounter;
        this.polyColor = color.length != 0 ? color[0] : generateColor();
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

    public boolean isAxisSymmetric() {
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

    public boolean isPointSymmetric() {
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

    public double functionValue(double x) {
        // Get the sum of all coefficients multiplied by x to the power of the exponent
        double functionValue = 0.0;

        for (int i = 0; i < this.coefficients.length; i++) {
            functionValue += this.coefficients[i] * Math.pow(x, i);
        }
        return functionValue;
    }

    private double[] derivationCoefficients() {
        // Example: (6x^4 - 12x^3 + 3x^2 + 4x + 8) --> (0 + 24x^3 - 36x^2 + 6x + 4)
        double[] derivation = { 0.0, 0.0, 0.0, 0.0, 0.0 };

        for (int i = 0; i < this.coefficients.length-1; i++) {
            // Multiply the coefficient with the exponent and subtract 1 from the exponent
            derivation[i] = (i+1) * this.coefficients[i+1];
        }
        return derivation;
    }

    public Polynomial derivationPolynom() {
        return new Polynomial(this.derivationCoefficients(), (this.derivationCounter+1));
    }

    private double[] antiderivativeCoefficients() {
        // Define the constant of integration
        double c = this.coefficients[this.coefficients.length - 1];

        // Calculate the antiderivative coefficients using the power rule
        double[] antiderivativeCoefficients = new double[coefficients.length + 1];
        for (int i = 0; i < coefficients.length; i++) {
            antiderivativeCoefficients[i] = coefficients[i] / (i + 1);
        }
        antiderivativeCoefficients[coefficients.length] = c;
        return antiderivativeCoefficients;
    }

    public Polynomial antiderivationPolynom() {
        return new Polynomial(this.antiderivativeCoefficients(), 0);
    }

    private ArrayList<Double> calculateRoots() {
        double[] startingValues = getStartingValues();
        double tol = 1.0e-6;
        int maxIter = 1000;

        ArrayList<Double> roots = new ArrayList<>();
        for (double x : startingValues) {
            for (int i = 0; i <= maxIter; i++) {
                double delta = this.functionValue(x) / this.derivationPolynom().functionValue(x);
                x -= delta;

                if (Math.abs(delta) < tol) {
                    // round roots if they are close to the next integer
                    // get difference between rounded root and root
                    if (getDifference(Math.round(Math.abs(x)), Math.abs(x)) <= 0.0001) {
                        roots.add((double) Math.round(x));
                    } else {
                        roots.add(Math.round(x*100.0)/100.0);
                    }
                    break; // break out of the loop once a root has been found
                }
            }
        }

        // remove duplicate roots (much more efficiently)
        HashSet<Double> uniqueSet = new HashSet<>();
        ArrayList<Double> rootsNoDuplicates = new ArrayList<>();

        for (double root : roots) {
            // taking advantage of a key quality of HashSets, which is that an element e isn't added
            // if an element e2 is already in the set, instead returning false
            if (uniqueSet.add(root)) {
                rootsNoDuplicates.add(root);
            }
        }
        Collections.sort(rootsNoDuplicates);
        return rootsNoDuplicates;
    }

    public double getDifference(double x, double y) {
        double difference;

        if (Math.abs(x) > Math.abs(y)) {
            difference = x - y;
        } else {
            difference = y - x;
        }

        return difference;
    }

    private double[] getStartingValues(){
        ArrayList<Double> startingValues = new ArrayList<>();
        // Size of the array
        int size = 50;
        // Range of the values
        double range = 0.5;

        ArrayList<Double> roots = this.getDegree() >= 1 ? this.derivationPolynom().calculateRoots() : new ArrayList<>(List.of(0.0));

        if (roots.size() == 0) {
            for (double i = -size / 2.0; i <= size / 2.0; i += range) {
                startingValues.add(i);
            }
        } else {
            for (double root : roots) {
                for (double i = root - size / 2.0; i <= root + size / 2.0; i += range) {
                    startingValues.add(i);
                }
            }
        }

        return startingValues.stream().mapToDouble(Double::doubleValue).toArray();
    }

    public ArrayList<double[]> calculateExtrema() throws ComputationFailedException {
        // before anything, return empty array list for degrees < 2
        if (this.getDegree() < 2) {
            return new ArrayList<>();
        }
        // first, get the derivative of the polynomial
        Polynomial firstDerivative = this.derivationPolynom();

        // then, get the roots of the derivative and their function values
        ArrayList<Double> firstDerivNulls = firstDerivative.calculateRoots();
        if (firstDerivNulls.isEmpty()) {
            throw new ComputationFailedException("extrema", this.toString(), "the first derivative has no roots/zeroes");
        }

        ArrayList<double[]> returnList = new ArrayList<>();
        for (double firstDerivNull: firstDerivNulls) {
            returnList.add(new double[]{firstDerivNull, this.functionValue(firstDerivNull)});
        }
        // return the array of null-value pairs
        return returnList;
    }

    public ArrayList<double[]> calculateInflectionPoints() throws ComputationFailedException {
        if (this.getDegree() < 3) {
            return new ArrayList<>();
        }
        // get the first and second derivatives of current function
        Polynomial secondDerivative = this.derivationPolynom().derivationPolynom();

        ArrayList<Double> secDerivNulls = secondDerivative.calculateRoots();
        if (secDerivNulls.isEmpty()) {
            throw new ComputationFailedException("inflection points", this.toString(), "the second derivative of the function " +
                    "has no roots/zeroes");
        }

        ArrayList<double[]> returnList = new ArrayList<>();
        for (double secDerivNull : secDerivNulls) {
            // example: 0.49249068954058 -> 490.0 -> 0.49
            returnList.add(new double[]{secDerivNull, this.functionValue(secDerivNull)});
        }

        // return an array of the inflection points
        return returnList;
    }

    public ArrayList<double[]> calculateSaddlePoints() throws ComputationFailedException {
        if (this.getDegree() < 3) {
            return new ArrayList<>();
        }
        // a function has a saddle point if its first and second derivatives equal zero
        Polynomial firstDerivative = this.derivationPolynom();
        Polynomial secondDerivative = firstDerivative.derivationPolynom();

        // get the zero of the second derivative and plug into the first derivative.
        // if both are zero, it's a saddle point.
        ArrayList<Double> secDerivNulls = secondDerivative.calculateRoots();
        if (secDerivNulls.isEmpty()) {
            throw new ComputationFailedException("saddle points", this.toString(), "the second derivative of the function " +
                    "has no roots/zeroes");
        }

        ArrayList<double[]> returnList = new ArrayList<>();
        for (double secDerivNull: secDerivNulls) {
            if (firstDerivative.functionValue(secDerivNull) == 0.0) {
                returnList.add(new double[]{secDerivNull, this.functionValue(secDerivNull)});
            }
        }
        return returnList;
    }

    public double getIntegral(double x1, double x2) {
        // Get bigger x value
        double biggerX = Math.max(x1, x2);
        // Get smaller x value
        double smallerX = Math.min(x1, x2);

        if (this.getDegree() == 0) {
            return (Math.abs(biggerX) + Math.abs(smallerX)) * this.coefficients[0];
        }

        // get anti-derivative
        Polynomial antiDerivative = this.antiderivationPolynom();
        // Get the integral
        return Math.abs(antiDerivative.functionValue(biggerX) - antiDerivative.functionValue(smallerX));
    }

    private String getOperator(int i) {
        // Check if the value is negative
        String operator = this.coefficients[i] < 0 ? "-" : i >= this.getDegree() ? "" : "+";
        // If operator is not the first operator, add whitespaces around it
        return i < this.getDegree() ? " " + operator + " " : operator;
    }

    private String getNumber(int i) {
        // If number is 1 --> (1.0) not (1.0x^0),  If number is an integer --> (3.0x) not (3.0x^1), Default --> (4.56x^2)
        return this.coefficients[i] == 1 && i >= 1 ? "" : this.coefficients[i] == Math.round(this.coefficients[i]) ? String.valueOf((int) Math.abs(this.coefficients[i])) : String.valueOf(Math.abs(this.coefficients[i]));
    }

    private String getExponent(int i) {
        // If exponent is 0 --> () not (x^0), If exponent is 1 --> (x) not (x^1), Default --> (x^2)
        return i == 0 ? "" : i == 1 ? "x" : "x^" + i;
    }

    @Override
    public String toString() {
        // Create a StringBuilder initialized with f(x) = | for every derivation add one apostrophe (')
        StringBuilder builder = new StringBuilder("f" + "'".repeat(this.derivationCounter) + "(x) = ");

        for (int i = this.coefficients.length-1; i >= 0; i--) {
            // If the coefficient is not 0, fill the builder with the operator, number and exponent
            if (this.coefficients[i] != 0) {
                builder.append(this.getOperator(i)).append(this.getNumber(i)).append(this.getExponent(i));
            }
        }
        return builder.toString();
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(double[] coefficients) {
        this.coefficients = coefficients;
    }


}