module com.example.polynomjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.polynomjavafx to javafx.fxml;
    exports com.example.polynomjavafx;
}