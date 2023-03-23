package com.polynomjavafx;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolynomialController {
    public Spinner<Double> coefficient5Spinner;
    public Spinner<Double> coefficient4Spinner;
    public Spinner<Double> coefficient3Spinner;
    public Spinner<Double> coefficient2Spinner;
    public Spinner<Double> coefficient1Spinner;
    public Spinner<Double> coefficient0Spinner;
    public Label inputWarningLabel;
    public Label symmetryLabel;
    public Label functionAsStringLabel;
    public Label degreeLabel;
    public Label rootLabel;
    public Label extremaLabel;
    public Label inflectionLabel;
    public Label saddleLabel;
    public Label integralLabel;
    public TextField integralTextField1;
    public TextField integralTextField2;
    public RadioMenuItem gridToggleMenuItem;
    public RadioMenuItem axisToggleMenuItem;
    public RadioMenuItem axisScalesMenuItemToggle;
    public MenuItem returnToOriginMenuItem;
    public HBox infoHbox;
    public ChoiceBox<String> scaleChoicebox;
    private Polynom polynom;
    @FXML
    private MathCanvas mathCanvas;

    @FXML
    private void initialize() {
        initializeVisuals();
        initializeSpinners();
        initializeMenuItems();
        scaleChoicebox.getItems().add("-5 bis 5");
        scaleChoicebox.getItems().add("-10 bis 10");
        scaleChoicebox.getItems().add("-50 bis 50");
        scaleChoicebox.getItems().add("-100 bis 100");
        scaleChoicebox.getItems().add("-500 bis 500");
        scaleChoicebox.getItems().add("-1000 bis 1000");

        scaleChoicebox.valueProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("-?[0-9]+([,.][0-9]+)?");
            Matcher matcher = pattern.matcher(newValue);

            if (matcher.find()) {
                double start = Double.parseDouble(matcher.group());
                if (matcher.find()) {
                    double end = Double.parseDouble(matcher.group());
                    try {
                        this.mathCanvas.setRange(start, end);
                    } catch (InvalidRangeException e) {
                        System.out.println("Ungültige Eingabe");
                    }
                }
            }

        });
    }

    private void initializeMenuItems() {
        //Set initial value to be selected
        axisScalesMenuItemToggle.setSelected(true);
        gridToggleMenuItem.setSelected(true);
        axisToggleMenuItem.setSelected(true);

        //Set change listeners on properties so the changes are applied
        axisScalesMenuItemToggle.selectedProperty().addListener((observable, oldValue, newValue) -> mathCanvas.setShowScales(newValue));
        axisToggleMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> mathCanvas.setShowAxis(newValue));
        gridToggleMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> mathCanvas.setShowGrid(newValue));
    }

    private void initializeSpinners() {
        //Array of every spinner
        List<Spinner<Double>> spinners = Arrays.asList(coefficient5Spinner, coefficient4Spinner, coefficient3Spinner, coefficient2Spinner, coefficient1Spinner,
                coefficient0Spinner);
        StringConverter<Double> stringConverter= new StringConverter<>() {
            @Override
            public String toString(Double doubleInput) {
                if(doubleInput == 0.0) {
                    return "0.0";
                }
                return Double.toString(doubleInput);
            }

            @Override
            public Double fromString(String string) {
                if(Objects.equals(string, "")) {
                    return 0.0;
                }
                //Replace comma with point
                string  = string.replace(",", ".");
                try {
                    return Double.parseDouble(string);
                }
                catch (NumberFormatException numberFormatException) {
                    return 0.0;
                }
            }
        };
        UnaryOperator<TextFormatter.Change> filter  = change -> {
            String newString = change.getControlNewText();
            if(newString.matches("-?([0-9]+[.,]?[0-9]*)*")) {
                return change;
            }
            else return null;
        };

        //Loop that iterates trough spinners for less code repetition
        for (int i = 0; i < spinners.size(); i++) {
            TextFormatter<Double> textFormatter = new TextFormatter<>(stringConverter, 0.0 , filter);
            Spinner<Double> spinner = spinners.get(i);
            //Set value factory
            spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 0.1));

            spinner.getEditor().setTextFormatter(textFormatter);

            //Spinner is the last spinner in the list, so set eventHandler to submit input if enter is pressed
            if(i == spinners.size()-1) {
                spinner.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                    if(keyEvent.getCode() == KeyCode.ENTER) {
                        submitInput();
                    }
                });
            }
            //Spinner is not the last in list, set event handler to set focus on next spinner when enter is pressed
            else {
                Spinner<Double> nextSpinner = spinners.get(i + 1);
                spinner.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if(event.getCode() == KeyCode.ENTER) {
                        nextSpinner.requestFocus();
                    }
                });
            }

        }

        addChangeListenerToIntegralInput(integralTextField1);
        addChangeListenerToIntegralInput(integralTextField2);

    }

    private void initializeVisuals() {

    }

    @FXML
    private void onSubmitButtonClicked() {
        submitInput();
    }

    private void submitInput() {
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
            mathCanvas.drawPolynomial(polynom, Color.RED);
        } catch (WrongInputSizeException inputSizeException) {
            inputWarningLabel.setVisible(true);
        }
    }

    @FXML
    private void onResetButtonClicked() {
        this.polynom = null;
        initializeSpinners();
        mathCanvas.reset();
        inputWarningLabel.setVisible(false);
        symmetryLabel.setVisible(false);
        rootLabel.setVisible(false);
        this.showExtrema();
        inputWarningLabel.setText("");
        symmetryLabel.setText("");
        rootLabel.setText("");
        degreeLabel.setText("");
        integralLabel.setText("");
        extremaLabel.setText("");
        inflectionLabel.setText("");
        saddleLabel.setText("");
        functionAsStringLabel.setText("");
    }

    private void addChangeListenerToIntegralInput(TextField integralInput) {
        ArrayList<Character> validCharacters = new ArrayList<>(List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-'));
        integralInput.textProperty().addListener((observable, oldValue, newValue) -> {
            // If change contains illegal character reset the field to old value
            for (int i = 0; i < newValue.length(); i++) {
                if (!validCharacters.contains(newValue.charAt(i))) {
                    integralLabel.setText(oldValue);
                    break;
                }
            }

            // check for multiple decimal points
            int count = 0;
            for (int i = 0; i < newValue.length(); i++) {
                // Count the numbers of decimal points
                if (newValue.charAt(i) == '.') count++;

                // If there is more than one decimal point delete it
                if (count > 1) {
                    integralLabel.setText(oldValue);
                    break;
                }
            }

            // Check for minus sign
            for (int i = 0; i < newValue.length(); i++) {
                // If there is more than one minus sign delete it
                if (newValue.charAt(i) == '-' && i != 0) {
                    integralLabel.setText(oldValue);
                    break;
                }
            }

            if (polynom == null) {
                return;
            }

            if (integralTextField1.getText().equals("") || integralTextField2.getText().equals("")) {
                return;
            }

            if (integralTextField1.getText().equals(integralTextField2.getText())) {
                return;
            }

            try {
                showIntegral();
            } catch (WrongInputSizeException e) {
                e.printStackTrace();
            }
        });

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
            mathCanvas.highlightPoint(root, 0);
        }

        if (polynom.getDegree() > 3) {
            rootLabel.setText("Grad zu hoch");
            return;
        }

        if (roots.size() == 0) {
            rootLabel.setText("Keine Nullstellen");
        } else {
            rootLabel.setText(roots.toString());
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
                mathCanvas.highlightPoint(extrema[0], extrema[1]);
                labelText.append(Arrays.toString(extrema));
            }

            extremaLabel.setText(labelText.toString());

        } catch (ArithmeticException | ComputationFailedException e) {
            System.out.println(e.getMessage());
            extremaLabel.setText("Keine Extrempunkte");
        }
    }

    private void showIntegral() throws WrongInputSizeException {
        double area = polynom.getIntegral(Double.parseDouble(integralTextField1.getText()), Double.parseDouble(integralTextField2.getText()));
        System.out.println(area);
        integralLabel.setText(String.valueOf(area));
        mathCanvas.drawIntegral(Double.parseDouble(integralTextField1.getText()), Double.parseDouble(integralTextField2.getText()));
    }


    private void showInflectionPoints() {
        try {

            ArrayList<double[]> inflectionArray = polynom.getInflectionPoints();
            StringBuilder labelText = new StringBuilder();

            if (inflectionArray.size() == 0) {
                labelText.append("Keine Wendepunkte");
            }

            for (double[] inflection : inflectionArray) {
                mathCanvas.highlightPoint(inflection[0], inflection[1]);
                labelText.append(Arrays.toString(inflection));
            }

            inflectionLabel.setText(labelText.toString());

        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
            inflectionLabel.setText("Keine Wendepunkte");
        } catch (ComputationFailedException e) {
            throw new RuntimeException(e);
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
                mathCanvas.highlightPoint(saddlePoint[0] , saddlePoint[1]);
                labelText.append(Arrays.toString(saddlePoint));
            }

            saddleLabel.setText(labelText.toString());

        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
            saddleLabel.setText("Keine Sattelpunkte");
        } catch (ComputationFailedException e) {
            throw new RuntimeException(e);
        }
    }


    public void onMouseScrolledOnCanvas(ScrollEvent scrollEvent) {
        if(scrollEvent.isControlDown()) {
            double delta = scrollEvent.getDeltaY()/10;
            mathCanvas.changeScale(delta, delta);
        }
        else {
            mathCanvas.scroll(scrollEvent.getDeltaX(),scrollEvent.getDeltaY());
        }
        scaleChoicebox.setValue("");
    }

    public void resetScaling() {
        mathCanvas.resetScaling();
    }

    public void returnToOrigin() {
        mathCanvas.returnToOrigin();
    }
}
