package left;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    // Stores ranges with their corresponding list of cells
    private Map<String, List<String>> ranges = new HashMap<>();
    private ObservableList<String> rangeNames = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Set up the ComboBox with the list of range names
        rangeComboBox.setItems(rangeNames);
    }

    // Handle adding a new range
    @FXML
    private void handleAddRange() {
        String rangeName = rangeNameField.getText();
        String fromCell = fromCellField.getText();
        String toCell = toCellField.getText();

        // Validate inputs (You can add more validation as necessary)
        if (rangeName.isEmpty() || fromCell.isEmpty() || toCell.isEmpty()) {
            rangeDetailsArea.setText("Please fill all fields.");
            return;
        }

        // Generate list of cells in the range
        List<String> cellsInRange = generateRange(fromCell, toCell);

        // Add the range to the map and update the ComboBox
        ranges.put(rangeName, cellsInRange);
        rangeNames.add(rangeName);

        // Clear the input fields
        rangeNameField.clear();
        fromCellField.clear();
        toCellField.clear();
    }

    // Handle selecting a range from the ComboBox
    @FXML
    private void handleSelectRange() {
        String selectedRange = rangeComboBox.getValue();
        if (selectedRange != null) {
            List<String> cellsInRange = ranges.get(selectedRange);
            rangeDetailsArea.setText(String.join(", ", cellsInRange));
        }
    }

    // Handle deleting the selected range
    @FXML
    private void handleDeleteRange() {
        String selectedRange = rangeComboBox.getValue();
        if (selectedRange != null) {
            ranges.remove(selectedRange);
            rangeNames.remove(selectedRange);
            rangeDetailsArea.clear();
        }
    }

    // Generate a list of cells between the 'from' and 'to' cells
    private List<String> generateRange(String fromCell, String toCell) {
        // For simplicity, assume we work within a single column or row range
        // You may add more complex logic for cross-column and cross-row ranges
        List<String> cells = new ArrayList<>();
        cells.add(fromCell);  // Add the "from" cell
        cells.add(toCell);    // Add the "to" cell
        // You can add logic here to generate the full range
        return cells;
    }
}
