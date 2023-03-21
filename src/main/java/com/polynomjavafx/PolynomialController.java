package com.polynomjavafx;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

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
    public Canvas coordinateSystemCanvas;
    public RadioMenuItem gridToggleMenuItem;
    public RadioMenuItem axisToggleMenuItem;
    public RadioMenuItem axisScalesMenuItemToggle;
    public MenuItem returnToOriginMenuItem;
    private GraphicsContext polynomialGraphicsContext;
    private GraphicsContext coordinateSystemGraphicsContext;
    private boolean showGrid;
    private boolean showAxis;
    private boolean showScales;
    private double xScale;
    private double yScale;
    private Polynom polynom;
    private double xOffset;
    private double yOffset;
    private double rowSize;
    private double colSize;
    private final double DEFAULT_CELL_AMOUNT = 10;

    @FXML
    private void initialize() {
        initializeVisuals();
        initializeSpinners();
        initializeMenuItems();
    }

    private void initializeMenuItems() {
        //Set initial value to be selected
        axisScalesMenuItemToggle.setSelected(true);
        gridToggleMenuItem.setSelected(true);
        axisToggleMenuItem.setSelected(true);

        //Set change listeners on properties so the changes are applied
        axisScalesMenuItemToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showScales = newValue;
            //Redraw coordinate System  to apply changes
            drawCoordinateSystem();
        });
        axisToggleMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showAxis = newValue;
            //Redraw coordinate System  to apply changes;
            drawCoordinateSystem();
        });
        gridToggleMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showGrid = newValue;
            //Redraw coordinate System to apply changes
            drawCoordinateSystem();
        });
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
    }


    private void initializeVisuals() {
        //Booleans for showing / hiding parts of the coordinate system
        showGrid = true;
        showScales = true;
        showAxis = true;


        //Scaling values that represent how many pixels represent one on each axis
        this.yScale = polynomialCanvas.getHeight() / DEFAULT_CELL_AMOUNT;
        this.xScale = polynomialCanvas.getWidth() / DEFAULT_CELL_AMOUNT;


        //Values that represent the space scrolled on the canvas in pixels
        this.xOffset = 0;
        this.yOffset = 0;

        polynomialGraphicsContext = polynomialCanvas.getGraphicsContext2D();
        coordinateSystemGraphicsContext = coordinateSystemCanvas.getGraphicsContext2D();


        rowSize = yScale;
        colSize = xScale;

        drawCoordinateSystem();
    }

    @FXML
    private void onSubmitButtonClicked() {
        submitInput();
    }

    private void submitInput() {
        try {
            System.out.println(coefficient5Spinner.getValue());
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
        } catch (WrongInputSizeException inputSizeException) {
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
        polynomialGraphicsContext.clearRect(0.0, 0.0, polynomialCanvas.getWidth(), polynomialCanvas.getHeight());
    }


    /**
     * Draws Polynomial onto canvas in given color
     * @param polynomialToDraw The polynomial to be drawn
     * @param color The color of the polynomial
     */
    private void drawPolynomialToCanvas(Polynom polynomialToDraw, Color color) {
        polynomialGraphicsContext.setStroke(color);
        double polynomialWidth = 1.0;
        polynomialGraphicsContext.setLineWidth(polynomialWidth);
        //Set step size so the function value is calculated for every pixel on the canvas
        double stepSize = (polynomialCanvas.getWidth() / xScale) / polynomialCanvas.getWidth();

        //Set starting point of the first drawn line to far left side of the canvas
        double lastX = ((-polynomialCanvas.getWidth()) - xOffset / xScale) / 2.0;
        double lastY = polynomialToDraw.functionValue(lastX);

        //Calculate the value for every pixel in a loop and stroke a line from the previous point
        for (double x = ((-polynomialCanvas.getWidth() / 2.0) - xOffset)  / xScale; x <= ((polynomialCanvas.getWidth() / 2.0 - xOffset)) / xScale; x += stepSize) {
            double y = polynomialToDraw.functionValue(x);
            polynomialGraphicsContext.strokeLine(mathXCoordinateToCanvasXCoordinate(lastX), mathYCoordinateToCanvasYCoordinate(lastY), mathXCoordinateToCanvasXCoordinate(x), mathYCoordinateToCanvasYCoordinate(y));

            //Set starting point for next line as endpoint of the previous line
            lastX = x;
            lastY = y;
        }
    }

    /**
     * Translates an x-coordinate of a mathematical coordinate system to the equivalent x-coordinate on the canvas
     * @param mathematicalXCoordinate x-coordinate to be translated
     * @return translated x-coordinate
     */
    private double mathXCoordinateToCanvasXCoordinate(double mathematicalXCoordinate) {
        return (mathematicalXCoordinate * xScale + polynomialCanvas.getWidth() / 2.0) + xOffset;
    }

    /**
     * Translates a y-coordinate of a mathematical coordinate system to the equivalent y-coordinate on the canvas
     * @param mathematicalYCoordinate y-coordinate to be translated
     * @return translated y-coordinate
     */
    private double mathYCoordinateToCanvasYCoordinate(double mathematicalYCoordinate) {
        return (-mathematicalYCoordinate * yScale + polynomialCanvas.getHeight() / 2.0) + yOffset;
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
            polynomialCanvas.getGraphicsContext2D().fillOval(mathXCoordinateToCanvasXCoordinate(root) - 2.5, mathYCoordinateToCanvasYCoordinate(0.0) - 2.5, 5.0, 5.0);
        }

        if (polynom.getDegree() > 3) {
            rootLabel.setText("Grad zu hoch");
            return;
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
                polynomialCanvas.getGraphicsContext2D().fillOval(mathXCoordinateToCanvasXCoordinate(extrema[0]) - 2.5,
                        mathYCoordinateToCanvasYCoordinate(extrema[1]) - 2.5, 5.0, 5.0);
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
                polynomialCanvas.getGraphicsContext2D().fillOval(mathXCoordinateToCanvasXCoordinate(inflection[0]) - 2.5,
                        mathYCoordinateToCanvasYCoordinate(inflection[1]) - 2.5, 5.0, 5.0);
                labelText.append(Arrays.toString(inflection));
            }

            inflectionLabel.setText(labelText.toString());

        } catch (ArithmeticException e) {
            System.out.println(e);
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
                polynomialCanvas.getGraphicsContext2D().fillOval(mathXCoordinateToCanvasXCoordinate(saddlePoint[0]) - 2.5,
                        mathYCoordinateToCanvasYCoordinate(saddlePoint[1]) - 2.5, 5.0, 5.0);
                labelText.append(Arrays.toString(saddlePoint));
            }

            saddleLabel.setText(labelText.toString());

        } catch (ArithmeticException e) {
            System.out.println(e);
            saddleLabel.setText("Keine Sattelpunkte");
        } catch (ComputationFailedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Draws the coordinate system onto the canvas.
     */
    private void drawCoordinateSystem() {
        coordinateSystemGraphicsContext.clearRect(0, 0, coordinateSystemCanvas.getWidth(), coordinateSystemCanvas.getHeight());
        if(showAxis) {
            drawAxis();
        }
        drawVerticalLines();
        drawHorizontalLines();

    }

    /**
     * Draws the axis onto the coordinate system canvas
     */
    private void drawAxis() {
        double originX = (coordinateSystemCanvas.getWidth() / 2) + xOffset;
        double originY = (coordinateSystemCanvas.getHeight() / 2) + yOffset;
        coordinateSystemGraphicsContext.setStroke(Color.BLACK);
        coordinateSystemGraphicsContext.setLineWidth(1.0);

        //Draw x-axis
        coordinateSystemGraphicsContext.strokeLine(0, originY, polynomialCanvas.getWidth(), originY);

        //Draw y-axis
        coordinateSystemGraphicsContext.strokeLine(originX, polynomialCanvas.getHeight(), originX, 0);
    }

    /**
     * Draws the horizontal lines of the coordinate system
     */
    private void drawHorizontalLines() {
        double majorScaleDistance = colSize; //The pixel amount between major scales
        //Set color and width of the lines to stroke
        double scrollingOffset = yOffset % majorScaleDistance; //The offset of the first line coordinates created by the y-offset
        double scalingOffset = coordinateSystemCanvas.getHeight() / 2 % majorScaleDistance; //The offset created by the scale distance. Without this, the axis and grid could go out of alignment
        //Only start loop if labels or gird are supposed to be shown
        if(showGrid || showScales){
            for(double yCoordinate = scrollingOffset + scalingOffset; yCoordinate <= coordinateSystemCanvas.getHeight(); yCoordinate += majorScaleDistance) {
                if (showGrid) {
                    //Draw the vertical line of the grid at the curren y-coordinate
                    coordinateSystemGraphicsContext.setStroke(Color.GRAY);
                    coordinateSystemGraphicsContext.setLineWidth(0.5);
                    coordinateSystemGraphicsContext.strokeLine(0, yCoordinate, coordinateSystemCanvas.getWidth(), yCoordinate);

                    coordinateSystemGraphicsContext.setLineWidth(0.1);
                    coordinateSystemGraphicsContext.strokeLine(0, yCoordinate - majorScaleDistance/2, coordinateSystemCanvas.getWidth(), yCoordinate - majorScaleDistance/2);
                }
                if (showScales) {
                    //Draw the label for the current y-Coordinate
                    double label = canvasYCoordinateToMathYCoordinate(yCoordinate);
                    drawYAxisLabel(Double.toString(label), yCoordinate);
                }
            }
        }
    }

    /**
     * Draws the vertical lines of the coordinate system
     */
    private void drawVerticalLines() {
        double majorScaleDistance = rowSize;
        double scrollingOffSet = xOffset % majorScaleDistance; //The offset of the first line coordinates created by the x-offset
        double scalingOffset = coordinateSystemCanvas.getWidth() / 2 % majorScaleDistance; //The offset created by the scale distance. Without this, the axis and grid could go out of alignment
        if(showGrid || showScales) {
            for (double xCoordinate = scrollingOffSet + scalingOffset; xCoordinate <= coordinateSystemCanvas.getWidth(); xCoordinate += majorScaleDistance) {
                if (showGrid) {
                    coordinateSystemGraphicsContext.setStroke(Color.GRAY);
                    coordinateSystemGraphicsContext.setLineWidth(0.5);
                    coordinateSystemGraphicsContext.strokeLine(xCoordinate, 0, xCoordinate, coordinateSystemCanvas.getHeight());

                    coordinateSystemGraphicsContext.setLineWidth(0.1);
                    coordinateSystemGraphicsContext.strokeLine(xCoordinate - majorScaleDistance/2 , 0, xCoordinate - majorScaleDistance/2, coordinateSystemCanvas.getHeight());
                }
                if (showScales) {
                    double label = canvasXCoordinateToMathXCoordinate(xCoordinate);
                    if (showScales) {
                        drawXAxisLabel(Double.toString(label), xCoordinate);
                    }
                }
            }
        }
    }

    /**
     * Draws label to x-axis at given coordinate
     * @param labelText Text to be displayed
     * @param x X-coordinate of the label
     */
    private void drawXAxisLabel(String labelText, double x) {
        Text label = new Text(labelText);
        double labelHeight = label.getBoundsInLocal().getHeight();
        double y;
        coordinateSystemGraphicsContext.setLineWidth(1);
        coordinateSystemGraphicsContext.setStroke(Color.BLACK);
        if(coordinateSystemCanvas.getHeight()/2  -labelHeight > yOffset && yOffset > -coordinateSystemCanvas.getHeight()/2) {
            y = coordinateSystemCanvas.getHeight() / 2 + yOffset + labelHeight;
        }
        else if(yOffset > -coordinateSystemCanvas.getHeight()/2) {
            y = coordinateSystemCanvas.getHeight();
        }
        else y= labelHeight;
        coordinateSystemGraphicsContext.fillText(labelText, x, y);
    }

    /**
     * Draws label to y-axis at given coordinate
     * @param labelText Text to be displayed
     * @param y Y-coordinate of the label
     */
    private void drawYAxisLabel(String labelText, double y) {
        Text label = new Text(labelText);
        double labelWidth = label.getBoundsInLocal().getWidth();
        double x;
        //If the axis is visible, draw at the location of the axis
        if( coordinateSystemCanvas.getWidth()/2 > xOffset && xOffset > -coordinateSystemCanvas.getWidth()/2 + labelWidth) {
            x = coordinateSystemCanvas.getWidth() / 2 + xOffset - labelWidth; // X-coordinate of the label
        }
        //If the axis ist too far to the left, draw to the left side of the canvas
        else if((coordinateSystemCanvas.getWidth()/2 - labelWidth) > xOffset) {
            x = 0;
        }
        //If the two previous conditions are false, axis is too far to the right, draw to right side of the screen
        else {
            x = coordinateSystemCanvas.getWidth() - labelWidth;
        }
        coordinateSystemGraphicsContext.fillText(labelText, x, y);
    }


    public void onMouseScrolledOnCanvas(ScrollEvent scrollEvent) {
        if(scrollEvent.isControlDown()) {
            double delta = scrollEvent.getDeltaY()/10;
            changeScale(delta, delta);
        }
        else {
            scroll(scrollEvent.getDeltaX(),scrollEvent.getDeltaY());
        }


    }

    /**
     * Shifts view of  the Canvas up / down and left / right by amount of pixels give as params
     * @param deltaX pixels to scroll horizontally
     * @param deltaY pixels to scroll vertically
     */
    private void scroll(double deltaX, double deltaY) {
        xOffset += deltaX;
        yOffset += deltaY;
        drawCoordinateSystem();
        if(polynom != null) {
            polynomialGraphicsContext.clearRect(0, 0, polynomialCanvas.getWidth(), polynomialCanvas.getHeight());
            drawPolynomialToCanvas(polynom, Color.RED);
        }
    }

    /**
     * Increases or reduces y and x scaling with given parameters
     * @param changeX
     * @param changeY
     */
    private void changeScale(double changeX, double changeY) {
        xScale += changeX * xScale / 100;
        yScale += changeY * yScale / 100;
        updateRowSize();
        updateColSize();
        drawCoordinateSystem();
        if(polynom != null) {
            polynomialGraphicsContext.clearRect(0, 0, polynomialCanvas.getWidth(), polynomialCanvas.getHeight());
            drawPolynomialToCanvas(polynom, Color.RED);
        }
    }

    /**
     * Updates row size dependent on current scaling to avoid to small / big rows
     */
    private void updateRowSize() {
        rowSize = xScale;
        double canvasWidth = coordinateSystemCanvas.getWidth();
        while (canvasWidth / rowSize > 10) {
            rowSize *= 2;
        }
        while (canvasWidth / rowSize < 4) {
            rowSize *= 0.5;
        }
    }

    /**
     * Updates column size dependent on current scaling to avoid to small / big columns
     */
    private void updateColSize() {
        colSize = yScale;
        double canvasHeight = coordinateSystemCanvas.getHeight();
        while (canvasHeight / colSize > 10) {
            colSize *= 2;
        }
        while (canvasHeight / colSize < 4) {
            colSize *= 0.5;
        }
    }


    /**
     * Translates canvas x-coordinate to mathematical equivalent
     * @param xCoordinate x-coordinate to translate
     * @return translated coordinate
     */
    double canvasXCoordinateToMathXCoordinate(double xCoordinate) {
        double mathXCoordinate = (xCoordinate - polynomialCanvas.getWidth() / 2.0 - xOffset) / xScale;
        return Math.round(mathXCoordinate * 100) / 100.0;
    }

    /**
     * Translates canvas y-coordinate to mathematical equivalent
     * @param yCoordinate y-coordinate to translate
     * @return translated coordinate
     */
    double canvasYCoordinateToMathYCoordinate(double yCoordinate) {
        double mathYCoordinate = -(yCoordinate - polynomialCanvas.getHeight() / 2.0 - yOffset) / yScale;
        return Math.round(mathYCoordinate * 100) / 100.0;
    }


    /**
     * Returns view to origin
     */
    public void returnToOrigin() {
        this.xOffset = 0;
        this.yOffset = 0;
        drawCoordinateSystem();
        if(polynom != null) {
            polynomialGraphicsContext.clearRect(0, 0, polynomialCanvas.getWidth(), polynomialCanvas.getHeight());
            drawPolynomialToCanvas(polynom, Color.RED);
        }
    }

    /**
     * Resets scaling to default values
     */
    public void resetScaling() {
        this.xScale = coordinateSystemCanvas.getWidth() / DEFAULT_CELL_AMOUNT;
        this.yScale = coordinateSystemCanvas.getHeight() / DEFAULT_CELL_AMOUNT;
        updateColSize();
        updateRowSize();
        drawCoordinateSystem();
        polynomialGraphicsContext.clearRect(0,0,coordinateSystemCanvas.getWidth(), coordinateSystemCanvas.getHeight());
        if(polynom != null) {
            drawPolynomialToCanvas(polynom, Color.RED);
        }
    }
}
