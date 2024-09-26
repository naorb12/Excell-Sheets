package left;

import javafx.stage.Modality;
import center.CenterController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import left.commands.CommandsPopupController;
import left.ranges.RangesPopUpController;
import main.SharedModel;

import java.io.IOException;

public class LeftController {

    @FXML
    private Button commandsButton;
    @FXML
    private Button rangesButton;
    @FXML
    private Button sortButton;
    @FXML
    private Button filterButton;

    private CenterController centerController;  // Reference to CenterController

    Stage commandsPopUp = new Stage();

    Stage rangesPopUp = new Stage();

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
        commandsButton.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );

        rangesButton.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );
        // Additional bindings
    }

    @FXML
    private void handleCommandsButtonAction() {
        try {
            sharedModel.getAnimationController().fade(commandsButton);
            commandsPopUp = new Stage();
            // Load the commands pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controller/src/left/commands/commandsPopup.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            CommandsPopupController commandsPopupController = loader.getController();

            // Pass the reference of CenterController to the CommandsPopupController
            commandsPopupController.setCenterController(centerController);

            // Create a new Stage (window) for the pop-up
            commandsPopUp.setTitle("Commands");
            commandsPopUp.setScene(new Scene(root));
            commandsPopUp.initOwner(commandsButton.getScene().getWindow());  // Set the parent window

            // Set the stage modality to WINDOW_MODAL to lock interaction with the parent window
            commandsPopUp.initModality(Modality.WINDOW_MODAL);

            // Allow the window to be resizable and movable
            commandsPopUp.setResizable(true);
            commandsPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            commandsPopUp.setOnCloseRequest(event -> commandsPopupController.handleRemoveSortAndFilter());

            // Show the pop-up
            commandsPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRangesButtonAction() {
        try {
            sharedModel.getAnimationController().fade(rangesButton);
            rangesPopUp = new Stage();
            // Load the commands pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Controller/src/left/ranges/rangesPopup.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            RangesPopUpController rangesPopUpController = loader.getController();

            // Pass the reference of CenterController to the CommandsPopupController
            rangesPopUpController.setCenterController(centerController);

            // Create a new Stage (window) for the pop-up
            rangesPopUp.setTitle("Commands");
            rangesPopUp.setScene(new Scene(root));
            rangesPopUp.initOwner(commandsButton.getScene().getWindow());  // Set the parent window

            // Set the stage modality to WINDOW_MODAL to lock interaction with the parent window
            rangesPopUp.initModality(Modality.WINDOW_MODAL);

            // Allow the window to be resizable and movable
            rangesPopUp.setResizable(true);
            rangesPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            // Show the pop-up
            rangesPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public Stage getCommandsPopUp() {
        return commandsPopUp;
    }

    public void setDisabled() {
        commandsButton.setMouseTransparent(true);
        rangesButton.setMouseTransparent(true);
    }

    public void setEnabled() {
        commandsButton.setMouseTransparent(false);
        rangesButton.setMouseTransparent(false);
    }
}
