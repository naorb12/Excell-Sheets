package left;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LeftController {

    @FXML
    private Button commandsButton;
    @FXML
    private Button rangesButton;

    public void initialize() {
        commandsButton.setOnAction(event -> handleCommandsButtonAction());
        rangesButton.setOnAction(event -> handleRangesButtonAction());
    }

    @FXML
    private void handleCommandsButtonAction() {
        System.out.println("Commands button clicked");
    }

    @FXML
    private void handleRangesButtonAction() {
        System.out.println("Ranges button clicked");
    }
}
