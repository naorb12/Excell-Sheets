package main;

import center.CenterController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import left.LeftController;
import top.TopController;


import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private TopController topController;
    @FXML
    private LeftController leftController;
    @FXML
    private CenterController centerController;

    private Stage primaryStage;

    public void initialize() {
        try {
            // Load and set the top section
            FXMLLoader topLoader = new FXMLLoader(getClass().getResource("/Resources/topSection.fxml"));
            VBox topPane = topLoader.load();
            topController = topLoader.getController();
            mainBorderPane.setTop(topPane);

            // Load and set the left section
            FXMLLoader leftLoader = new FXMLLoader(getClass().getResource("/Resources/leftSection.fxml"));
            VBox leftPane = leftLoader.load();
            leftController = leftLoader.getController();
            mainBorderPane.setLeft(leftPane);

            // Load and set the center section
            FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/Resources/centerSection.fxml"));
            ScrollPane centerPane = centerLoader.load();
            centerController = centerLoader.getController();
            mainBorderPane.setCenter(centerPane);

            // Set references for cross-controller communication
            topController.setCenterController(centerController);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        topController.setPrimaryStage(primaryStage); // Pass primary stage to TopController
    }
}
