package com.polynomjavafx;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
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
    public Label functionAsStringLabel;
    public Label degreeLabel;
    public Label rootLabel;

    private GraphicsContext graphicsContext;

    private double xScale;
    private double yScale;
    private Polynom polynom;

    @FXML
    private void initialize(){
        graphicsContext = polynomialCanvas.getGraphicsContext2D();
        this.yScale = polynomialCanvas.getHeight()/10;
        this.xScale = polynomialCanvas.getWidth()/10;
        initializeSpinners();
    }

    private void initializeSpinners(){
        coefficient0Spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-200, 200,0.0,0.01));
        coefficient1Spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-200, 200,0.0,0.01));
        coefficient2Spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-200, 200,0.0,0.01));
        coefficient3Spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-200, 200,0.0,0.01));
        coefficient4Spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-200, 200,0.0,0.01));
        coefficient5Spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-200, 200,0.0,0.01));
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
            }

            inputWarningLabel.setVisible(false);
            drawPolynomialToCanvas(polynom, Color.RED);
        }
        catch (NumberFormatException invalidInput){
            inputWarningLabel.setVisible(true);
        }

    }

    @FXML
    private void onResetButtonClicked(){
        initializeSpinners();
        inputWarningLabel.setVisible(false);
        symmetryLabel.setVisible(false);
        rootLabel.setVisible(false);
        graphicsContext.clearRect(0,0,polynomialCanvas.getWidth(), polynomialCanvas.getHeight());
    }

    private void drawPolynomialToCanvas(Polynom polynomialToDraw, Color color) {
        graphicsContext.setStroke(color);
        double polynomialWidth = 1;
        graphicsContext.setLineWidth(polynomialWidth);

        double lastX = (-polynomialCanvas.getWidth() / xScale) / 2;
        double lastY = polynomialToDraw.functionValue(lastX);

        for (double x = (-polynomialCanvas.getWidth() / 2) / xScale; x <= (polynomialCanvas.getWidth() / 2) / xScale; x += 0.1) {
            double y = polynomialToDraw.functionValue(x);
            graphicsContext.strokeLine(adaptXCoordinate(lastX), adaptYCoordinate(lastY), adaptXCoordinate(x), adaptYCoordinate(y));
            lastX = x;
            lastY = y;
        }
    }

    private double adaptXCoordinate(double mathematicalXCoordinate){
        return mathematicalXCoordinate * xScale + polynomialCanvas.getWidth() / 2;
    }

    private double adaptYCoordinate(double mathematicalYCoordinate){
        return -mathematicalYCoordinate * yScale + polynomialCanvas.getHeight() / 2;
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
            polynomialCanvas.getGraphicsContext2D().fillOval(adaptXCoordinate(root) -2.5, adaptYCoordinate(0) -2.5, 5, 5);
        }

        if (roots.size() == 0) {
            rootLabel.setText("Keine Nullstellen");
        } else {
            rootLabel.setText("Nullstellen: " + roots);
        }
    }

}
