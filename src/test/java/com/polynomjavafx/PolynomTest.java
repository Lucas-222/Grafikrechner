package com.polynomjavafx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolynomTest {

    Polynom polynom;

    @BeforeEach
    void setUp() throws WrongInputSizeException {
        polynom = new Polynom(new double[]{0.0, 0.0, 2.0, 0.0, 0.0});
    }

    @Test
    void getExtrema() {
    }

    @Test
    void getInflectionPoints() {
    }

    @Test
    void getSaddlePoints() {
    }

}