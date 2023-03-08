package com.polynomjavafx;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;

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
    public Label rootLabel;

    private GraphicsContext graphicsContext;

    private double xScale; //The amount of pixels on the canvas that represent one on the x-axis
    private double yScale; //The amount of pixels on the canvas that represent one on the y-axis
    private Polynom polynom;

    @FXML
    private void initialize() {
        graphicsContext = polynomialCanvas.getGraphicsContext2D();
        this.yScale = polynomialCanvas.getHeight()/10;
        this.xScale = polynomialCanvas.getWidth()/10;
        initializeSpinners();
    }

    private void initializeSpinners() {
        Spinner<Double>[] spinners = new Spinner[] {coefficient0Spinner, coefficient1Spinner, coefficient2Spinner,
                coefficient3Spinner, coefficient4Spinner, coefficient5Spinner};
        for (Spinner<Double> spinner : spinners) {
            //Set value factory
            spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-200, 200, 0.0, 0.1));
            //Add listener to textProperty so only valid input is accepted
            spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("-?[0-9]+\\.?[0-9]*")) {
                            if(newValue.equals("")){
                                spinner.getEditor().setText("0");
                            }
                            else {
                                spinner.getEditor().setText(oldValue);
                            }
                        }
                    }
            );
        }
    }

    @FXML
    private void onSubmitButtonClicked() throws WrongInputSizeException {
        submitInput();
    }
    @FXML
    private void onKeyPressed(KeyEvent keyEvent) throws WrongInputSizeException {
        if(keyEvent.getCode() == KeyCode.ENTER){
            submitInput();
        }
    }

    private void submitInput() throws WrongInputSizeException {
        double[] coefficients = {coefficient0Spinner.getValue(), coefficient1Spinner.getValue(),
                coefficient2Spinner.getValue(), coefficient3Spinner.getValue(),
                coefficient4Spinner.getValue(), coefficient5Spinner.getValue()};
            polynom = new Polynom(coefficients);

            // show information about polynomial
            showSymmetry();
            showRoots();

            inputWarningLabel.setVisible(false);
            drawPolynomialToCanvas(polynom, Color.RED);
        }

    private void drawPolynomialToCanvas(Polynom polynomialToDraw, Color color) {
        graphicsContext.setStroke(color);
        //Set width of the drawn line
        double polynomialWidth = 1;
        graphicsContext.setLineWidth(polynomialWidth);

        //Set the starting location of the line to the left side of the canvas
        double lastX = (-polynomialCanvas.getWidth() / xScale) / 2;
        double lastY = polynomialToDraw.functionValue(lastX);

        for (double x = (-polynomialCanvas.getWidth() / 2) / xScale; x <= (polynomialCanvas.getWidth() / 2) / xScale; x += 0.01) {
            double y = polynomialToDraw.functionValue(x);
            graphicsContext.strokeLine(adaptXCoordinate(lastX), adaptYCoordinate(lastY), adaptXCoordinate(x), adaptYCoordinate(y));
            //Sets the ending coordinates of the drawn line to be the starting point of the next line
            lastX = x;
            lastY = y;
        }
    }

    /**
     *Adapts mathematical x coordinate to coordinates of a canvas with the origin point at the center
     */
    private double adaptXCoordinate(double mathematicalXCoordinate) {
        return mathematicalXCoordinate * xScale + polynomialCanvas.getWidth() / 2;
    }

    /**
     *Adapts mathematical y coordinate to coordinates of a canvas with the origin point at the center
     */
    private double adaptYCoordinate(double mathematicalYCoordinate) {
        return -mathematicalYCoordinate * yScale + polynomialCanvas.getHeight() / 2;
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
            polynomialCanvas.getGraphicsContext2D().fillOval(adaptXCoordinate(root) - 5, adaptYCoordinate(0) - 5, 10, 10);
        }

        if (roots.size() == 0) {
            rootLabel.setText("Keine Nullstellen");
        } else {
            rootLabel.setText("Nullstellen: " + roots);
        }
    }

}
