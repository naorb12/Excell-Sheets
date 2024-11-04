package shticell.client.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import shticell.client.component.main.ShticellAppMainController;
import shticell.client.util.Constants;
import shticell.client.util.http.HttpClientUtil;

import java.io.IOException;
import java.net.URL;

public class ShticellClient extends Application {

    private ShticellAppMainController shticellAppMainController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(600);
        primaryStage.setTitle("Shticell App Client");

        URL loginPage = getClass().getResource(Constants.MAIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPage);
            Parent root = fxmlLoader.load();
            shticellAppMainController = fxmlLoader.getController();

            Scene scene = new Scene(root, 1200, 760);
            primaryStage.setScene(scene);
            primaryStage.getScene().getStylesheets().add(getClass().getResource("/shticell/client/main/Light_Theme.css").toExternalForm());
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        HttpClientUtil.shutdown();
        shticellAppMainController.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
