package shticell.client.component.sheet.left;

import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sheet.coordinate.Coordinate;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.left.design.DesignPopUpController;
import shticell.client.component.sheet.left.dynamic.DynamicAnalysisController;
import shticell.client.component.sheet.left.graph.GraphPopUpController;
import shticell.client.component.sheet.left.sort.and.filter.SortingAndFilteringController;
import shticell.client.component.sheet.main.SharedModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class LeftController {

    @FXML
    private Button designButton;
    @FXML
    private Button sortingFilteringButton;
    @FXML
    private Button graphsButton;
    @FXML
    private Button dynamicAnalysisButton;

    // Range
    @FXML
    private TextField rangeNameField;
    @FXML
    private TextField fromCellField;
    @FXML
    private TextField toCellField;
    @FXML
    private ComboBox<String> rangeComboBox;
    @FXML
    private TextArea rangeDetailsArea;
    @FXML
    private Button addRangeButton;
    @FXML
    private Button deleteRangeButton;
    // Stores ranges with their corresponding list of cells
    private ObservableList<String> rangeNames = FXCollections.observableArrayList();


    private CenterController centerController;  // Reference to CenterController

    private Stage designPopUp = new Stage();
    private Stage sortingFilteringPopUp = new Stage();
    private Stage graphsPopUp = new Stage();
    private Stage dynamicAnalysisPopUp = new Stage();

    private SharedModel sharedModel;

    // Method to inject the shared model into this controller
    public void setSharedModel(SharedModel sharedModel) {
        this.sharedModel = sharedModel;
    }

    public void initialize() {
        designButton.setOnAction(event -> handleDesignButtonAction());
        sortingFilteringButton.setOnAction(event -> handleSortingFilteringButtonAction());
        graphsButton.setOnAction(event -> handleGraphsButtonAction());
        dynamicAnalysisButton.setOnAction(event -> handleDynamicAnalysisButtonAction());

        // Set up the ComboBox with the list of range names
        // Assuming sharedModel has a boolean property `isFileLoadedProperty`
        rangeComboBox.setItems(rangeNames);
        addRangeButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> rangeNameField.getText().trim().isEmpty() ||
                                fromCellField.getText().trim().isEmpty() ||
                                toCellField.getText().trim().isEmpty(),
                        rangeNameField.textProperty(),
                        fromCellField.textProperty(),
                        toCellField.textProperty()
                )
        );

    }


    public void setupBindings() {
        deleteRangeButton.disableProperty().bind(
                Bindings.isNull(rangeComboBox.valueProperty())
                        .or(sharedModel.latestVersionSelectedProperty().not())
        );

        designButton.disableProperty().bind(
                sharedModel.readOnlyProperty().or(sharedModel.latestVersionSelectedProperty().not())
        );
        sortingFilteringButton.disableProperty().bind(
                (sharedModel.latestVersionSelectedProperty().not())
        );
        graphsButton.disableProperty().bind(
               (sharedModel.latestVersionSelectedProperty().not())
        );
        dynamicAnalysisButton.disableProperty().bind(
                sharedModel.readOnlyProperty().or(sharedModel.latestVersionSelectedProperty().not())
        );

        rangeComboBox.disableProperty().bind(
                sharedModel.readOnlyProperty().or(sharedModel.latestVersionSelectedProperty().not())
        );
        rangeNameField.disableProperty().bind(
                sharedModel.readOnlyProperty().or(sharedModel.latestVersionSelectedProperty().not())
        );
        fromCellField.disableProperty().bind(
                sharedModel.readOnlyProperty().or(sharedModel.latestVersionSelectedProperty().not())
        );
        toCellField.disableProperty().bind(
                sharedModel.readOnlyProperty().or(sharedModel.latestVersionSelectedProperty().not())
        );
        rangeDetailsArea.disableProperty().bind(Bindings.isNull(rangeComboBox.valueProperty()));

        sharedModel.latestVersionSelectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                System.out.println("populating selectors");
                //populateSelectors(sheetDTO);
            } else {
                rangeComboBox.getItems().clear();
                rangeComboBox.setValue("Select Range"); // Optionally clear the selected value
                rangeDetailsArea.clear();
            }
        });
        // Additional bindings
    }

    @FXML
    private void handleDesignButtonAction() {
        try {
            designPopUp = new Stage();
            // Load the design pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shticell/client/component/sheet/left/design/designPopUp.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            DesignPopUpController designPopupController = loader.getController();
            designPopupController.setSharedModel(sharedModel);
            designPopupController.setCenterController(centerController);

            // Set up the pop-up window
            designPopUp.setTitle("Design");
            designPopUp.setScene(new Scene(root));
            designPopUp.initOwner(designButton.getScene().getWindow());
            designPopUp.initModality(Modality.WINDOW_MODAL);
            designPopUp.setResizable(true);
           // designPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            designPopUp.show();
        } catch (IOException e) {
            System.err.println("Unexpected exception: " + e);
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleSortingFilteringButtonAction() {
        try {
            sortingFilteringPopUp = new Stage();
            // Load the sorting and filtering pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shticell/client/component/sheet/left/sort/and/filter/sortAndFilterPopUp.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            SortingAndFilteringController sortingFilteringPopupController = loader.getController();
            sortingFilteringPopupController.setCenterController(centerController);
            sortingFilteringPopupController.setSharedModel(sharedModel);

            // Set up the pop-up window
            sortingFilteringPopUp.setTitle("Sorting and Filtering");
            sortingFilteringPopUp.setScene(new Scene(root));
            sortingFilteringPopUp.initOwner(sortingFilteringButton.getScene().getWindow());
            sortingFilteringPopUp.initModality(Modality.WINDOW_MODAL);
            sortingFilteringPopUp.setResizable(true);
            //sortingFilteringPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            sortingFilteringPopUp.setOnCloseRequest(event -> {
                try {
                    sortingFilteringPopupController.handleRemoveSortAndFilter();
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            sortingFilteringPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGraphsButtonAction() {
        try {
            graphsPopUp = new Stage();
            // Load the design pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shticell/client/component/sheet/left/graph/graphsPopUp.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            GraphPopUpController graphPopUpController = loader.getController();
            graphPopUpController.setCenterController(centerController);
            graphPopUpController.setSharedModel(sharedModel);

            // Set up the pop-up window
            graphsPopUp.setTitle("Graphs");
            graphsPopUp.setScene(new Scene(root));
            graphsPopUp.initOwner(graphsButton.getScene().getWindow());
            graphsPopUp.initModality(Modality.WINDOW_MODAL);
            graphsPopUp.setResizable(true);
            //graphsPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            graphsPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDynamicAnalysisButtonAction() {
        try {
            dynamicAnalysisPopUp = new Stage();
            // Load the sorting and filtering pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/shticell/client/component/sheet/left/dynamic/dynamicPopUp.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            DynamicAnalysisController dynamicAnalysisController = loader.getController();
            dynamicAnalysisController.setSharedModel(sharedModel);
            dynamicAnalysisController.setCenterController(centerController);

            // Set up the pop-up window
            dynamicAnalysisPopUp.setTitle("Dynamic Analysis");
            dynamicAnalysisPopUp.setScene(new Scene(root));
            dynamicAnalysisPopUp.initOwner(dynamicAnalysisButton.getScene().getWindow());
            dynamicAnalysisPopUp.initModality(Modality.WINDOW_MODAL);
            dynamicAnalysisPopUp.setResizable(true);
            //dynamicAnalysisPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            dynamicAnalysisPopUp.setOnCloseRequest(event -> dynamicAnalysisController.handleRemoveDynamicAnalysis());
            dynamicAnalysisPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    // Method to set the reference to CenterController
    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;

    }


    public void setDisabled() {
        designButton.setMouseTransparent(true);
        sortingFilteringButton.setMouseTransparent(true);
    }

    public void setEnabled() {
        designButton.setMouseTransparent(false);
        sortingFilteringButton.setMouseTransparent(false);
    }

    // Handle adding a new range
    @FXML
    private void handleAddRange() throws ExecutionException, InterruptedException {

        if (!isOnLatestVersion()) {
            showErrorPopup("Update Failed", "You are not on the latest version. Please update to the latest version first.");
            return;
        }

        String rangeName = rangeNameField.getText();
        String fromCell = fromCellField.getText();
        String toCell = toCellField.getText();

        if (rangeName.isEmpty() || fromCell.isEmpty() || toCell.isEmpty()) {
            showErrorPopup("Error", "Please fill all fields.");
            return;
        }

        if (rangeNames.contains(rangeName)) {
            showErrorPopup("Error", "Range already exists.");
            return;
        }

        centerController.getServerEngineService().createNewRange(sharedModel.getSheetName(), rangeName, fromCell, toCell)
                .thenAccept(cellsInRange -> {
                    Platform.runLater(() -> {
                        rangeNames.add(rangeName);
                        rangeNameField.clear();
                        fromCellField.clear();
                        toCellField.clear();
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> showErrorPopup("Error Adding Range", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()));
                    return null;
                });
    }



    // Handle selecting a range from the ComboBox
    @FXML
    private void handleSelectRange() throws ExecutionException, InterruptedException {
        String selectedRange = rangeComboBox.getValue();
        if (selectedRange != null) {
            List<Coordinate> cellsInRange = centerController.getServerEngineService().getSheet(sharedModel.getSheetName()).get().getRange(selectedRange);
            rangeDetailsArea.setText(formatCoordinates(cellsInRange));  // Display formatted coordinates
        }
    }

    @FXML
    private void handleDeleteRange() throws ExecutionException, InterruptedException {

        if (!isOnLatestVersion()) {
            showErrorPopup("Update Failed", "You are not on the latest version. Please update to the latest version first.");
            return;
        }
        String selectedRange = rangeComboBox.getValue();
        if (selectedRange != null) {
            centerController.getServerEngineService()
                    .removeRange(sharedModel.getSheetName(), selectedRange)
                    .thenRun(() -> {
                        Platform.runLater(() -> {
                            rangeNames.remove(selectedRange);
                            rangeDetailsArea.clear();

                            if (!rangeNames.isEmpty()) {
                                try {
                                    handleSelectRange(); // Select the next range if available
                                } catch (ExecutionException e) {
                                    throw new RuntimeException(e);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> showErrorPopup("Error Removing Range", ex.getMessage()));
                        return null;
                    });
        }
    }


    // Populate the ComboBox with ranges from the sheet
    @FXML
    public void populateSelectors(SheetDTO sheetDTO) {
        if (centerController != null) {

            // Clear and populate the ComboBox with range names
            rangeNames.clear();
            rangeNames.addAll(sheetDTO.getAllRanges().keySet());
        }
    }

    // Helper method to format a list of coordinates as a string for display
    private String formatCoordinates(List<Coordinate> coordinates) {
        StringBuilder formatted = new StringBuilder();
        if(coordinates != null) {
            for (Coordinate coord : coordinates) {
                formatted.append(coord.toString()).append(", ");
            }
        }
        return formatted.length() > 0 ? formatted.substring(0, formatted.length() - 2) : "";  // Remove the last comma
    }

    @FXML
    private void showErrorPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();  // Shows the alert and waits for the user to close it
    }

    private boolean isOnLatestVersion() throws ExecutionException, InterruptedException {
        int latestVersion = centerController.getServerEngineService()
                .getVersionHistory(sharedModel.getSheetName())
                .get()
                .keySet()
                .stream()
                .max(Integer::compare)
                .orElse(0);
        return sharedModel.getCurrentVersionLoaded() >= latestVersion;
    }
}
