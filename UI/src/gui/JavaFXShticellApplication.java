package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import main.MainController;

import java.net.URL;

public class JavaFXShticellApplication extends Application {

    private static final String SPREAD_SHEET_FXML_PATH = "/Resources/borderpane.fxml";


    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(SPREAD_SHEET_FXML_PATH));
        Parent root = fxmlLoader.load();

        MainController mainController = fxmlLoader.getController();
        mainController.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Shticell");
        primaryStage.setScene(scene);
        primaryStage.getScene().getStylesheets().add(getClass().getResource("/Resources/css/Light_Theme.css").toExternalForm());
        primaryStage.show();
    }

    private FXMLLoader getPlayersFXMLLoader() {
        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url = getClass().getResource(SPREAD_SHEET_FXML_PATH);
        fxmlLoader.setLocation(url);
        return fxmlLoader;
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}
