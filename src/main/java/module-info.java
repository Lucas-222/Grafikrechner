module com.example.polynomjavafx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.polynomjavafx to javafx.fxml;
    exports com.polynomjavafx;
}
