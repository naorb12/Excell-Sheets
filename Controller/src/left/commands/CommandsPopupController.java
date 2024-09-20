package left.commands;

import exception.InvalidXMLFormatException;
import javafx.beans.binding.Bindings;
import javafx.scene.control.CheckBox;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import center.CenterController;
import immutable.objects.SheetDTO;
import sheet.coordinate.Coordinate;

import java.util.*;

public class CommandsPopupController {

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
    private ColorPicker colorPicker;
    @FXML
    private Button colorUndoButton;

    // Sorting and Filtering
    @FXML
    private TextField fromCellFieldSortField;
    @FXML
    private TextField toCellFieldSortField;
    @FXML
    private ListView<CheckBox> checkboxListView;
    @FXML
    private Button applySortButton;
    @FXML
    private Button revertSortButton;
    private List<Integer> columnsToSortBy;

    @FXML
    private TextField fromCellFieldFilter;
    @FXML
    private TextField toCellFieldFilter;
    @FXML
    private ListView<CheckBox> checkboxListViewFilterColumns;
    @FXML
    private ListView<CheckBox> checkboxListViewFilterWords;
    @FXML
    private Button applyFilterButton;
    @FXML
    private Button revertFilterButton;
    private List<Integer> columnsToFilterBy;


    public void initialize() {
        initializeDesign();
        initializeSort();
        initializeFilter();
    }

    private void initializeDesign() {
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
        colorPicker.disableProperty().bind(Bindings.isNull(cellSelector.valueProperty()));

    }

    private void initializeSort() {
        // Initially disable the applySortButton
        applySortButton.setDisable(true);
        revertSortButton.setDisable(true);
        // Add listeners to the text fields to track their state
        fromCellFieldSortField.textProperty().addListener((observable, oldValue, newValue) -> updateApplySortButtonState());
        toCellFieldSortField.textProperty().addListener((observable, oldValue, newValue) -> updateApplySortButtonState());

        // Add listeners to the checkboxes when they are populated
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            if (!fromCellFieldSortField.getText().trim().isEmpty() && !toCellFieldSortField.getText().trim().isEmpty()) {
                String fromCell = fromCellFieldSortField.getText().trim();
                String toCell = toCellFieldSortField.getText().trim();
                try {
                    populateCheckboxListView(fromCell, toCell, checkboxListView, ()->{
                        try {
                            updateApplySortButtonState();
                        } catch (RuntimeException e) {
                            //
                        }
                    });
                } catch (InvalidXMLFormatException e) {
                    //showErrorPopup("Invalid Range", e.getMessage());
                }
            }
        };
        fromCellFieldSortField.textProperty().addListener(listener);
        toCellFieldSortField.textProperty().addListener(listener);

        applySortButton.setOnAction(event -> {
            try {
                handleApplySort();
            } catch (InvalidXMLFormatException e) {
                throw new RuntimeException(e);
            }
        });

