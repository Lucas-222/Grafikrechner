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
        stage.show();
    }

    public static void main(String[] args) throws WrongInputSizeException {
        launch();
        try {

            Polynom polynom = new Polynom(new double[]{0.0, 0.0, 3.0, -8.0, -9.0, -1.0});
            System.out.println(Arrays.toString(polynom.getInflectionPoints().get(0)));
        } catch (ComputationFailedException | ArithmeticException e) {
            System.out.println(e);
        }
    }
}
