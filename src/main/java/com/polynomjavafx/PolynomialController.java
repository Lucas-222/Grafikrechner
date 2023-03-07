package com.polynomjavafx;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;

import java.text.ParseException;

public class PolynomialController {
    public Spinner<Double> coefficient5Spinner;
    public Spinner<Double> coefficient4Spinner;
    public Spinner<Double> coefficient3Spinner;
    public Spinner<Double> coefficient2Spinner;
    public Spinner<Double> coefficient1Spinner;
    public Spinner<Double> coefficient0Spinner;
    public Canvas polynomialCanvas;
    public Label inputWarningLabel;
    private Polynom polynom;

    private GraphicsContext graphicsContext;

    @FXML
    private void initialize(){
        graphicsContext = polynomialCanvas.getGraphicsContext2D();
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
            new Polynom(coefficients);
            inputWarningLabel.setVisible(false);
        }
        catch (NumberFormatException invalidInput){
            inputWarningLabel.setVisible(true);
        }

    }
}
