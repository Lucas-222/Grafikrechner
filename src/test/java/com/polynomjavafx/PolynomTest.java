package com.polynomjavafx;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolynomTest {

    List<double[]> expected;
    List<double[]> actual;
    @DisplayName("Test method for calculating the extrema of the function(s)")
    @Test
    void getExtrema() throws WrongInputSizeException, ComputationFailedException {

        expected = new ArrayList<>(Arrays.asList(new double[]{-1.0, 9.0}, new double[]{0.11, 4.88}));
        actual = new Polynom(new double[]{5.0, -2.0, 8.0, 6.0, 0.0, 0.0}).getExtrema();
        assertArrayEquals(expected.get(0), actual.get(0));
        assertArrayEquals(expected.get(0), actual.get(0));

        expected = List.of(new double[]{-0.01, 3.03});
        actual = new Polynom(new double[]{ 3.0, -5.4, -234.4, 9.4, 0.0, 0.0 }).getExtrema();
        assertArrayEquals(expected.get(0), actual.get(0));


        expected = Arrays.asList(new double[]{0.0, 0.0}, new double[]{0.88, -2.10});
        actual = new Polynom(new double[]{ 0.0, 0.0, -8.0, 6.0, 0.0, 0.0 }).getExtrema();
        assertArrayEquals(expected.get(0), actual.get(0));
        // getRoots needs to be fixed, not all roots are calculated
        // assertArrayEquals(expected.get(1), actual.get(1));

        expected = List.of(new double[]{0.0, 12.0});
        actual = new Polynom(new double[]{ 12.0, 0.0, 2.0, 0.0, 0.0, 0.0 }).getExtrema();
        assertArrayEquals(expected.get(0), actual.get(0));
    }

    @DisplayName("Test method for calculating the inflection points of the function(s)")
    @Test
    void getInflectionPoints() throws WrongInputSizeException, ComputationFailedException {
        expected = List.of(new double[]{0.44, -1.06});
        actual = new Polynom(new double[]{0.0, 0.0, -8.0, 6.0, 0.0, 0.0}).getInflectionPoints();
        assertArrayEquals(expected.get(0), actual.get(0));

        expected = List.of(new double[]{0.0, -0.5});
        actual = new Polynom(new double[]{-0.5, 0.0, 0.0, -1.0, 0.0, 0.0}).getInflectionPoints();
        assertArrayEquals(expected.get(0), actual.get(0));

        expected = List.of(new double[]{-0.03, 2.97});
        actual = new Polynom(new double[]{3.0, 1.0, 1.0, 13.0, 0.0, 0.0}).getInflectionPoints();
        assertArrayEquals(expected.get(0), actual.get(0));

        expected = List.of(new double[]{-0.33, 1.25});
        actual = new Polynom(new double[]{1.0, -1.0, -1.0, -1.0, 0.0, 0.0}).getInflectionPoints();
        assertArrayEquals(expected.get(0), actual.get(0));
    }
    @DisplayName("Test method for calculating the saddle points of the function(s)")
    @Test
    void getSaddlePoints() throws WrongInputSizeException, ComputationFailedException {
        expected = List.of(new double[]{0.0, 0.0});
        actual = new Polynom(new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0}).getSaddlePoints();
        assertArrayEquals(expected.get(0), actual.get(0));
    }

}