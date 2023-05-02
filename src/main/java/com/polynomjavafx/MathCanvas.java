package com.polynomjavafx;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class MathCanvas extends StackPane {
    // Uninitialized Attributes
    Canvas contentLayer;
    Canvas coordinateSystemLayer;
    GraphicsContext contentGC;
    GraphicsContext coordinateSysGC;
    double xScale;
    double yScale;
    double xOffset;
    double yOffset;
    double cellSize;
    private double tickLineLength;
    private final double DEFAULT_CELL_AMOUNT;
    private boolean showAxis;
    private boolean showGrid;
    private boolean showScales;

    // Initialized Attributes
    ArrayList<Polynomial> polynomialArray = new ArrayList<>(10);
    ArrayList<double[]> pointsArray = new ArrayList<>();

    public MathCanvas() {
        super();

        this.contentLayer = new Canvas();
        this.coordinateSystemLayer = new Canvas();
        contentGC = contentLayer.getGraphicsContext2D();
        coordinateSysGC = coordinateSystemLayer.getGraphicsContext2D();
        this.getChildren().add(contentLayer);
        this.getChildren().add(coordinateSystemLayer);
        DEFAULT_CELL_AMOUNT = 10;

        //Booleans for showing / hiding parts of the coordinate system
        showGrid = true;
        showScales = true;
        showAxis = true;


        // Add change listeners to keep size of child elements consistent with parent
        this.widthProperty().addListener((observable, oldValue, newValue) -> {
            double oldWidth =(double)oldValue;
            double newWidth = (double) newValue;
            coordinateSystemLayer.setWidth((newWidth));
            contentLayer.setWidth(newWidth);

            //If xScale is 0 (only during initialization) use default cell amount, otherwise get current cell amount from dividing old width by cell size
            double cellAmount = xScale != 0 ? oldWidth / cellSize : newWidth / DEFAULT_CELL_AMOUNT;

            //Calculate new xScale by dividing width by cell amount
            xScale = newWidth / cellAmount;

            this.yScale = xScale;
            cellSize = xScale;
            updateCellSize();
            drawCoordinateSystem();
        });

        this.heightProperty().addListener(((observable, oldValue, newValue )-> {
            double newHeight = (double) newValue;

            coordinateSystemLayer.setHeight(newHeight);
            contentLayer.setHeight(newHeight);

            tickLineLength = this.getHeight()/100;


            updateCellSize();
            drawCoordinateSystem();
        } ));

        // values that represent the space scrolled on the canvas in pixels
        this.xOffset = 0;
        this.yOffset = 0;

    }

    // Show Methods

    /**
     * Set whether the axis should be shown
     * @param showAxis value to set to
     */
    public void setShowAxis(boolean showAxis) {
        this.showAxis = showAxis;
        drawCoordinateSystem();
    }

    /**
     * Set whether the grid of the coordinateSystem should be shown
     * @param showGrid value to set to
     */
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        drawCoordinateSystem();
    }

    /**
     * Set whether the Labels that mark scales on the coordinate system should be shown
     * @param showScales value to set to
     */
    public void setShowScales(boolean showScales) {
        this.showScales = showScales;
        drawCoordinateSystem();
    }

    // Draw Methods (alphanumeric ascending)

    /**
     * Draws the axis onto the coordinate system canvas
     */
    private void drawAxis() {
        double yAxisPosition = 0;
        double yAxisCanvasPos = mathXCoordinateToCanvasXCoordinate(yAxisPosition);
        double xAxisPosition = 0;
        double xAxisCanvasPos = mathYCoordinateToCanvasYCoordinate(xAxisPosition);
        coordinateSysGC.setStroke(Color.BLACK);
        coordinateSysGC.setLineWidth(1.0);

        //Draw x-axis
        coordinateSysGC.strokeLine(0, xAxisCanvasPos, contentLayer.getWidth(), xAxisCanvasPos);

        //Draw y-axis
        coordinateSysGC.strokeLine(yAxisCanvasPos, contentLayer.getHeight(), yAxisCanvasPos, 0);
    }

    /**
     * Draws the coordinate system onto the canvas.
     */
    private void drawCoordinateSystem() {
        coordinateSysGC.clearRect(0, 0, contentLayer.getWidth(), coordinateSystemLayer.getHeight());
        if(showAxis) {
            drawAxis();
        }
        drawVerticalLines();
        drawHorizontalLines();
    }

    /**
     * Draws the horizontal lines of the coordinate system
     */
    private void drawHorizontalLines() {
        double majorScaleDistance = cellSize; //The pixel amount between major scales
        //Set color and width of the lines to stroke
        double scrollingOffset = yOffset % majorScaleDistance; //The offset of the first line coordinates created by the y-offset
        double scalingOffset = coordinateSystemLayer.getHeight() / 2 % majorScaleDistance; //The offset created by the scale distance. Without this, the axis and grid could go out of alignment
        //Only start loop if labels or gird are supposed to be shown
        if(showGrid || showScales){
            for(double yCoordinate = scrollingOffset + scalingOffset; yCoordinate <= coordinateSystemLayer.getHeight(); yCoordinate += majorScaleDistance) {
                if (showGrid) {
                    //Draw the vertical line of the grid at the curren y-coordinate
                    coordinateSysGC.setStroke(Color.GRAY);
                    coordinateSysGC.setLineWidth(0.5);
                    coordinateSysGC.strokeLine(0, yCoordinate, coordinateSystemLayer.getWidth(), yCoordinate);
                    for(double i = yCoordinate + majorScaleDistance/10; i < yCoordinate + majorScaleDistance; i+=majorScaleDistance/10) {
                        coordinateSysGC.setLineWidth(0.1);
                        coordinateSysGC.strokeLine(0, i, coordinateSystemLayer.getWidth(), i);
                    }
                }
                if (showScales) {
                    //Draw the label for the current y-Coordinate
                    double label = canvasYCoordinateToMathYCoordinate(yCoordinate);
                    drawYAxisLabel(Double.toString(label), yCoordinate);
                }
            }
        }
    }

    public void drawIntegral(double x1, double x2, Polynomial polynomial) {
        contentGC.setStroke(Color.BLUE);
        double stepSize = (contentLayer.getWidth() / xScale) / contentLayer.getWidth();
        for (double start = Math.min(x1, x2); start < Math.max(x1, x2); start += stepSize) {
            contentGC.strokeLine(mathXCoordinateToCanvasXCoordinate(start),
                    mathYCoordinateToCanvasYCoordinate(0.0),
                    mathXCoordinateToCanvasXCoordinate(start),
                    mathYCoordinateToCanvasYCoordinate(polynomial.functionValue(start)));
        }
    }

    public void drawPoint(double x, double y) {
        contentLayer.getGraphicsContext2D().fillOval(mathXCoordinateToCanvasXCoordinate(x) - 2.5,
                mathYCoordinateToCanvasYCoordinate(y) - 2.5, 5.0, 5.0);
    }

    public void drawPointLabel(double x, double y) {
        double xRounded = Math.round(x * 100.0) / 100.0;
        double yRounded = Math.round(y * 100.0) / 100.0;
        contentLayer.getGraphicsContext2D().fillOval(mathXCoordinateToCanvasXCoordinate(x) - 2.5,
                mathYCoordinateToCanvasYCoordinate(y) - 2.5, 5.0, 5.0);
        contentLayer.getGraphicsContext2D().fillText("(" + xRounded + ", " + yRounded + ")",
                mathXCoordinateToCanvasXCoordinate(x) + 5.0, mathYCoordinateToCanvasYCoordinate(y) - 2.5);
    }

    /**
     * draw points retrieved from pointsArray attribute
     */
    public void drawPoints() {
        for (double[] point : pointsArray) {
            this.drawPointLabel(point[0], point[1]);
        }
    }

    public void drawPolynomial(Polynomial polynomialToDraw) {
        contentGC.setStroke(polynomialToDraw.polyColor);

        double polynomialWidth = 1.0;
        contentGC.setLineWidth(polynomialWidth);
        //Set step size so the function value is calculated for every pixel on the canvas
        double stepSize = (contentLayer.getWidth() / xScale) / contentLayer.getWidth();

        //Set starting point of the first drawn line to far left side of the canvas
        double lastX = ((-contentLayer.getWidth()) - xOffset / xScale) / 2.0;
        double lastY = polynomialToDraw.functionValue(lastX);

        //Calculate the value for every pixel in a loop and stroke a line from the previous point
        for (double x = ((-contentLayer.getWidth() / 2.0) - xOffset)  / xScale; x <= ((contentLayer.getWidth() / 2.0 - xOffset)) / xScale; x += stepSize) {
            double y = polynomialToDraw.functionValue(x);
            contentGC.strokeLine(mathXCoordinateToCanvasXCoordinate(lastX),
                    mathYCoordinateToCanvasYCoordinate(lastY),
                    mathXCoordinateToCanvasXCoordinate(x),
                    mathYCoordinateToCanvasYCoordinate(y));

            //Set starting point for next line as endpoint of the previous line
            lastX = x;
            lastY = y;
        }
    }

    /**
     * Draws the vertical lines of the coordinate system
     */
    private void drawVerticalLines() {
        double majorScaleDistance = cellSize;
        double scrollingOffSet = xOffset % majorScaleDistance; //The offset of the first line coordinates created by the x-offset
        double scalingOffset = coordinateSystemLayer.getWidth() / 2 % majorScaleDistance; //The offset created by the scale distance. Without this, the axis and grid could go out of alignment
        if(showGrid || showScales) {
            for (double xCoordinate = scrollingOffSet + scalingOffset; xCoordinate <= coordinateSystemLayer.getWidth(); xCoordinate += majorScaleDistance) {
                if (showGrid) {
                    coordinateSysGC.setStroke(Color.GRAY);
                    coordinateSysGC.setLineWidth(0.5);
                    coordinateSysGC.strokeLine(xCoordinate, 0, xCoordinate, coordinateSystemLayer.getHeight());


                    //Draw 10 small lines between this line and the next
                    for(double i = xCoordinate + majorScaleDistance/10; i < xCoordinate + majorScaleDistance; i+=majorScaleDistance/10) {
                        coordinateSysGC.setLineWidth(0.1);
                        coordinateSysGC.strokeLine(i , 0, i, coordinateSystemLayer.getHeight());
                    }
                }
                if (showScales) {
                    double label = canvasXCoordinateToMathXCoordinate(xCoordinate);
                    drawXAxisLabel(Double.toString(label), xCoordinate);
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
        coordinateSysGC.setLineWidth(1);
        coordinateSysGC.setStroke(Color.BLACK);

        //If the axis is visible, draw at the location of the axis
        if(coordinateSystemLayer.getHeight()/2  -labelHeight > yOffset && yOffset > -coordinateSystemLayer.getHeight()/2) {
            y = coordinateSystemLayer.getHeight() / 2 + yOffset + labelHeight + tickLineLength;

            //Stroke a small line from the axis
            coordinateSysGC.setStroke(Color.BLACK);
            coordinateSysGC.setLineWidth(1);
            coordinateSysGC.strokeLine(x, coordinateSystemLayer.getHeight() / 2 + yOffset, x, coordinateSystemLayer.getHeight() / 2 + yOffset + tickLineLength);
        }
        //If the axis ist too far below, draw to the bottom of the canvas
        else if(yOffset > -coordinateSystemLayer.getHeight()/2) {
            y = coordinateSystemLayer.getHeight();
        }
        //If the two previous conditions are false, axis is too far up, draw to the top of the canvas
        else y= labelHeight;
        coordinateSysGC.fillText(labelText, x, y);
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
        if( coordinateSystemLayer.getWidth()/2 > xOffset && xOffset > -coordinateSystemLayer.getWidth()/2 + labelWidth) {
            x = coordinateSystemLayer.getWidth() / 2 + xOffset - labelWidth - tickLineLength; // X-coordinate of the label

            //Stroke a small line from the axis
            coordinateSysGC.setStroke(Color.BLACK);
            coordinateSysGC.setLineWidth(1);
            coordinateSysGC.strokeLine(coordinateSystemLayer.getWidth() / 2 + xOffset, y, coordinateSystemLayer.getWidth() / 2 + xOffset - tickLineLength, y);
        }
        //If the axis ist too far to the left, draw to the left side of the canvas
        else if((coordinateSystemLayer.getWidth()/2 - labelWidth) > xOffset) {
            x = 0;
        }
        //If the two previous conditions are false, axis is too far to the right, draw to right side of the screen
        else {
            x = coordinateSystemLayer.getWidth() - labelWidth;
        }
        coordinateSysGC.fillText(labelText, x, y);
    }

    // Update Methods/Change Methods

    /**
     * Shifts view of  the Canvas up / down and left / right by amount of pixels give as params
     * @param deltaX pixels to scroll horizontally
     * @param deltaY pixels to scroll vertically
     */
    public void scroll(double deltaX, double deltaY) {
        xOffset += deltaX;
        yOffset += deltaY;
        drawCoordinateSystem();
    }

    public void setRange(double start, double end) throws InvalidRangeException {
        if (start >= end) {
            throw new InvalidRangeException(start, end);
        }

        double range = Math.abs(end - start);
        //Set scale to be able to display range
        this.xScale = this.getWidth()/range;
        this.yScale = xScale;

        double offset = Math.abs(start) - Math.abs(end);
        xOffset = offset * xScale;

        this.yOffset = 0;
        updateCellSize();
        drawCoordinateSystem();
    }

    /**
     * Increases or reduces y and x scaling with given parameters
     * @param changeX x-scale to add/subtract
     * @param changeY y-scale to add/subtract
     */
    public void changeScale(double changeX, double changeY) {
        xScale += changeX * xScale / 100;
        yScale += changeY * yScale / 100;
        updateCellSize();
        drawCoordinateSystem();
    }


    /**
     * Updates cell size dependent on current scaling to avoid to small / big columns
     */
    private void updateCellSize() {
        cellSize = xScale;
        double canvasWidth = coordinateSystemLayer.getWidth();
        while (canvasWidth / cellSize > 5) {
            cellSize *= 10;
        }
        while (canvasWidth / cellSize < 2) {
            cellSize /= 10;
        }
    }

    // Translation Methods/Adaption Methods

    /**
     * Translates canvas x-coordinate to mathematical equivalent
     * @param xCoordinate x-coordinate to translate
     * @return translated coordinate
     */
    public double canvasXCoordinateToMathXCoordinate(double xCoordinate) {
        double mathXCoordinate = (xCoordinate - contentLayer.getWidth() / 2.0 - xOffset) / xScale;
        return Math.round(mathXCoordinate * 100.0) / 100.0;
    }

    /**
     * Translates canvas y-coordinate to mathematical equivalent
     * @param yCoordinate y-coordinate to translate
     * @return translated coordinate
     */
    public double canvasYCoordinateToMathYCoordinate(double yCoordinate) {
        double mathYCoordinate = -(yCoordinate - contentLayer.getHeight() / 2.0 - yOffset) / yScale;
        return Math.round(mathYCoordinate * 100.0) / 100.0;
    }


    /**
     * Translates an x-coordinate of a mathematical coordinate system to the equivalent x-coordinate on the canvas
     * @param mathematicalXCoordinate x-coordinate to be translated
     * @return translated x-coordinate
     */
    private double mathXCoordinateToCanvasXCoordinate(double mathematicalXCoordinate) {
        return (mathematicalXCoordinate * xScale + contentLayer.getWidth() / 2.0) + xOffset;
    }

    /**
     * Translates a y-coordinate of a mathematical coordinate system to the equivalent y-coordinate on the canvas
     * @param mathematicalYCoordinate y-coordinate to be translated
     * @return translated y-coordinate
     */
    private double mathYCoordinateToCanvasYCoordinate(double mathematicalYCoordinate) {
        return (-mathematicalYCoordinate * yScale + contentLayer.getHeight() / 2.0) + yOffset;
    }

    // Reset Methods/Clear Methods

    /**
     * Clears the content displayed on the canvas
     */
    public void reset() {
        clearContentLayer();
        this.pointsArray.clear();
        this.polynomialArray.clear();
    }

    /**
     * Resets scaling to default values
     */
    public void resetScaling() {
        this.xScale = coordinateSystemLayer.getWidth() / DEFAULT_CELL_AMOUNT;
        this.yScale = xScale;
        updateCellSize();
        drawCoordinateSystem();
    }

    /**
     * Returns view to origin
     */
    public void returnToOrigin() {
        this.xOffset = 0;
        this.yOffset = 0;
        drawCoordinateSystem();
    }

    public void clearContentLayer() {
        this.contentGC.clearRect(0, 0, contentLayer.getWidth(), contentLayer.getHeight());
    }
}
