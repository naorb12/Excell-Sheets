package left;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import center.CenterController;
import immutable.objects.SheetDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CommandsPopupController {

    @FXML
    private ComboBox<String> columnWidthSelector;

    @FXML
    private ComboBox<String> columnAlignmentSelector;

    @FXML
    private Spinner<Double> columnWidthSpinner;

    @FXML
    private ComboBox<String> rowSelector;
    @FXML
    private Spinner<Double> rowHeightSpinner;

    @FXML
    private ComboBox<String> alignmentSelector;

    @FXML
    private ComboBox<String> cellSelector;
    @FXML
    private ColorPicker colorPicker;

    private CenterController centerController;

    public void initialize() {
        // Initialize the spinners with default value factories
        columnWidthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(10, 500, 100, 1));
        columnWidthSpinner.getValueFactory().setValue(null);  // Clear the initial value
        rowHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(10, 200, 30, 1));
        rowHeightSpinner.getValueFactory().setValue(null);

        // Add listeners to update spinner values when a column/row is selected
        columnWidthSelector.setOnAction(event -> updateColumnWidthSpinner());
        rowSelector.setOnAction(event -> updateRowHeightSpinner());
    }

    public void populateSelectors() {
        if (centerController != null) {
            SheetDTO sheet = centerController.getEngine().getSheet();
            int columnCount = sheet.getColumnCount();
            int rowCount = sheet.getRowCount();

            // Populate columnSelector with column names (A, B, C, ...)
            for (int i = 0; i < columnCount; i++) {
                String columnName = getColumnName(i);
                columnWidthSelector.getItems().add(columnName);
                columnAlignmentSelector.getItems().add(columnName);
            }

            // Populate rowSelector with row numbers (1, 2, 3, ...)
            for (int i = 1; i <= rowCount; i++) {
                rowSelector.getItems().add(String.valueOf(i));
            }

            // Collect all cell names (A1, B2, C3, ...) into a list
            List<String> cellNames = new ArrayList<>();
            for (int row = 1; row <= rowCount; row++) {
                for (int col = 0; col < columnCount; col++) {
                    String cellName = getColumnName(col) + row;  // Create cell name (e.g., A1, B2, etc.)
                    cellNames.add(cellName);
                }
            }

            // Sort the cell names correctly: by column (lexicographically) and row (numerically)
            Collections.sort(cellNames, new Comparator<String>() {
                @Override
                public int compare(String cell1, String cell2) {
                    // Split column letter and row number for both cells
                    String column1 = cell1.replaceAll("\\d", "");  // Extract column letter from cell1
                    String column2 = cell2.replaceAll("\\d", "");  // Extract column letter from cell2

                    int row1 = Integer.parseInt(cell1.replaceAll("[A-Z]", ""));  // Extract row number from cell1
                    int row2 = Integer.parseInt(cell2.replaceAll("[A-Z]", ""));  // Extract row number from cell2

                    // First, compare the columns lexicographically (A, B, C...)
                    int columnComparison = column1.compareTo(column2);
                    if (columnComparison != 0) {
                        return columnComparison;
                    }

                    // If columns are the same, compare the rows numerically
                    return Integer.compare(row1, row2);
                }
            });

            // Populate cellSelector with the sorted cell names
            cellSelector.getItems().addAll(cellNames);
        }
    }


    // Method to update the column width spinner based on the selected column
    private void updateColumnWidthSpinner() {
        String selectedColumn = columnWidthSelector.getValue();
        if (selectedColumn != null) {
            int colIndex = getColumnIndex(selectedColumn);
            if (colIndex >= 0) {
                // Get the current column width from the GridPane
                double currentWidth = centerController.getColumnWidth(colIndex);

                // Update the spinner with the current width
                columnWidthSpinner.getValueFactory().setValue(currentWidth);
            }
        }
    }

    // Method to update the row height spinner based on the selected row
    private void updateRowHeightSpinner() {
        String selectedRow = rowSelector.getValue();
        if (selectedRow != null) {
            int rowIndex = Integer.parseInt(selectedRow) - 1;
            if (rowIndex >= 0) {
                // Get the current row height from the GridPane
                double currentHeight = centerController.getRowHeight(rowIndex);

                // Update the spinner with the current height
                rowHeightSpinner.getValueFactory().setValue(currentHeight);
            }
        }
    }

    // Helper method to convert column index to column name
    private String getColumnName(int index) {
        StringBuilder columnName = new StringBuilder();
        while (index >= 0) {
            columnName.insert(0, (char) ('A' + index % 26));
            index = (index / 26) - 1;
        }
        return columnName.toString();
    }

    // Helper method to get the column index from column name
    private int getColumnIndex(String columnName) {
        int index = 0;
        for (int i = 0; i < columnName.length(); i++) {
            index *= 26;
            index += columnName.charAt(i) - 'A' + 1;
        }
        return index - 1;
    }

    // Method to update the column width using Spinner
    @FXML
    private void handleUpdateColumnWidth() {
        String selectedColumn = columnWidthSelector.getValue();
        double width = columnWidthSpinner.getValue();  // Get width from Spinner

        centerController.updateColumnWidth(selectedColumn, width);  // Pass the width update to CenterController
    }

    // Method to update the row height using Spinner
    @FXML
    private void handleUpdateRowHeight() {
        String selectedRow = rowSelector.getValue();
        double height = rowHeightSpinner.getValue();  // Get height from Spinner

        centerController.updateRowHeight(selectedRow, height);  // Pass the height update to CenterController
    }

    // Method to update the text alignment
    @FXML
    private void handleUpdateTextAlignment() {
        String selectedColumn = columnAlignmentSelector.getValue();
        String alignment = alignmentSelector.getValue();

        centerController.updateColumnAlignment(selectedColumn, alignment);  // Pass the alignment update to CenterController
    }

    // Method to update the background color of a cell
    @FXML
    private void handleUpdateBackgroundColor() {
        String selectedCell = cellSelector.getValue();
        Color color = colorPicker.getValue();

        centerController.updateCellBackgroundColor(selectedCell, color);  // Pass color update to CenterController
    }

    // Method to undo the background color of a cell
    @FXML
    private void handleUndoBackgroundColor() {
        String selectedCell = cellSelector.getValue();

        centerController.undoCellBackgroundColor(selectedCell);  // Undo background color in CenterController
    }

    // Set the reference to CenterController and populate selectors after it's set
    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;
        populateSelectors();  // Populate the selectors after setting centerController
    }
}
