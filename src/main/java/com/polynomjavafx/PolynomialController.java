package com.polynomjavafx;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

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
    public Canvas coordinateSystemCanvas;
    public RadioMenuItem gridToggleMenuItem;
    public RadioMenuItem axisToggleMenuItem;
    public RadioMenuItem axisScalesMenuItemToggle;
    public MenuItem returnToOriginMenuItem;
    private GraphicsContext polynomialGraphicsContext; //Canvas for displaying the polynomial
    private GraphicsContext coordinateSystemGraphicsContext; //Canvas for displaying the coordinate grid and axis
    private boolean showGrid;
    private boolean showAxis;
    private boolean showScales;
    private double xScale;
    private double yScale;
    private Polynom polynom;
    private  double majorScaleSize;
    private double xOffset;
    private double yOffset;
    private double spaceBetweenCols;
    private double spaceBetweenRows;

    @FXML
    private void initialize() {
        initializeVisuals();
        initializeSpinners();
        initializeMenuItems();
    }

    public  void initializeShortcuts() {
        KeyCombination keyCombination = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN);
        Runnable returnToOrigin = () -> this.xOffset = 0;
        this.yOffset = 0;
        this.resetCoordinateCanvas();
        this.coordinateSystemCanvas.getScene().getAccelerators().put(keyCombination, returnToOrigin);
    }

    private void initializeMenuItems() {
        //Set initial value to be selected
        axisScalesMenuItemToggle.setSelected(true);
        gridToggleMenuItem.setSelected(true);
        axisToggleMenuItem.setSelected(true);

        //Set change listeners on properties so the changes are applied
        axisScalesMenuItemToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showScales = newValue;
            //Reset canvas to apply changes
            resetCoordinateCanvas();
        });
        axisToggleMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showAxis = newValue;
            //Disable/enable axis scale toggle menuitem since showing the axis labels without axis doesn't make sense
            axisScalesMenuItemToggle.setDisable(!newValue);
            //Reset canvas to apply changes
            resetCoordinateCanvas();
        });
        gridToggleMenuItem.selectedProperty().addListener((observable, oldValue, newValue) -> {
            showGrid = newValue;
            //Reset canvas to apply changes
            resetCoordinateCanvas();
        });
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

    private void resetCoordinateCanvas(){
        coordinateSystemGraphicsContext.clearRect(0,0, coordinateSystemCanvas.getWidth(), coordinateSystemCanvas.getHeight());
        drawCoordinateSystem();
    }

    private void initializeVisuals() {
        showGrid = true;
        showScales = true;
        showAxis = true;


        //Scaling values that represent how many pixels represent one on each axis
        this.yScale = polynomialCanvas.getHeight() / 10;
        this.xScale = polynomialCanvas.getWidth() / 10;


        //Values that represent the space scrolled on the canvas. Axis are at the center if both are 0
        this.xOffset = 0;
        this.yOffset = 0;

        polynomialGraphicsContext = polynomialCanvas.getGraphicsContext2D();
        coordinateSystemGraphicsContext = coordinateSystemCanvas.getGraphicsContext2D();

        spaceBetweenCols = xScale;
        spaceBetweenRows = yScale;

        resetCoordinateCanvas();
    }

    @FXML
    private void onSubmitButtonClicked() throws WrongInputSizeException, ComputationFailedException {
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
        polynomialGraphicsContext.clearRect(0.0, 0.0, polynomialCanvas.getWidth(), polynomialCanvas.getHeight());
    }

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

    private double mathXCoordinateToCanvasXCoordinate(double mathematicalXCoordinate) {
        return (mathematicalXCoordinate * xScale + polynomialCanvas.getWidth() / 2.0) + xOffset;
    }

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
     * Draws labels onto axis according to current scaling
     */
    private void drawCoordinateSystem() {
        double originX = (coordinateSystemCanvas.getWidth() / 2) + xOffset;
        double originY = (coordinateSystemCanvas.getHeight() / 2) + yOffset;
        if(showAxis) {
            drawAxis(originX, originY);
        }
        if(showGrid) {
            drawVerticalLines();
            drawHorizontalLines();
        }
    }

    private void drawAxis(double originXCoordinate, double originYCoordinate) {
        coordinateSystemGraphicsContext.setStroke(Color.BLACK);
        coordinateSystemGraphicsContext.setLineWidth(1.0);

        //Draw x-axis
        coordinateSystemGraphicsContext.strokeLine(0, originYCoordinate, polynomialCanvas.getWidth(), originYCoordinate);

        //Draw y-axis
        coordinateSystemGraphicsContext.strokeLine(originXCoordinate, polynomialCanvas.getHeight(), originXCoordinate, 0);
    }

    private void drawHorizontalLines() {
        double majorScaleDistance = spaceBetweenRows; //The pixel amount between major scales
        //Set color and width of the lines to stroke
        double scrollingOffset = yOffset % majorScaleDistance; //The offset of the first line coordinates created by the y-offset
        double scalingOffset = coordinateSystemCanvas.getHeight() / 2 % majorScaleDistance; //The offset created by the scale distance. Without this, the axis and grid could go out of alignment
        for(double yCoordinate = scrollingOffset + scalingOffset; yCoordinate <= coordinateSystemCanvas.getHeight(); yCoordinate += majorScaleDistance) {
            double scaleLabel = (yCoordinate + yOffset) / spaceBetweenRows;
            coordinateSystemGraphicsContext.setStroke(Color.GRAY);
            coordinateSystemGraphicsContext.setLineWidth(0.5);
            coordinateSystemGraphicsContext.strokeLine(0, yCoordinate, coordinateSystemCanvas.getWidth(), yCoordinate);
            double label = canvasYCoordinateToMathYCoordinate(yCoordinate);
            label = Math.round(label);
            drawMajorYAxisScale(Double.toString(label), coordinateSystemCanvas.getWidth() / 2 + xOffset, yCoordinate);
        }
    }

    private void drawVerticalLines() {
        double majorScaleDistance = spaceBetweenCols;
        double scrollingOffSet = xOffset % majorScaleDistance; //The offset of the first line coordinates created by the x-offset
        double scalingOffset = coordinateSystemCanvas.getWidth() / 2 % majorScaleDistance; //The offset created by the scale distance. Without this, the axis and grid could go out of alignment
        for(double xCoordinate = scrollingOffSet + scalingOffset; xCoordinate <= coordinateSystemCanvas.getWidth(); xCoordinate += majorScaleDistance) {
            coordinateSystemGraphicsContext.setStroke(Color.GRAY);
            coordinateSystemGraphicsContext.setLineWidth(0.5);
            coordinateSystemGraphicsContext.strokeLine(xCoordinate, 0, xCoordinate, coordinateSystemCanvas.getHeight());
            double label = canvasXCoordinateToMathXCoordinate(xCoordinate);
            label = Math.round(label);
            drawMajorXAxisScale(Double.toString(label), xCoordinate, coordinateSystemCanvas.getHeight()/2 + yOffset);
        }
    }

    private void drawMajorXAxisScale(String scaleLabel, double x, double y) {
        double xAxisToLabelSpacing = 15;
        coordinateSystemGraphicsContext.setLineWidth(1);
        coordinateSystemGraphicsContext.setStroke(Color.BLACK);
        coordinateSystemGraphicsContext.strokeLine(x, y, x, y + majorScaleSize);
        coordinateSystemGraphicsContext.fillText(scaleLabel, x, y + xAxisToLabelSpacing);
    }

    private void drawMajorYAxisScale(String scaleLabel, double x, double y) {
        double yAxisToLabelSpacing = 0;
        coordinateSystemGraphicsContext.setLineWidth(1);
        coordinateSystemGraphicsContext.setStroke(Color.BLACK);
        coordinateSystemGraphicsContext.strokeLine(x, y, x - majorScaleSize, y);

        Text label = new Text(scaleLabel);
        label.setFont(coordinateSystemGraphicsContext.getFont());
        double labelWidth = label.getBoundsInLocal().getWidth();

        coordinateSystemGraphicsContext.fillText(scaleLabel, x - yAxisToLabelSpacing - labelWidth, y);
    }


    private void drawMinorXAxisScale(double x, double y) {
        coordinateSystemGraphicsContext.setLineWidth(0.5);
        coordinateSystemGraphicsContext.setStroke(Color.GRAY);
        if(showScales && showAxis) {
            coordinateSystemGraphicsContext.strokeLine(x, y, x, y + majorScaleSize / 2);
        }
        if(showGrid) {
            coordinateSystemGraphicsContext.setLineWidth(0.1);
            coordinateSystemGraphicsContext.strokeLine(x, coordinateSystemCanvas.getHeight(), x, 0);
        }
    }


    private void drawMinorYAxisScale(double x, double y) {
        coordinateSystemGraphicsContext.setLineWidth(0.5);
        coordinateSystemGraphicsContext.setStroke(Color.GRAY);
        if(showScales && showAxis) {
            coordinateSystemGraphicsContext.strokeLine(x, y, x - majorScaleSize / 2, y);
        }
        if(showGrid) {
            coordinateSystemGraphicsContext.setLineWidth(0.1);
            coordinateSystemGraphicsContext.strokeLine(0, y, coordinateSystemCanvas.getWidth(), y);
        }
    }

    public void onMouseScrolledOnCanvas(ScrollEvent scrollEvent) {
        if(scrollEvent.isControlDown()) {
            double deltaY = scrollEvent.getDeltaY()/10;
            xScale += deltaY;
            yScale += deltaY;
            modifySpaceBetweenRows(deltaY);
            modifySpaceBetweenCols(deltaY);
        }
        else {
        yOffset += scrollEvent.getDeltaY();
        xOffset += scrollEvent.getDeltaX();
        }

        resetCoordinateCanvas();
        drawCoordinateSystem();
        if(polynom != null) {
            polynomialGraphicsContext.clearRect(0, 0, polynomialCanvas.getWidth(), polynomialCanvas.getHeight());
            drawPolynomialToCanvas(polynom, Color.RED);
        }
    }

    private void modifySpaceBetweenRows(double delta) {
        if(coordinateSystemCanvas.getHeight() / spaceBetweenRows + delta < 5) {
            //spaceBetweenRows *= 0.5;
        }
        else if (coordinateSystemCanvas.getHeight() / spaceBetweenRows + delta > 30) {
            //spaceBetweenRows *= 2;
        }
         spaceBetweenRows += delta;
    }
    private void modifySpaceBetweenCols(double delta) {
        if(coordinateSystemCanvas.getWidth() / spaceBetweenCols + delta < 5) {
            //spaceBetweenCols *= 0.5;
        }
        else if(coordinateSystemCanvas.getWidth() / spaceBetweenCols + delta > 30){
            //paceBetweenCols *= 2;
        }
         spaceBetweenCols += delta;
    }

    double canvasXCoordinateToMathXCoordinate(double xCoordinate) {
        return ((xCoordinate)  / xScale  - coordinateSystemCanvas.getWidth() / xScale / 2 - xOffset / xScale);
    }

    double canvasYCoordinateToMathYCoordinate(double yCoordinate) {
        return -((yCoordinate /yScale) - coordinateSystemCanvas.getHeight() / xScale / 2 - yOffset / xScale);
    }

    public void returnToOrigin() {
        this.xOffset = 0;
        this.yOffset = 0;
        resetCoordinateCanvas();
        if(polynom != null) {
            drawPolynomialToCanvas(polynom, Color.RED);
        }
    }
    public void resetScaling() {
        this.xScale = coordinateSystemCanvas.getWidth() / 10;
        this.yScale = coordinateSystemCanvas.getHeight() / 10;
        spaceBetweenCols = xScale;
        spaceBetweenRows = yScale;
        resetCoordinateCanvas();
        polynomialGraphicsContext.clearRect(0,0,coordinateSystemCanvas.getWidth(), coordinateSystemCanvas.getHeight());
        if(polynom != null) {
            drawPolynomialToCanvas(polynom, Color.RED);
        }
    }
}
