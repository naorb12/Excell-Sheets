package shticell.client.component.sheet.left.design;

import immutable.objects.SheetDTO;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.main.SharedModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DesignPopUpController {

    private CenterController centerController;

    // Design
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
    private ColorPicker colorPickerBackground;
    @FXML
    private ColorPicker colorPickerTextColor;
    @FXML
    private Button colorUndoButton;

    private SharedModel sharedModel;


    public void initialize() {
        // Initialize the spinners with default value factories
        columnWidthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 500, 100, 1));
        columnWidthSpinner.getValueFactory().setValue(null);  // Clear the initial value
        columnWidthSpinner
                .valueProperty()
                .addListener((observable, oldValue, newValue) -> handleUpdateColumnWidth());

        rowHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1, 200, 30, 1));
        rowHeightSpinner.getValueFactory().setValue(null);
        rowHeightSpinner
                .valueProperty()
                .addListener((observable, oldValue, newValue) -> handleUpdateRowHeight());

        // Add listeners to update spinner values when a column/row is selected
        columnWidthSelector.setOnAction(event -> updateColumnWidthSpinner());
        columnWidthSpinner.disableProperty().bind(Bindings.isNull(columnWidthSelector.valueProperty()));

        rowSelector.setOnAction(event -> updateRowHeightSpinner());
        rowHeightSpinner.disableProperty().bind(Bindings.isNull(rowSelector.valueProperty()));

        columnAlignmentSelector.setOnAction(event -> resetColumnAlignmentSelector());
        alignmentSelector.disableProperty().bind(Bindings.isNull((columnAlignmentSelector.valueProperty())));

        cellSelector.setOnAction(event -> resetColorPicker());
        colorUndoButton.disableProperty().bind(Bindings.isNull(cellSelector.valueProperty()));
        colorPickerBackground.disableProperty().bind(Bindings.isNull(cellSelector.valueProperty()));
        colorPickerTextColor.disableProperty().bind(Bindings.isNull(cellSelector.valueProperty()));

    }

    // Set the reference to CenterController and populate selectors after it's set
    public void setCenterController(CenterController centerController) throws ExecutionException, InterruptedException {
        this.centerController = centerController;
        populateSelectors();  // Populate the selectors after setting centerController
    }

    public void setSharedModel(SharedModel sharedModel) {
        this.sharedModel = sharedModel;
    }

    private void populateSelectors() throws ExecutionException, InterruptedException {
        if (centerController != null) {
            SheetDTO sheet = centerController.getServerEngineService().getSheet(sharedModel.getSheetName()).get();
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
            int colIndex = getColumnIndex(selectedColumn) + 1;
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
            int rowIndex = Integer.parseInt(selectedRow);
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

    private void resetColorPicker() {
        colorPickerBackground.setValue(Color.WHITE);
        colorPickerTextColor.setValue(Color.BLACK);
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

    @FXML
    private void resetColumnAlignmentSelector() {
        alignmentSelector.setValue("Center");
    }


    // Method to update the background color of a cell
    @FXML
    private void handleUpdateBackgroundColor() {
        String selectedCell = cellSelector.getValue();
        Color color = colorPickerBackground.getValue();

        centerController.updateCellBackgroundColor(selectedCell, color);  // Pass color update to CenterController
    }

    @FXML
    private void handleUpdateTextColor(){
        String selectedCell = cellSelector.getValue();
        Color color = colorPickerTextColor.getValue();

        centerController.updateCellTextColor(selectedCell, color);
    }

    // Method to undo the background color of a cell
    @FXML
    private void handleUndoColor() {
        String selectedCell = cellSelector.getValue();

        centerController.undoCellColor(selectedCell);  // Undo background color in CenterController
    }

}