        revertSortButton.setOnAction(event -> handleRemoveSort());
    }

    private void initializeFilter() {
        // Initially disable the applySortButton
        applyFilterButton.setDisable(true);
        revertFilterButton.setDisable(true);
        // Add listeners to the text fields to track their state
        fromCellFieldFilter.textProperty().addListener((observable, oldValue, newValue) -> updateApplyFilterButtonState());
        toCellFieldFilter.textProperty().addListener((observable, oldValue, newValue) -> updateApplyFilterButtonState());

        // Add listeners to the checkboxes when they are populated
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            if (!fromCellFieldFilter.getText().trim().isEmpty() && !toCellFieldFilter.getText().trim().isEmpty()) {
                String fromCell = fromCellFieldFilter.getText().trim();
                String toCell = toCellFieldFilter.getText().trim();
                try {
                    populateCheckboxListView(fromCell, toCell, checkboxListViewFilterColumns, ()->{populateCheckboxListViewFilterWords();});
                } catch (InvalidXMLFormatException e) {
                    //showErrorPopup("Invalid Range", e.getMessage());
                }
            }
        };
        fromCellFieldFilter.textProperty().addListener(listener);
        toCellFieldFilter.textProperty().addListener(listener);

        applyFilterButton.setOnAction(event -> {
            handleApplyFilter();
        });

        revertFilterButton.setOnAction(event -> handleRemoveFilter());
    }

    // Method to update the apply button's state based on text fields and checkbox selections
    private void updateApplySortButtonState() {
        boolean areTextFieldsFilled = !fromCellFieldSortField.getText().trim().isEmpty() && !toCellFieldSortField.getText().trim().isEmpty();
        boolean isAnyCheckboxSelected = checkboxListView.getItems().stream().anyMatch(CheckBox::isSelected);

        // Enable or disable the applySortButton based on the conditions
        applySortButton.setDisable(!(areTextFieldsFilled && isAnyCheckboxSelected));
    }

    private void updateApplyFilterButtonState() {
        boolean areTextFieldsFilled = !fromCellFieldFilter.getText().trim().isEmpty() && !toCellFieldFilter.getText().trim().isEmpty();
        boolean isAnyColumnCheckboxSelected = checkboxListViewFilterColumns.getItems().stream().anyMatch(CheckBox::isSelected);
        boolean isAnyWordCheckboxSelected = checkboxListViewFilterWords.getItems().stream().anyMatch(CheckBox::isSelected);

        // Enable or disable the applySortButton based on the conditions
        applyFilterButton.setDisable(!(areTextFieldsFilled && isAnyColumnCheckboxSelected && isAnyWordCheckboxSelected));
    }

    private void populateCheckboxListViewFilterWords(){
        updateApplyFilterButtonState();
    }

    private void populateCheckboxListView(String fromCell, String toCell, ListView<CheckBox> listView,  Runnable runnable) throws InvalidXMLFormatException {
        // Clear previous items
        listView.getItems().clear();
        columnsToSortBy = new ArrayList<>();  // Clear the columnsToSortBy list

        List<Coordinate> range = centerController.getEngine().validateRange(fromCell, toCell);
        Set<Integer> columnsSet = parseRangeToColumns(range);

        for (Integer columnIndex : columnsSet) {
            String columnName = getColumnName(columnIndex - 1);
            CheckBox checkBox = new CheckBox(columnName);

            // Add listener to track the state of each checkbox and update columnsToSortBy list
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    // Checkbox is selected: add the column (in order) to columnsToSortBy
                    columnsToSortBy.add(columnIndex);
                } else {
                    // Checkbox is deselected: remove the column from columnsToSortBy
                    columnsToSortBy.remove(Integer.valueOf(columnIndex));
                }

                runnable.run();
            });

            // Add the CheckBox to the ListView
            listView.getItems().add(checkBox);
        }
    }



    private Set<Integer> parseRangeToColumns(List<Coordinate> range) {
        Set<Integer> columnsToSortBy = new HashSet<>();
        for (Coordinate coordinate : range) {
            columnsToSortBy.add(coordinate.getColumn());
        }
        return columnsToSortBy;
    }

    private void populateSelectors() {
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
        colorPicker.setValue(Color.WHITE);
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
        Color color = colorPicker.getValue();

        centerController.updateCellBackgroundColor(selectedCell, color);  // Pass color update to CenterController
    }

    // Method to undo the background color of a cell
    @FXML
    private void handleUndoColor() {
        String selectedCell = cellSelector.getValue();

        centerController.undoCellColor(selectedCell);  // Undo background color in CenterController
    }



    @FXML
    private void handleApplySort() throws InvalidXMLFormatException {
        try {
            if (!columnsToSortBy.isEmpty()) {
                // Perform sorting based on the selected columns
                centerController.applySorting(fromCellFieldSortField.getText().trim(), toCellFieldSortField.getText().trim(), columnsToSortBy);
                revertSortButton.setDisable(false);
            }
        } catch (InvalidXMLFormatException e) {
            showErrorPopup("Range not valid", e.getMessage());
        } catch (RuntimeException e) {
            showErrorPopup("Runtime Error", e.getMessage());
        }
    }

    @FXML
    public void handleRemoveSort() {
        centerController.removeSorting();
    }


    @FXML
    private void handleRemoveFilter() {
    }

    @FXML
    private void handleApplyFilter() {
    }

    // Set the reference to CenterController and populate selectors after it's set
    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;
        populateSelectors();  // Populate the selectors after setting centerController
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
