package shticell.client.component.sheet.top;

import immutable.objects.SheetDTO;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.left.LeftController;
import shticell.client.component.sheet.main.SharedModel;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TopController {

    @FXML
    private Label selectedCellIDLabel;
    @FXML
    private TextArea cellOriginalValueTextArea;
    @FXML
    private Button updateValueButton;
    @FXML
    private Label lastUpdateCellVersionLabel;
    @FXML
    private ComboBox<String> sheetVersionSelector;


    private CenterController centerController; // For communication with the center grid

    private LeftController leftController;

    private SharedModel sharedModel = new SharedModel();

    public void initialize() {

        // Apply default style after initializing the controller (if primaryStage is set)
//        if (sharedModel.getPrimaryStage() != null && sharedModel.getPrimaryStage().getScene() != null) {
//            applyStyle(styleSelector.getValue());
//        }

        updateValueButton.setOnAction(event -> handleUpdateValueButtonAction());

        sheetVersionSelector.setValue("Select Sheet Version");
        sheetVersionSelector.setOnAction(event -> {
            try {
                handleSheetVersionSelected();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

    }


    // Method to inject the shared model into this controller
    public void setSharedModel(SharedModel sharedModel) {
        this.sharedModel = sharedModel;
    }

    public void enableCellOriginalValueTExtField() {
        cellOriginalValueTextArea.setEditable(true);
    }

    public void setupBindings() {
        // Bind buttons to the sheetLoaded property from sharedModel
        // Bind the update button to file selection
        updateValueButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> sharedModel.readOnlyProperty().get() || selectedCellIDLabel.getText().equals("Selected Cell ID"),
                        sharedModel.readOnlyProperty(), selectedCellIDLabel.textProperty()
                )
        );

    }

    @FXML
    private void handleUpdateValueButtonAction() {
        try {
            String cellID = selectedCellIDLabel.getText();
            int row = Integer.parseInt(cellID.substring(1));
            int col = cellID.charAt(0) - 'A' + 1;
            centerController.getServerEngineService().setCell(sharedModel.getSheetName(), row, col, cellOriginalValueTextArea.getText());
            populateVersionSelector();
            centerController.renderGridPane();
        }
        catch (Exception e) {
            showErrorPopup("Failed to update value", e.getMessage());
        }

    }

    @FXML
    private void handleSheetVersionSelected() throws ExecutionException, InterruptedException {
        if (sheetVersionSelector != null && sheetVersionSelector.getValue() != null) {
            String selectedValue = sheetVersionSelector.getValue();

            if (!selectedValue.equals("Select Sheet Version")) {
                if (selectedValue.equals(sheetVersionSelector.getItems().get(sheetVersionSelector.getItems().size() - 1))) {
                    centerController.renderGridPane();
                    centerController.setEnabled();
                    sharedModel.setLatestVersionSelected(true);
                } else {
                    String versionNumber = selectedValue.replaceAll("\\D+", ""); // Removes all non-digit characters
                    int version = Integer.parseInt(versionNumber);
                    centerController.renderGrid(centerController.getServerEngineService().peekVersion(sharedModel.getSheetName(), version).get());
                    resetLabelsAndText();
                    sharedModel.setLatestVersionSelected(false);
                    centerController.setDisabled();
                }
            }
        }
    }

    @FXML
    private void showErrorPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();  // Shows the alert and waits for the user to close it
    }


    // Method to set the reference to CenterController
    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;
    }

    public void setLeftController(LeftController leftController) {
        this.leftController = leftController;
    }

    @FXML
    public void populateVersionSelector() throws ExecutionException, InterruptedException {
        // Clear previous items
        sheetVersionSelector.getItems().clear();
        // Get version history from the engine's version history map
        Map<Integer, SheetDTO> versionHistory = centerController.getServerEngineService().getVersionHistory(sharedModel.getSheetName()).get();
        // Add version numbers (keys) as strings to the ComboBox
        for (Integer version : versionHistory.keySet()) {
            sheetVersionSelector.getItems().add("Version " + version);
        }

        // Optionally, select the latest version by default
        if (!sheetVersionSelector.getItems().isEmpty()) {
            sheetVersionSelector.getSelectionModel().selectLast(); // Select the latest version
        }
    }

    public void updateSelectedCell(String cellID, String originalValue, int version) {
        selectedCellIDLabel.setText(cellID);
        cellOriginalValueTextArea.setText(originalValue);
        lastUpdateCellVersionLabel.setText("Cell Version: " + String.valueOf(version));
    }

    public void setSelectedCellIDLabel(Label selectedCellIDLabel) {
        this.selectedCellIDLabel = selectedCellIDLabel;
    }

    public void resetLabelsAndText() {
        selectedCellIDLabel.setText("Selected Cell ID");
        cellOriginalValueTextArea.setText("Original Value");
        cellOriginalValueTextArea.setEditable(false);
        lastUpdateCellVersionLabel.setText("Last Update Cell Version");
    }

}

