package com.polynomjavafx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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

    public static void main(String[] args) {
        launch();
        try {
            Polynomial polynomial = new Polynomial(new double[]{0.0, 0.0, 2.0, 0.0, 0.0, 0.0});
            System.out.println(polynomial.getInflectionPoints());
        } catch (ComputationFailedException | ArithmeticException | WrongInputSizeException e) {
            System.out.println(e);
        }
    }
}
