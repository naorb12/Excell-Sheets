package left;

import exception.InvalidXMLFormatException;
import exception.OutOfBoundsException;
import immutable.objects.SheetDTO;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.Modality;
import center.CenterController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import left.design.DesignPopUpController;
import left.dynamic.DynamicAnalysisController;
import left.graph.GraphPopUpController;
import left.sort.and.filter.SortingAndFilteringController;
import main.SharedModel;
import sheet.coordinate.Coordinate;

import java.io.IOException;
import java.util.List;

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
        // Bind buttons to the sheetLoaded property from sharedModel
        deleteRangeButton.disableProperty().bind(Bindings.isNull(rangeComboBox.valueProperty()));

        designButton.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );
        sortingFilteringButton.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );
        graphsButton.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );
        dynamicAnalysisButton.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );

        rangeComboBox.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );
        rangeNameField.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );
        fromCellField.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );
        toCellField.disableProperty().bind(
                sharedModel.sheetLoadedProperty().not().or(sharedModel.latestVersionSelectedProperty().not())
        );
        rangeDetailsArea.disableProperty().bind(Bindings.isNull(rangeComboBox.valueProperty()));

        sharedModel.latestVersionSelectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                populateSelectors();
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
            sharedModel.getAnimationController().fade(designButton);
            designPopUp = new Stage();
            // Load the design pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/left/design/designPopUp.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            DesignPopUpController designPopupController = loader.getController();
            designPopupController.setCenterController(centerController);

            // Set up the pop-up window
            designPopUp.setTitle("Design");
            designPopUp.setScene(new Scene(root));
            designPopUp.initOwner(designButton.getScene().getWindow());
            designPopUp.initModality(Modality.WINDOW_MODAL);
            designPopUp.setResizable(true);
            designPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            designPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSortingFilteringButtonAction() {
        try {
            sharedModel.getAnimationController().fade(sortingFilteringButton);
            sortingFilteringPopUp = new Stage();
            // Load the sorting and filtering pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/left/sort/and/filter/sortAndFilterPopUp.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            SortingAndFilteringController sortingFilteringPopupController = loader.getController();
            sortingFilteringPopupController.setCenterController(centerController);

            // Set up the pop-up window
            sortingFilteringPopUp.setTitle("Sorting and Filtering");
            sortingFilteringPopUp.setScene(new Scene(root));
            sortingFilteringPopUp.initOwner(sortingFilteringButton.getScene().getWindow());
            sortingFilteringPopUp.initModality(Modality.WINDOW_MODAL);
            sortingFilteringPopUp.setResizable(true);
            sortingFilteringPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            sortingFilteringPopUp.setOnCloseRequest(event -> sortingFilteringPopupController.handleRemoveSortAndFilter());
            sortingFilteringPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleGraphsButtonAction() {
        try {
            sharedModel.getAnimationController().fade(graphsButton);
            graphsPopUp = new Stage();
            // Load the design pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/left/graph/graphsPopUp.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            GraphPopUpController graphPopUpController = loader.getController();
            graphPopUpController.setCenterController(centerController);

            // Set up the pop-up window
            graphsPopUp.setTitle("Graphs");
            graphsPopUp.setScene(new Scene(root));
            graphsPopUp.initOwner(graphsButton.getScene().getWindow());
            graphsPopUp.initModality(Modality.WINDOW_MODAL);
            graphsPopUp.setResizable(true);
            graphsPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            graphsPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDynamicAnalysisButtonAction() {
        try {
            sharedModel.getAnimationController().fade(dynamicAnalysisButton);
            dynamicAnalysisPopUp = new Stage();
            // Load the sorting and filtering pop-up FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/left/dynamic/dynamicPopUp.fxml"));
            Parent root = loader.load();

            // Get the CommandsPopupController from the FXML
            DynamicAnalysisController dynamicAnalysisController = loader.getController();
            dynamicAnalysisController.setCenterController(centerController);

            // Set up the pop-up window
            dynamicAnalysisPopUp.setTitle("Dynamic Analysis");
            dynamicAnalysisPopUp.setScene(new Scene(root));
            dynamicAnalysisPopUp.initOwner(dynamicAnalysisButton.getScene().getWindow());
            dynamicAnalysisPopUp.initModality(Modality.WINDOW_MODAL);
            dynamicAnalysisPopUp.setResizable(true);
            dynamicAnalysisPopUp.getScene().getStylesheets().addAll(sharedModel.getPrimaryStage().getScene().getStylesheets());
            dynamicAnalysisPopUp.setOnCloseRequest(event -> dynamicAnalysisController.handleRemoveDynamicAnalysis());
            dynamicAnalysisPopUp.show();
        } catch (IOException e) {
            e.printStackTrace();
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
    private void handleAddRange() {
        String rangeName = rangeNameField.getText();
        String fromCell = fromCellField.getText();
        String toCell = toCellField.getText();

        // Validate inputs (You can add more validation as necessary)
        if (rangeName.isEmpty() || fromCell.isEmpty() || toCell.isEmpty()) {
            showErrorPopup("Error","Please fill all fields.");
            return;
        }

        if(rangeNames.contains(rangeName)) {
            showErrorPopup("Error","Range already exists.");
            return;
        }

        try {
            // Generate list of cells in the range
            List<Coordinate> cellsInRange = centerController.getEngine().createNewRange(rangeName, fromCell, toCell);

            // Add the range to the map and update the ComboBox
            rangeNames.add(rangeName);

            // Clear the input fields
            rangeNameField.clear();
            fromCellField.clear();
            toCellField.clear();
        }
        catch(OutOfBoundsException e)
        {
            showErrorPopup("Out of Bounds",e.getMessage());
        }
        catch (InvalidXMLFormatException e)
        {
            showErrorPopup("Invalid", e.getMessage());
        }
        catch (Exception e){
            showErrorPopup("Error", e.getMessage());
        }
    }

    // Handle selecting a range from the ComboBox
    @FXML
    private void handleSelectRange() {
        String selectedRange = rangeComboBox.getValue();
        if (selectedRange != null) {
            List<Coordinate> cellsInRange = centerController.getEngine().getSheet().getRange(selectedRange);
            rangeDetailsArea.setText(formatCoordinates(cellsInRange));  // Display formatted coordinates
        }
    }

    // Handle deleting the selected range
    @FXML
    private void handleDeleteRange() {
        String selectedRange = rangeComboBox.getValue();
        try {
            if (selectedRange != null) {
                centerController.getEngine().removeRange(selectedRange);
                rangeNames.remove(selectedRange);
                rangeDetailsArea.clear();

                // Trigger selection of the next available range after deletion
                if (!rangeNames.isEmpty()) {
                    handleSelectRange();  // Trigger the select range event to display the new selection
                }
            }
        } catch (IllegalArgumentException e) {
            showErrorPopup("Error Removing Range", e.getMessage());
        } catch (Exception e) {
            showErrorPopup("Error", e.getMessage());
        }
    }

    // Populate the ComboBox with ranges from the sheet
    @FXML
    public void populateSelectors() {
        if (centerController != null) {
            SheetDTO sheet = centerController.getEngine().getSheet();

            // Clear and populate the ComboBox with range names
            rangeNames.clear();
            rangeNames.addAll(centerController.getEngine().getSheet().getAllRanges().keySet());
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
}
