package left.ranges;

import center.CenterController;
import exception.InvalidXMLFormatException;
import exception.OutOfBoundsException;
import immutable.objects.SheetDTO;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sheet.coordinate.Coordinate;

import java.util.List;

public class RangesPopUpController {

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

    private CenterController centerController;

    // Stores ranges with their corresponding list of cells
    private ObservableList<String> rangeNames = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Set up the ComboBox with the list of range names
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
        deleteRangeButton.disableProperty().bind(Bindings.isNull(rangeComboBox.valueProperty()));
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

    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;
        populateSelectors();
    }

    // Populate the ComboBox with ranges from the sheet
    private void populateSelectors() {
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
        for (Coordinate coord : coordinates) {
            formatted.append(coord.toString()).append(", ");
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
