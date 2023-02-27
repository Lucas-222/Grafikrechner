package com.polynomjavafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) throws WrongInputSizeException {
        Polynom myPoly = new Polynom(new double[]{1.0, 2.0, 5.0, 0.0, 0.0});
        System.out.println(Arrays.toString(myPoly.getExtremaQuadratic().get(0)));

    }
}