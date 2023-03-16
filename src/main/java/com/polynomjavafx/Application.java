package com.polynomjavafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("polynomial_view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Grafikrechner");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) throws WrongInputSizeException {
        launch();
        try {

            Polynom polynom = new Polynom(new double[]{0.0, 0.0, 2.0, 0.0, 0.0, 0.0});
            System.out.println(polynom.getInflectionPoints());
        } catch (ComputationFailedException | ArithmeticException e) {
            System.out.println(e);
        }
    }
}
