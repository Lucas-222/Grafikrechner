package com.polynomjavafx;

import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableMap;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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
    public ToggleGroup pointSelectionTG;
    public MenuItem returnToOriginMenuItem;
    public HBox infoHbox;
    public ChoiceBox<String> scaleChoiceBox;
    private ArrayList<Polynomial> polynomialArray;
    private Polynomial selectedPolynomial;
    @FXML
    private ChoiceBox<String> polynomialsCB;
    @FXML
    private MathCanvas mathCanvas;


    @FXML
    private void initialize() {
        initializeVisuals();
        initializeMenuItems();
        initializePolynomials();
        initScaleChoiceBox();
        initIntegralTextFields();
        scaleChoiceBoxListener();
        polynomialsCBListener();
    }

    /**
     * drawPolynomials each time a polynomial is submitted/a new is picked out from the drop-down list
     */
    private void drawPolynomials() {
        mathCanvas.clearLayers();

        for (Polynomial p : polynomialArray) {
            this.mathCanvas.drawPolynomial(p);
        }

        if (selectedPolynomial != null) {
            this.drawAttributes(selectedPolynomial);
        }

    }

    private void redrawContent() {
        this.drawPolynomials();
        mathCanvas.drawPoints();
    }

    private void initializePolynomials() {
        selectedPolynomial = null;
        polynomialArray = mathCanvas.polynomialArray;
    }

    private void updateChoiceBox(Polynomial polynomial) {
        this.polynomialsCB.getItems().add(polynomial.toString());
        this.polynomialsCB.setValue(polynomial.toString());
    }

    private void resetChoiceBox() {
        this.polynomialsCB.getItems().clear();
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
                    try {
                        this.mathCanvas.setRange(start, end);
                        this.redrawContent();
                    } catch (InvalidRangeException e) {
                        System.out.println("Ungültige Eingabe");
                    }
                }
            }

        });
    }

    private void polynomialsCBListener() {
        this.polynomialsCB.valueProperty().addListener((observable, oldValue, newValue) -> {
            for (Polynomial p : polynomialArray) {
                try {
                    if (p.toString().contentEquals(newValue)) {
                        this.selectedPolynomial = p;
                        mathCanvas.integralGC.clearRect(0, 0, mathCanvas.integralLayer.getWidth(),
                                mathCanvas.integralLayer.getHeight());
                        this.drawAttributes(p);
                    }
                } catch (NullPointerException e) {
                    System.out.println();
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
        pointSelectionTG = new ToggleGroup();
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
                spinner.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER) {
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

    @SuppressWarnings("unchecked")
    public void addPolynomial() {
        try {
            // load DialogPane and its contents
            FXMLLoader loadDialog = new FXMLLoader(Objects.requireNonNull(getClass().getResource("input_dialog.fxml")));
            // define dialog along with its child elements
            Dialog<String> polyDialog = new Dialog<>();
            Parent root = loadDialog.load();
            DialogPane polyPane = polyDialog.getDialogPane();
            polyPane.setContent(root);
            Stage stage = (Stage) polyPane.getScene().getWindow();
            ObservableMap<String, Object> namespace = loadDialog.getNamespace();
            ArrayList<Spinner<Double>> spinners = new ArrayList<>(6);
            ColorPicker colorPicker = (ColorPicker) namespace.get("polyColorPicker");
            polyDialog.setTitle("Polynomial Input");
            stage.setResizable(true);
            colorPicker.setValue(Color.rgb(new Random().nextInt(256), new Random().nextInt(256),
                    new Random().nextInt(256)));

            for (int i = 5; i >= 0; i--) {
                try {
                    spinners.add((Spinner<Double>) namespace.get("coefficient" + i + "spinner"));
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }

            initializeSpinners(spinners);
            Button okButton = (Button) namespace.get("confirmButton");
            okButton.setOnAction(event -> {
                try {
                    double[] coefficients = new double[]{spinners.get(5).getValue(), spinners.get(4).getValue(),
                    spinners.get(3).getValue(), spinners.get(2).getValue(), spinners.get(1).getValue(),
                    spinners.get(0).getValue()};
                    boolean allZeroes = true;
                    for (double coefficient : coefficients) {
                        if (coefficient != 0.0) {
                            allZeroes = false;
                            break;
                        }
                    }
                    if (!allZeroes) {
                        submitInput(coefficients, colorPicker.getValue());
                    }
                } catch (WrongInputSizeException e) {
                    Label inputWarningLabel = (Label) namespace.get("inputWarningLabel");
                    HBox warningHBox = (HBox) namespace.get("warningHBox");
                    warningHBox.setManaged(true);
                    inputWarningLabel.setVisible(true);
                }
            });

            Button cancelButton = (Button) namespace.get("cancelButton");
            cancelButton.setOnAction(event -> polyPane.fireEvent(new WindowEvent(polyPane.getScene().getWindow(),
                    WindowEvent.WINDOW_CLOSE_REQUEST)));

            stage.setOnCloseRequest(event -> {
                polyDialog.setResult(null);
                polyDialog.close();
            });

            stage.sizeToScene();
            polyDialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void submitInput(double[] coefficients, Color color) throws WrongInputSizeException {
        Polynomial newPolynomial;
        newPolynomial = new Polynomial(coefficients, color);
        System.out.println(newPolynomial.getDegree());
        this.polynomialArray.add(newPolynomial);
        this.updateChoiceBox(newPolynomial);
        this.redrawContent();
    }

    /**
     * (selectively) show attributes for each polynomial in array
     */
    private void drawAttributes(Polynomial p) {
        // show symmetry and roots if degree is <= 3
        if (p.getDegree() <= 3) {
            showSymmetry(p);
            showRoots(p);
            showExtrema(p);
            showInflectionPoints(p);
            showSaddlePoints(p);
        } else {
            clearLabels();
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
        resetChoiceBox();
        initializePolynomials();
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

            if (polynomialArray.isEmpty()) {
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

        // draw roots
        for (Double root : roots) {
            mathCanvas.drawPoint(root, 0);
        }

        if (polynomial.getDegree() > 3) {
            rootLabel.setText("Grad zu hoch");
            return;
        }

        if (roots.size() == 0) {
            rootLabel.setText("Keine Nullstellen");
        } else {
            rootLabel.setText(roots.toString());
        }

    }

    private void showExtrema(Polynomial polynomial) {
        try {

            ArrayList<double[]> extremaArray = polynomial.extrema;
            StringBuilder labelText = new StringBuilder();

            if (extremaArray.size() == 0) {
                labelText.append("Keine Extremstellen");
            }

            for (double[] extrema : extremaArray) {
                mathCanvas.drawPoint(extrema[0], extrema[1]);
                labelText.append(Arrays.toString(extrema));
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
            integralLabel.setText(String.valueOf(area));
            integralLabel.setText(String.valueOf(area));
            mathCanvas.drawIntegral(Double.parseDouble(integralTextField1.getText()), Double.parseDouble(integralTextField2.getText()), selectedPolynomial);
        }
    }

    private void showInflectionPoints(Polynomial polynomial) {
        try {
            ArrayList<double[]> inflectionArray = polynomial.inflections;
            StringBuilder labelText = new StringBuilder();

            if (inflectionArray.size() == 0) {
                labelText.append("Keine Wendepunkte");
            }

            for (double[] inflection : inflectionArray) {
                mathCanvas.drawPoint(inflection[0], inflection[1]);
                labelText.append(Arrays.toString(inflection));
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
            }

            for (double[] saddlePoint : saddleArray) {
                mathCanvas.drawPoint(saddlePoint[0] , saddlePoint[1]);
                labelText.append(Arrays.toString(saddlePoint));
            }

            saddleLabel.setText(labelText.toString());

        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
            saddleLabel.setText("Keine Sattelpunkte");
        }
    }

    public void onMouseScrolledOnCanvas(ScrollEvent scrollEvent) throws WrongInputSizeException {
        if (scrollEvent.isControlDown()) {
            double delta = scrollEvent.getDeltaY() / 10;
            mathCanvas.changeScale(delta, delta);
        } else {
            mathCanvas.scroll(scrollEvent.getDeltaX(), scrollEvent.getDeltaY());
        }
        this.redrawContent();
        scaleChoiceBox.setValue("");
    }

    public void onMouseClickedOnCanvas(MouseEvent mouseEvent) {
        double mathX = mathCanvas.canvasXCoordinateToMathXCoordinate(mouseEvent.getX());
        double mathY = mathCanvas.canvasYCoordinateToMathYCoordinate(mouseEvent.getY());

        if (mathCanvas.pointsArray.size() <= 5 && mouseEvent.getClickCount() == 1) {
            if (canvasPoints.isSelected()) {
                mathCanvas.drawPointLabel(mathX, mathY);
                mathCanvas.pointsArray.add(new double[]{mathX, mathY});
            } else {
                try {
                    mathCanvas.drawPointLabel(mathX, selectedPolynomial.functionValue(mathX));
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
}
