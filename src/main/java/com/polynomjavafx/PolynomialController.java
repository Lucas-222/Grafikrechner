package com.polynomjavafx;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolynomialController {
    public Spinner<Double> coefficient5Spinner;
    public Spinner<Double> coefficient4Spinner;
    public Spinner<Double> coefficient3Spinner;
    public Spinner<Double> coefficient2Spinner;
    public Spinner<Double> coefficient1Spinner;
    public Spinner<Double> coefficient0Spinner;
    public Canvas polynomialCanvas;
    public Label inputWarningLabel;
    public Label symmetryLabel;
    public Label functionAsStringLabel;
    public Label degreeLabel;
    public Label rootLabel;
    public Label extremaLabel;
    public Label inflectionLabel;
    public Label saddleLabel;
    private GraphicsContext graphicsContext;

    private double xScale;
    private double yScale;
    private Polynom polynom;

    @FXML
    private void initialize() {
        graphicsContext = polynomialCanvas.getGraphicsContext2D();
        this.yScale = polynomialCanvas.getHeight() / 10;
        this.xScale = polynomialCanvas.getWidth() / 10;
        initializeSpinners();
        drawAxis();
    }

    private void initializeSpinners() {
        //Upcast list to Arraylist
        List<Spinner<Double>> spinners = Arrays.asList(coefficient0Spinner, coefficient1Spinner, coefficient2Spinner, coefficient3Spinner, coefficient4Spinner,
                coefficient5Spinner);
        for (Spinner<Double> spinner : spinners) {
            //Set value factory
            spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-200, 200, 0.0, 0.1));
            //Add listener to textProperty so only valid input is accepted
            spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("-?[0-9]+\\.?[0-9]*")) {
                            if (newValue.equals("")) {
                                spinner.getEditor().setText("0");
                            } else {
                                spinner.getEditor().setText(oldValue);
                            }
                        }
                    }
            );
        }
    }

    @FXML
    private void onSubmitButtonClicked() throws WrongInputSizeException {
        try {
            double[] coefficients = {coefficient0Spinner.getValue(), coefficient1Spinner.getValue(),
                    coefficient2Spinner.getValue(), coefficient3Spinner.getValue(),
                    coefficient4Spinner.getValue(), coefficient5Spinner.getValue()};

            polynom = new Polynom(coefficients);

            // show information about polynomial
            showFunctionAsString();
            showDegree();

            // show symmetry and roots if degree is <= 3
            if (polynom.getDegree() <= 3) {
                showSymmetry();
                showRoots();
                showExtrema();
                showInflectionPoints();
                showSaddlePoints();
            }

            inputWarningLabel.setVisible(false);
            drawPolynomialToCanvas(polynom, Color.RED);
        } catch (NumberFormatException invalidInput) {
            inputWarningLabel.setVisible(true);
        }

    }

    @FXML
    private void onResetButtonClicked() throws WrongInputSizeException {
        this.polynom = new Polynom(new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0});
        initializeSpinners();
        inputWarningLabel.setVisible(false);
        symmetryLabel.setVisible(false);
        rootLabel.setVisible(false);
        this.showExtrema();
        graphicsContext.clearRect(0.0, 0.0, polynomialCanvas.getWidth(), polynomialCanvas.getHeight());
    }

    private void drawPolynomialToCanvas(Polynom polynomialToDraw, Color color) {
        graphicsContext.setStroke(color);
        double polynomialWidth = 1.0;
        graphicsContext.setLineWidth(polynomialWidth);
        //Set step size so the function value is calculated for every pixel on the canvas
        double stepSize = (polynomialCanvas.getWidth() / xScale) / polynomialCanvas.getWidth();

        //Set starting point of the first drawn line to far left side of the canvas
        double lastX = (-polynomialCanvas.getWidth() / xScale) / 2.0;
        double lastY = polynomialToDraw.functionValue(lastX);

        //Calculate the value for every pixel in a loop and stroke a line from the previous point
        for (double x = (-polynomialCanvas.getWidth() / 2.0) / xScale; x <= (polynomialCanvas.getWidth() / 2.0) / xScale; x += stepSize) {
            double y = polynomialToDraw.functionValue(x);
            graphicsContext.strokeLine(adaptXCoordinate(lastX), adaptYCoordinate(lastY), adaptXCoordinate(x), adaptYCoordinate(y));

            //Set starting point for next line as endpoint of the previous line
            lastX = x;
            lastY = y;
        }
    }

    private void drawAxis(){
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.setLineWidth(1.0);

        //Draw x-axis
        graphicsContext.strokeLine(0, polynomialCanvas.getHeight()/2, polynomialCanvas.getWidth(), polynomialCanvas.getHeight()/2);

        //Draw y-axis
        graphicsContext.strokeLine(polynomialCanvas.getWidth()/2, polynomialCanvas.getHeight(), polynomialCanvas.getWidth()/2, 0);
    }

    private double adaptXCoordinate(double mathematicalXCoordinate) {
        return mathematicalXCoordinate * xScale + polynomialCanvas.getWidth() / 2.0;
    }

    private double adaptYCoordinate(double mathematicalYCoordinate) {
        return -mathematicalYCoordinate * yScale + polynomialCanvas.getHeight() / 2.0;
    }

    private void showFunctionAsString() {
        String function = polynom.toString();
        functionAsStringLabel.setText(function);
    }

    private void showDegree() {
        int degree = polynom.getDegree();
        degreeLabel.setText(String.valueOf(degree));
    }

    private void showSymmetry() {
        String symmetry;

        if (polynom.isAxisSymmetric()) {
            symmetry = "Achsymmetrisch";
        } else if (polynom.isPointSymmetric()) {
            symmetry = "Punktsymmetrisch";
        } else {
            symmetry = "Keine Symmetrie";
        }

        symmetryLabel.setText(symmetry);
    }

    private void showRoots() {
        ArrayList<Double> roots = polynom.getRoots();

        // draw roots
        for (Double root : roots) {
            polynomialCanvas.getGraphicsContext2D().fillOval(adaptXCoordinate(root) - 2.5, adaptYCoordinate(0.0) - 2.5, 5.0, 5.0);
        }

        if (roots.size() == 0) {
            rootLabel.setText("Keine Nullstellen");
        } else {
            rootLabel.setText("Nullstellen: " + roots);
        }

    }

    private void showExtrema() {
        try {

            ArrayList<double[]> extremaArray = polynom.getExtrema();
            StringBuilder labelText = new StringBuilder();

            if (extremaArray.size() == 0) {
                labelText.append("Keine Extremstellen");
            }

            for (double[] extrema : extremaArray) {
                polynomialCanvas.getGraphicsContext2D().fillOval(adaptXCoordinate(extrema[0]) - 2.5,
                        adaptYCoordinate(extrema[1]) - 2.5, 5.0, 5.0);
                labelText.append(Arrays.toString(extrema));
            }

            extremaLabel.setText(labelText.toString());

        } catch (ArithmeticException | ComputationFailedException e) {
            System.out.println(e);
            extremaLabel.setText("Keine Extrempunkte");
        }
    }

    private void showInflectionPoints() {
        try {

            ArrayList<double[]> inflectionArray = polynom.getInflectionPoints();
            StringBuilder labelText = new StringBuilder();

            if (inflectionArray.size() == 0) {
                labelText.append("Keine Wendepunkte");
            }

            for (double[] inflection : inflectionArray) {
                polynomialCanvas.getGraphicsContext2D().fillOval(adaptXCoordinate(inflection[0]) - 2.5,
                        adaptYCoordinate(inflection[1]) - 2.5, 5.0, 5.0);
                labelText.append(Arrays.toString(inflection));
            }

            inflectionLabel.setText(labelText.toString());

        } catch (ComputationFailedException | ArithmeticException e) {
            System.out.println(e);
            inflectionLabel.setText("Keine Wendepunkte");
        }
    }

    private void showSaddlePoints() {
        try {

            ArrayList<double[]> saddleArray = polynom.getSaddlePoints();
            StringBuilder labelText = new StringBuilder();

            if (saddleArray.size() == 0) {
                saddleLabel.setText("Keine Sattelpunkte");
            }

            for (double[] saddlePoint : saddleArray) {
                polynomialCanvas.getGraphicsContext2D().fillOval(adaptXCoordinate(saddlePoint[0]) - 2.5,
                        adaptYCoordinate(saddlePoint[1]) - 2.5, 5.0, 5.0);
                labelText.append(Arrays.toString(saddlePoint));
            }

            saddleLabel.setText(labelText.toString());

        } catch (ComputationFailedException | ArithmeticException e) {
            System.out.println(e);
            saddleLabel.setText("Keine Sattelpunkte");
        }
    }
}
