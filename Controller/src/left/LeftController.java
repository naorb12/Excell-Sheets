package left;

import center.CenterController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import main.SharedModel;

import java.io.IOException;

public class LeftController {

    @FXML
    private Button commandsButton;
    @FXML
    private Button rangesButton;

    private CenterController centerController;  // Reference to CenterController

    private SharedModel sharedModel;

    // Method to inject the shared model into this controller
    public void setSharedModel(SharedModel sharedModel) {
        this.sharedModel = sharedModel;
    }

    public void initialize(){
        commandsButton.setOnAction(event -> handleCommandsButtonAction());
        rangesButton.setOnAction(event -> handleRangesButtonAction());
    }

    public void setupBindings() {
        // Bind buttons to the sheetLoaded property from sharedModel
        commandsButton.disableProperty().bind(sharedModel.sheetLoadedProperty().not());
        rangesButton.disableProperty().bind(sharedModel.sheetLoadedProperty().not());
        // Additional bindings
    }

    @FXML
    private void handleCommandsButtonAction() {
        try {
            // Load the commands pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Resources/commandsPopup.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            CommandsPopupController commandsPopupController = loader.getController();

            // Pass the reference of CenterController to the CommandsPopupController
            commandsPopupController.setCenterController(centerController);

            // Create a new Stage (window) for the pop-up
            Stage popupStage = new Stage();
            popupStage.setTitle("Commands");
            popupStage.setScene(new Scene(root));
            popupStage.initOwner(commandsButton.getScene().getWindow());  // Set the parent window

            // Allow the window to be resizable and movable
            popupStage.setResizable(true);

            // Show the pop-up
            popupStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRangesButtonAction() {
        System.out.println("Ranges button clicked");
    }

    // Method to set the reference to CenterController
    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;
    }

    public Button getCommandsButton() {
        return commandsButton;
    }
    public Button getRangesButton() {
        return rangesButton;
    }
}
