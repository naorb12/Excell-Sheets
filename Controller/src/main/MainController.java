package main;

import center.CenterController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
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

    private SharedModel sharedModel;

    public void initialize() {
        try {
            // Initialize the shared model
            sharedModel = new SharedModel();

            // Load and set the center section first (initialize centerController first)
            FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/Resources/centerSection.fxml"));
            ScrollPane centerPane = centerLoader.load();
            centerController = centerLoader.getController();  // Assign centerController here
            mainBorderPane.setCenter(centerPane);

            // Load and set the top section
            FXMLLoader topLoader = new FXMLLoader(getClass().getResource("/Resources/topSection.fxml"));
            VBox topPane = topLoader.load();
            topController = topLoader.getController();
            topController.setCenterController(centerController);  // Now set the centerController in top
            topController.setSharedModel(sharedModel);  // Make sure sharedModel is set before bindings
            topController.setupBindings();  // Call the method to setup bindings
            centerController.setTopController(topController);
            mainBorderPane.setTop(topPane);

            // Load and set the left section
            FXMLLoader leftLoader = new FXMLLoader(getClass().getResource("/Resources/leftSection.fxml"));
            VBox leftPane = leftLoader.load();
            leftController = leftLoader.getController();
            leftController.setCenterController(centerController);  // Set centerController here
            leftController.setSharedModel(sharedModel);  // Make sure sharedModel is set before bindings
            leftController.setupBindings();  // Call the method to setup bindings
            mainBorderPane.setLeft(leftPane);

            // Assuming there is a background pane that covers the whole window
            mainBorderPane.setOnMouseClicked(event -> {
                if (centerController.getSelectedCellLabel() != null) {
                    centerController.getSelectedCellLabel().setBackground(null);  // Reset background when clicking outside the grid
                    for(Label influenceing : centerController.getInfluencingOnCellLabel()){
                        influenceing.setBackground(null);
                    }
                    for(Label depends : centerController.getDependsOnCellLabel()){
                        depends.setBackground(null);
                    }
                    topController.resetLabelsAndText();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setPrimaryStage(Stage primaryStage) {
        sharedModel.setPrimaryStage(primaryStage);
    }
}
