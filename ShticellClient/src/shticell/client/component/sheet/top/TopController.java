package shticell.client.component.sheet.top;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.left.LeftController;
import shticell.client.component.sheet.main.SharedModel;

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
            if(!isOnLatestVersion())
            {
                showErrorPopup("Update Failed", "You are not on the latest version. Please update to the latest version first.");
                return;
            }
            String cellID = selectedCellIDLabel.getText();

            int row = Integer.parseInt(cellID.substring(1));
            int col = cellID.charAt(0) - 'A' + 1;

            centerController.getServerEngineService()
                    .setCell(sharedModel.getSheetName(), row, col, cellOriginalValueTextArea.getText(), sharedModel.getUserName())
                    .thenRun(() -> Platform.runLater(() -> {
                        try {
                            populateVersionSelector();
                            centerController.renderGridPane();
                            sharedModel.setCurrentVersionLoaded(sharedModel.getCurrentVersionLoaded() + 1);
                            //System.out.println("grid pane rendered.");
                        } catch (Exception ex) {
                            showErrorPopup("Failed to update UI", ex.getMessage());
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showErrorPopup("Failed to update value", ex.getMessage()));
                        return null;
                    });
        } catch (Exception e) {
            showErrorPopup("Failed to update value", e.getMessage());
        }
    }

    @FXML
    private void handleSheetVersionSelected() throws ExecutionException, InterruptedException {
        if (sheetVersionSelector != null && sheetVersionSelector.getValue() != null) {
            String selectedValue = sheetVersionSelector.getValue();

            if (!selectedValue.equals("Select Sheet Version")) {
                String versionNumber = selectedValue.replaceAll("\\D+", ""); // Removes all non-digit characters
                int version = Integer.parseInt(versionNumber);

                if (selectedValue.equals(sheetVersionSelector.getItems().get(sheetVersionSelector.getItems().size() - 1))) {
                    centerController.renderGridPane();
                    centerController.setEnabled();
                    centerController.getServerEngineService().getSheet(sharedModel.getSheetName()).thenAccept(sheetDTO -> {
                        // Ensure UI updates are on the JavaFX Application Thread
                        Platform.runLater(() -> {
                            leftController.populateSelectors(sheetDTO);
                        });
                    }).exceptionally(ex -> {
                        // Handle any errors that occur
                        System.out.println("Error occurred: " + ex.getMessage());
                        return null;
                    });
                    sharedModel.setLatestVersionSelected(true);
                    sheetVersionSelector.getStyleClass().removeAll("highlight-selector");
                    //sheetVersionSelector.setTooltip(null);
                } else {
                    centerController.renderGrid(centerController.getServerEngineService().peekVersion(sharedModel.getSheetName(), version).get());
                    resetLabelsAndText();
                    sharedModel.setLatestVersionSelected(false);
                    centerController.setDisabled();
                }

                sharedModel.setCurrentVersionLoaded(version);
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
    public void populateVersionSelector() {
        // Clear previous items
        sheetVersionSelector.getItems().clear();

        // Asynchronously get the version history
        centerController.getServerEngineService().getVersionHistory(sharedModel.getSheetName())
                .thenAccept(versionHistory -> {
                    Platform.runLater(() -> {
                        // Add version numbers (keys) as strings to the ComboBox
                        versionHistory.keySet().stream()
                                .sorted() // Ensure versions are sorted numerically
                                .forEach(version -> sheetVersionSelector.getItems().add("Version " + version));
//                        System.out.println("Size of versionHistory map" + versionHistory.size());
//                        System.out.println("Size of version selector: " + sheetVersionSelector.getItems().size());

                        // Optionally, select the latest version by default
                        if (!sheetVersionSelector.getItems().isEmpty()) {
                            sheetVersionSelector.getSelectionModel().selectLast(); // Select the latest version
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showErrorPopup("Error", "Failed to fetch version history: " + ex.getMessage()));
                    return null;
                });
    }


    public ComboBox<String> getSheetVersionSelector() {
        return sheetVersionSelector;
    }

    public void updateSelectedCell(String cellID, String originalValue, int version, String userNameUpdated) {
        selectedCellIDLabel.setText(cellID);
        cellOriginalValueTextArea.setText(originalValue);
        if(userNameUpdated != null) {
            lastUpdateCellVersionLabel.setText("Cell Version: " + String.valueOf(version) + " Updated by: " + userNameUpdated);
        } else {
            lastUpdateCellVersionLabel.setText("Cell Version: " + String.valueOf(version));
        }
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

    private boolean isOnLatestVersion() throws ExecutionException, InterruptedException {
        int latestVersion = getLatestVersionFromServer();
        return sharedModel.getCurrentVersionLoaded() >= latestVersion;
    }

    private int getLatestVersionFromServer() throws ExecutionException, InterruptedException {
        int latestVersion = 0;
        latestVersion = centerController.getServerEngineService()
                .getVersionHistory(sharedModel.getSheetName())
                .get()
                .keySet()
                .stream()
                .max(Integer::compare)
                .orElse(0);

        return latestVersion;
    }


    // A populate versions method, used by the Sheet Main Refresher - so it WILL NOT force transition to latest versio
    @FXML
    public void populateVersionSelectorFromRefresher() {
        // Clear previous items
        sheetVersionSelector.getItems().clear();

        // Asynchronously get the version history
        centerController.getServerEngineService().getVersionHistory(sharedModel.getSheetName())
                .thenAccept(versionHistory -> {
                    Platform.runLater(() -> {
                        // Add version numbers (keys) as strings to the ComboBox
                        versionHistory.keySet().stream()
                                .sorted() // Ensure versions are sorted numerically
                                .forEach(version -> sheetVersionSelector.getItems().add("Version " + version));

                        if (!sheetVersionSelector.getItems().isEmpty()) {
                            sheetVersionSelector.getSelectionModel().select(sharedModel.getCurrentVersionLoaded() - 1);
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showErrorPopup("Error", "Failed to fetch version history: " + ex.getMessage()));
                    return null;
                });
    }
}

