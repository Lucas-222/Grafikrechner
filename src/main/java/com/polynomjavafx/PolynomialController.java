package com.polynomjavafx;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PolynomialController {
    public Label symmetryLabel;
    public Label degreeLabel;
    public Label rootLabel;
    public Label extremaLabel;
    public Label inflectionLabel;
    public Label saddleLabel;
    public Label integralLabel;
    public Label yInterceptLabel;
    public TextField integralTextField1;
    public TextField integralTextField2;
    public RadioMenuItem gridToggleMenuItem;
    public RadioMenuItem axisToggleMenuItem;
    public RadioMenuItem axisScalesMenuItemToggle;
    public RadioMenuItem canvasPoints;
    public RadioMenuItem polynomialPoints;
    public RadioMenuItem aboveThirdDegree;
    public MenuItem returnToOriginMenuItem;
    public HBox infoHbox;
    public ChoiceBox<String> scaleChoiceBox;
    public TextField scaleTextField1;
    public TextField scaleTextField2;
    private Polynomial selectedPolynomial;
    @FXML
    private ChoiceBox<String> polynomialsCB;
    @FXML
    private MathCanvas mathCanvas;

    private Color zeroPointColor;
    private Color extremaColor;
    private Color inflectionsPointColor;
    private Color saddlePointColor;
    private Color userPointColor;



    @FXML
    private void initialize() {
        initializeVisuals();
        initializeMenuItems();
        initScaleChoiceBox();
        initIntegralTextFields();
        initScaleTextFields();
        initColors();
        scaleChoiceBoxListener();
        polynomialsCBListener();
    }

    private void initColors() {
        this.zeroPointColor = Color.RED;
        this.inflectionsPointColor = Color.BLUE;
        this.saddlePointColor = Color.YELLOW;
        this.userPointColor = Color.PURPLE;
        this.extremaColor = Color.GREEN;
    }

    private void initScaleTextFields() {
    }

    /**
     * drawPolynomials each time a polynomial is submitted/a new is picked out from the drop-down list
     */
    private void drawPolynomials() {
        mathCanvas.clearLayers();

        for (Polynomial p : mathCanvas.polynomialArray) {
            this.mathCanvas.drawPolynomial(p);
        }

        if (selectedPolynomial != null) {
            this.drawAttributes(selectedPolynomial);
        }

    }

    private void redrawContent() {
        this.drawPolynomials();
        mathCanvas.drawPoints(userPointColor);
    }

    private void updatePolynomialChoiceBox(Polynomial polynomial) {
        this.polynomialsCB.getItems().add(polynomial.toString());
        this.polynomialsCB.setValue(polynomial.toString());
    }

    private void resetPolynomialChoiceBox() {
        this.polynomialsCB.getItems().clear();
    }

    private void polynomialsCBListener() {
        this.polynomialsCB.valueProperty().addListener((observable, oldValue, newValue) -> {
            for (Polynomial p : mathCanvas.polynomialArray) {
                try {
                    if (p.toString().contentEquals(newValue)) {
                        this.selectedPolynomial = p;
                        mathCanvas.integralGC.clearRect(0, 0, mathCanvas.integralLayer.getWidth(),
                                mathCanvas.integralLayer.getHeight());
                        mathCanvas.pointsGC.clearRect(0, 0, mathCanvas.pointsLayer.getWidth(),
                                mathCanvas.pointsLayer.getHeight());
                        this.drawAttributes(p);
                        mathCanvas.drawPoints(userPointColor);
                    }
                } catch (NullPointerException e) {
                    System.out.println();
                }
            }
        });
    }

    private void initScaleChoiceBox() {
        scaleChoiceBox.getItems().add("-5 bis 5");
        scaleChoiceBox.getItems().add("-10 bis 10");
        scaleChoiceBox.getItems().add("-50 bis 50");
        scaleChoiceBox.getItems().add("-100 bis 100");
        scaleChoiceBox.getItems().add("-500 bis 500");
        scaleChoiceBox.getItems().add("-1000 bis 1000");
    }

    private void scaleChoiceBoxListener() {
        scaleChoiceBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            Pattern pattern = Pattern.compile("-?[0-9]+([,.][0-9]+)?");
            Matcher matcher = pattern.matcher(newValue);

            if (matcher.find()) {
                double start = Double.parseDouble(matcher.group());
                if (matcher.find()) {
                    double end = Double.parseDouble(matcher.group());
                        this.scaleTextField1.setText(Double.toString(start));
                        this.scaleTextField2.setText(Double.toString(end));
                        setScale();
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


        // add menuItems to toggle group to make selection mutually exclusive
        ToggleGroup pointSelectionTG = new ToggleGroup();
        canvasPoints.setToggleGroup(pointSelectionTG);
        polynomialPoints.setToggleGroup(pointSelectionTG);
    }

    private void initIntegralTextFields() {
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            // some notes about regular expressions:
            // ^ and $ are string anchors to exclude the rest of the string.
            // [] are used to define a character set, and the ? makes it optional
            // some chars like the period have special meanings in regular expressions and must be escaped with \\
            // the + means the pattern may be repeated one or more times
            if (newValue.matches("^[+-]?[0-9]+(\\.[0-9]+)?$")) {
                mathCanvas.integralGC.clearRect(0, 0, mathCanvas.integralLayer.getWidth(),
                        mathCanvas.integralLayer.getHeight());
                try {
                    this.showIntegral(selectedPolynomial);
                } catch (WrongInputSizeException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        integralTextField1.textProperty().addListener(listener);
        integralTextField2.textProperty().addListener(listener);
    }

    private void initializeSpinners(List<Spinner<Double>> spinners) {
        StringConverter<Double> stringConverter = new StringConverter<>() {
            @Override
            public String toString(Double doubleInput) {
                if (doubleInput == 0.0) {
                    return "0.0";
                }
                return Double.toString(doubleInput);
            }

            @Override
            public Double fromString(String string) {
                if (Objects.equals(string, "")) {
                    return 0.0;
                }
                //Replace comma with point
                string = string.replace(",", ".");
                try {
                    return Double.parseDouble(string);
                } catch (NumberFormatException numberFormatException) {
                    return 0.0;
                }
            }
        };

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newString = change.getControlNewText();
            if (newString.matches("-?([0-9]+[.,]?[0-9]*)*")) {
                return change;
            } else return null;
        };

        //Loop that iterates trough spinners for less code repetition
        for (int i = 0; i < spinners.size(); i++) {
            TextFormatter<Double> textFormatter = new TextFormatter<>(stringConverter, 0.0, filter);
            Spinner<Double> spinner = spinners.get(i);
            //Set value factory
            spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(-Double.MAX_VALUE, Double.MAX_VALUE, 0.0, 0.1));

            spinner.getEditor().setTextFormatter(textFormatter);

            //Spinner is the last spinner in the list, so set eventHandler to submit input if enter is pressed
            if (i == spinners.size() - 1) {
                spinner.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        System.out.println("Ready for confirmation");
                    }
                });
            }
            //Spinner is not the last in list, set event handler to set focus on next spinner when enter is pressed
            else {
                Spinner<Double> nextSpinner = spinners.get(i + 1);
                spinner.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
                    if (keyEvent.getCode() == KeyCode.ENTER) {
                        nextSpinner.requestFocus();
                        keyEvent.consume();
                    }
                });
            }

        }
        addChangeListenerToIntegralInput(integralTextField1);
        addChangeListenerToIntegralInput(integralTextField2);
    }

    private void initializeVisuals() {
    }

    public void addPolynomial(ActionEvent event) {
        try {
            // load DialogPane and its contents
            FXMLLoader loadDialog = new FXMLLoader(Objects.requireNonNull(getClass().getResource("input_dialog.fxml")));
            // load the button that made the call
            Button callButton = (Button) event.getTarget();
            // define dialog along with its child elements
            Dialog<double[]> polyDialog = new Dialog<>();
            Parent root = loadDialog.load();
            DialogPane polyPane = polyDialog.getDialogPane();
            polyPane.setContent(root);
            ObservableMap<String, Object> namespace = loadDialog.getNamespace();
            ArrayList<Spinner<Double>> spinners = new ArrayList<>(6);
            ColorPicker colorPicker = (ColorPicker) namespace.get("polyColorPicker");


            ButtonType okButton = new ButtonType("Bestätigen", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
            polyPane.getButtonTypes().addAll(okButton, cancelButton);
            polyPane.lookupButton(okButton).setStyle("-fx-base: #f4f4f4;");

            // add spinners to array
            spinners.addAll(getSpinners(namespace));
            // initialize the spinners after they've been added to the array
            initializeSpinners(spinners);
            // initialize spinners with values if user clicked on edit
            if (callButton.getUserData().equals("edit") && selectedPolynomial != null) {
                for (int i = 0; i < spinners.size(); i++) {
                    spinners.get(spinners.size() - 1 - i).getValueFactory().setValue(selectedPolynomial.getCoefficients()[i]);
                }
                colorPicker.setValue(selectedPolynomial.polyColor);
                polyDialog.setTitle("Polynom Bearbeiten");
            } else if (callButton.getUserData().equals("edit")) {
                return;
            } else {
                colorPicker.setValue(Color.rgb(new Random().nextInt(256), new Random().nextInt(256),
                        new Random().nextInt(256)));
                polyDialog.setTitle("Polynom Erstellen");
            }

            polyDialog.setResultConverter(buttonType -> {
                if (buttonType.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    return new double[]{spinners.get(5).getValue(), spinners.get(4).getValue(),
                            spinners.get(3).getValue(), spinners.get(2).getValue(), spinners.get(1).getValue(),
                            spinners.get(0).getValue()};
                }
                return null;
            });

            polyDialog.showAndWait().ifPresent(result -> {
                try {
                    boolean allZeroes = true;
                    for (double coefficient : result) {
                        if (coefficient != 0.0) {
                            allZeroes = false;
                            break;
                        }
                    }
                    if (!allZeroes) {
                        if (!callButton.getUserData().equals("edit")) {
                            submitInput(result, colorPicker.getValue());
                        } else {
                            mathCanvas.polynomialArray.remove(selectedPolynomial);
                            polynomialsCB.getItems().remove(selectedPolynomial.toString());
                            selectedPolynomial = new Polynomial(result, colorPicker.getValue());
                            mathCanvas.polynomialArray.add(selectedPolynomial);
                            updatePolynomialChoiceBox(selectedPolynomial);
                            redrawContent();
                        }
                    }
                } catch (WrongInputSizeException e) {
                    Label inputWarningLabel = (Label) namespace.get("inputWarningLabel");
                    HBox warningHBox = (HBox) namespace.get("warningHBox");
                    warningHBox.setManaged(true);
                    inputWarningLabel.setVisible(true);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Spinner<Double>> getSpinners(ObservableMap<String, Object> namespace) {
        ArrayList<Spinner<Double>> returnArray = new ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            try {
                Spinner<Double> spinner = (Spinner<Double>) namespace.get("coefficient" + i + "spinner");
                returnArray.add(spinner);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        return returnArray;
    }

    public void deletePolynomial() {
        if (selectedPolynomial != null) {
            ArrayList<Polynomial> polyArray = mathCanvas.polynomialArray;
            polyArray.remove(selectedPolynomial);
            polynomialsCB.getItems().remove(selectedPolynomial.toString());
            if (polyArray.size() > 0) {
                selectedPolynomial = polyArray.get(polyArray.size() - 1);
            } else {
                selectedPolynomial = null;
            }
            clearLabels();
            redrawContent();
        }
    }

    private void submitInput(double[] coefficients, Color color) throws WrongInputSizeException {
        Polynomial newPolynomial;
        newPolynomial = new Polynomial(coefficients, color);
        System.out.println(newPolynomial.getDegree());
        this.mathCanvas.polynomialArray.add(newPolynomial);
        this.updatePolynomialChoiceBox(newPolynomial);
        this.redrawContent();
    }

    /**
     * (selectively) show attributes for each polynomial in array
     */
    private void drawAttributes(Polynomial p) {
        clearLabels();
        // show symmetry and roots
        if (p.getDegree() <= 3 || aboveThirdDegree.isSelected()){
            showSymmetry(p);
            showRoots(p);
            showExtrema(p);
            showInflectionPoints(p);
            showSaddlePoints(p);
        }
        // show information about polynomial
        showDegree(p);
        showYIntercept(p);
        try {
            showIntegral(p);
        } catch (WrongInputSizeException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void onResetButtonClicked() {
        mathCanvas.reset();
        clearLabels();
        resetPolynomialChoiceBox();
        this.selectedPolynomial = null;
    }

    private void clearLabels() {
        symmetryLabel.setText("");
        rootLabel.setText("");
        symmetryLabel.setText("");
        rootLabel.setText("");
        degreeLabel.setText("");
        integralLabel.setText("");
        extremaLabel.setText("");
        inflectionLabel.setText("");
        saddleLabel.setText("");
        yInterceptLabel.setText("");
    }

    private void addChangeListenerToIntegralInput(TextField integralInput) {

        ArrayList<Character> validCharacters = new ArrayList<>(List.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-'));
        integralInput.textProperty().addListener((observable, oldValue, newValue) -> {
            // If change contains illegal character reset the field to old value
            for (int i = 0; i < newValue.length(); i++) {
                if (!validCharacters.contains(newValue.charAt(i))) {
                    integralInput.setText(oldValue);
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
                    integralInput.setText(oldValue);
                    break;
                }
            }

            // Check for minus sign
            for (int i = 0; i < newValue.length(); i++) {
                // If there is more than one minus sign delete it
                if (newValue.charAt(i) == '-' && i != 0) {
                    integralInput.setText(oldValue);
                    break;
                }
            }

            if (mathCanvas.polynomialArray.isEmpty()) {
                return;
            }

            if (integralInput.getText().equals("-") || integralInput.getText().equals(".") || integralInput.getText().equals("-.")) {
                return;
            }

            if (integralTextField1.getText().equals("") || integralTextField2.getText().equals("")) {
                return;
            }

            if (integralTextField1.getText().equals(integralTextField2.getText())) {
                return;
            }

            try {
                showIntegral(selectedPolynomial);
                redrawContent();
                mathCanvas.drawIntegral(Double.parseDouble(integralTextField1.getText()), Double.parseDouble(integralTextField2.getText()), selectedPolynomial);
            } catch (WrongInputSizeException e) {
                e.printStackTrace();
            }
        });

    }
    private void showDegree(Polynomial polynomial) {
        int degree = polynomial.getDegree();
        degreeLabel.setText(String.valueOf(degree));
    }

    private void showYIntercept(Polynomial polynomial) {
        double yIntercept = polynomial.functionValue(0.0);
        yInterceptLabel.setText(String.valueOf(yIntercept));
    }

    private void showSymmetry(Polynomial polynomial) {
        String symmetry;

        if (polynomial.isAxisSymmetric()) {
            symmetry = "Achsymmetrisch";
        } else if (polynomial.isPointSymmetric()) {
            symmetry = "Punktsymmetrisch";
        } else {
            symmetry = "Keine Symmetrie";
        }
        symmetryLabel.setText(symmetry);
    }

    private void showRoots(Polynomial polynomial) {
        ArrayList<Double> roots = polynomial.getRoots();
        StringBuilder labelText = new StringBuilder();

        if (roots.size() == 0) {
            labelText.append("Keine Nullstellen");
        } else if (!aboveThirdDegree.isSelected() && polynomial.getDegree() > 3) {
            labelText.append("Grad zu hoch");
        } else {
            for (Double root : roots) {
                mathCanvas.drawPoint(root, 0.0, zeroPointColor);
                labelText.append(root).append("; ");
            }
            labelText.delete(labelText.length() - 2, labelText.length());
        }

        rootLabel.setText(labelText.toString());
    }

    private void showExtrema(Polynomial polynomial) {
        try {
            ArrayList<double[]> extremaArray = polynomial.extrema;
            StringBuilder labelText = new StringBuilder();

            if (extremaArray.size() == 0) {
                labelText.append("Keine Extremstellen");
            } else {
                for (double[] extrema : extremaArray) {
                    mathCanvas.drawPoint(extrema[0], extrema[1], extremaColor);
                    labelText.append("(")
                            .append(UtilityClasses.roundToSecondDecimalPoint(extrema[0]))
                            .append(", ")
                            .append(UtilityClasses.roundToSecondDecimalPoint(extrema[1]))
                            .append("); ");
                }
                labelText.delete(labelText.length() - 2, labelText.length());
            }
            extremaLabel.setText(labelText.toString());
        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
            extremaLabel.setText("Keine Extrempunkte");
        }
    }

    private void showIntegral(Polynomial polynomial) throws WrongInputSizeException {
        if (!integralTextField1.getText().isEmpty() && !integralTextField2.getText().isEmpty()) {
            double area = polynomial.getIntegral(Double.parseDouble(integralTextField1.getText()), Double.parseDouble(integralTextField2.getText()));
            integralLabel.setText(String.valueOf(UtilityClasses.roundToSecondDecimalPoint(area)));
            mathCanvas.drawIntegral(Double.parseDouble(integralTextField1.getText()), Double.parseDouble(integralTextField2.getText()), selectedPolynomial);
        }
    }

    private void showInflectionPoints(Polynomial polynomial) {
        try {
            ArrayList<double[]> inflectionArray = polynomial.inflections;
            StringBuilder labelText = new StringBuilder();

            if (inflectionArray.size() == 0) {
                labelText.append("Keine Wendepunkte");
            } else {
                for (double[] inflection : inflectionArray) {
                    mathCanvas.drawPoint(inflection[0], inflection[1], inflectionsPointColor);
                    labelText.append("(")
                            .append(UtilityClasses.roundToSecondDecimalPoint(inflection[0]))
                            .append(", ")
                            .append(UtilityClasses.roundToSecondDecimalPoint(inflection[1]))
                            .append("); ");
                }
                labelText.delete(labelText.length() - 2, labelText.length());
            }
            inflectionLabel.setText(labelText.toString());

        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
            inflectionLabel.setText("Keine Wendepunkte");
        }

    }

    private void showSaddlePoints(Polynomial polynomial) {
        try {
            ArrayList<double[]> saddleArray = polynomial.saddles;
            StringBuilder labelText = new StringBuilder();

            if (saddleArray.size() == 0) {
                labelText.append("Keine Sattelpunkte");
            } else {
                for (double[] saddlePoint : saddleArray) {
                    mathCanvas.drawPoint(saddlePoint[0] , saddlePoint[1], saddlePointColor);
                    labelText.append("(")
                            .append(UtilityClasses.roundToSecondDecimalPoint(saddlePoint[0]))
                            .append(", ")
                            .append(UtilityClasses.roundToSecondDecimalPoint(saddlePoint[1]))
                            .append("); ");
                }
                labelText.delete(labelText.length() - 2, labelText.length());
            }
            saddleLabel.setText(labelText.toString());
        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
            saddleLabel.setText("Keine Sattelpunkte");
        }
    }

    public void onMouseScrolledOnCanvas(ScrollEvent scrollEvent) {
        if (scrollEvent.isControlDown()) {
            double delta = scrollEvent.getDeltaY() / 10;
            mathCanvas.changeScale(delta, delta);
        } else {
            mathCanvas.scroll(scrollEvent.getDeltaX(), scrollEvent.getDeltaY());
        }
        this.redrawContent();
        scaleChoiceBox.setValue("");
        scaleTextField1.clear();
        scaleTextField2.clear();
    }

    public void onMouseClickedOnCanvas(MouseEvent mouseEvent) {
        double mathX = mathCanvas.canvasXCoordinateToMathXCoordinate(mouseEvent.getX());
        double mathY = mathCanvas.canvasYCoordinateToMathYCoordinate(mouseEvent.getY());

        if (mathCanvas.pointsArray.size() <= 5 && mouseEvent.getClickCount() == 1) {
            if (canvasPoints.isSelected()) {
                mathCanvas.drawPointLabel(mathX, mathY, userPointColor);
                mathCanvas.pointsArray.add(new double[]{mathX, mathY});
            } else {
                try {
                    mathCanvas.drawPointLabel(mathX, selectedPolynomial.functionValue(mathX), userPointColor);
                    mathCanvas.pointsArray.add(new double[]{mathX, selectedPolynomial.functionValue(mathX)});
                } catch (NullPointerException e) {
                    System.out.println("Can't plot a point because no polynomial has been selected. Please select" +
                            " a different mode from the menu or input a function to be drawn.");
                    if (mathCanvas.pointsArray.size() != 0) {
                        mathCanvas.pointsArray.remove(mathCanvas.pointsArray.size() - 1);
                    }
                }
            }
        } else if (mathCanvas.pointsArray.size() > 1 && mouseEvent.getClickCount() > 1) {
            mathCanvas.pointsArray.clear();
            mathCanvas.pointsGC.clearRect(0, 0, mathCanvas.integralLayer.getWidth(),
                    mathCanvas.integralLayer.getHeight());
            redrawPolynomialPoints();
        }
    }

    public void redrawPolynomialPoints() {
        ArrayList<double[]> extrema = selectedPolynomial.extrema;
        ArrayList<double[]> inflections = selectedPolynomial.inflections;
        ArrayList<double[]> saddles = selectedPolynomial.saddles;

        for (double[] point: extrema) {
                mathCanvas.drawPoint(point[0], point[1],extremaColor);
        }

        for (double[] point: inflections) {
            mathCanvas.drawPoint(point[0], point[1],inflectionsPointColor);
        }

        for (double[] saddlePoint : saddles) {
            mathCanvas.drawPoint(saddlePoint[0], saddlePoint[1], saddlePointColor);
        }

        for (double root: selectedPolynomial.getRoots()) {
            mathCanvas.drawPoint(root, 0.0, zeroPointColor);
        }
    }

    public void resetScaling() {
        mathCanvas.resetScaling();
        this.redrawContent();
    }

    public void returnToOrigin() {
        mathCanvas.returnToOrigin();
        this.redrawContent();
    }

    public void setScale(){
        if(scaleTextField1.getText().matches("-?[0-9]+(\\.[0-9]+)?") && scaleTextField2.getText().matches("-?[0-9]+(\\.[0-9]+)?")) {
            double rangeInput1 = Double.parseDouble(scaleTextField1.getText());
            double rangeInput2 = Double.parseDouble(scaleTextField2.getText());
            try {
                mathCanvas.setRange(Math.min(rangeInput1, rangeInput2), Math.max(rangeInput1, rangeInput2));
                scaleTextField1.setStyle("-fx-text-fill: black;");
                scaleTextField2.setStyle("-fx-text-fill: black;");
            }
            catch (InvalidRangeException e) {
                scaleTextField1.setStyle("-fx-text-fill: red;");
                scaleTextField2.setStyle("-fx-text-fill: red;");
            }
        }
    }
}
