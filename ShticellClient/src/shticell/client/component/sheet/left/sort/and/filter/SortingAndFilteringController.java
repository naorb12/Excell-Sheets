package shticell.client.component.sheet.left.sort.and.filter;

import exception.InvalidXMLFormatException;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sheet.coordinate.Coordinate;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.main.SharedModel;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class SortingAndFilteringController {

    private CenterController centerController;

    private SharedModel sharedModel;

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
    private List<Integer> columnsToSortBy = new ArrayList<>();;

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
    private List<Integer> columnsToFilterBy = new ArrayList<>();;
    private Set<String> selectedWordsSet = new HashSet<>();

    public void initialize(){
        initializeSort();
        initializeFilter();
    }

    public void setSharedModel(SharedModel sharedModel) {
        this.sharedModel = sharedModel;
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
                populateCheckboxListView(fromCell, toCell, checkboxListView, ()->{
                    try {
                        updateApplySortButtonState();
                    } catch (RuntimeException e) {
                        //
                    }
                }, columnsToSortBy);
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

        revertSortButton.setOnAction(event -> {
            try {
                handleRemoveSort();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void initializeFilter() {
        // Initially disable the applyFilterButton and revertFilterButton
        applyFilterButton.setDisable(true);
        revertFilterButton.setDisable(true);

        // Add listeners to the text fields to track their state
        fromCellFieldFilter.textProperty().addListener((observable, oldValue, newValue) -> handleFilterRangeChange());
        toCellFieldFilter.textProperty().addListener((observable, oldValue, newValue) -> handleFilterRangeChange());

        applyFilterButton.setOnAction(event -> {
            try {
                handleApplyFilter();
            } catch (InvalidXMLFormatException e) {
                throw new RuntimeException(e);
            }
        });

        revertFilterButton.setOnAction(event -> {
            try {
                handleRemoveFilter();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // Add listener to checkboxes in the columns list to reset the words when columns change
        checkboxListViewFilterColumns.getItems().forEach(checkbox -> {
            checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> handleColumnSelectionChange());
        });
    }


    private void handleFilterRangeChange() {
        if (!fromCellFieldFilter.getText().trim().isEmpty() && !toCellFieldFilter.getText().trim().isEmpty()) {
            String fromCell = fromCellFieldFilter.getText().trim();
            String toCell = toCellFieldFilter.getText().trim();
            // Populate column selector
            populateCheckboxListView(fromCell, toCell, checkboxListViewFilterColumns, () -> {
                if (checkboxListViewFilterColumns.getItems().stream().anyMatch(CheckBox::isSelected)) {
                    if (!isValidCellFormat(fromCellFieldFilter.getText()) || !isValidCellFormat(toCellFieldFilter.getText())) {
                        // Skip the request if the format is invalid
                        return;
                    }
                    populateCheckboxListViewFilterWords(fromCell, toCell);
                } else {
                    checkboxListViewFilterWords.getItems().clear();  // Clear words if no columns are selected
                }
            }, columnsToFilterBy);
        } else {
            // Clear both columns and words if the range fields are empty
            checkboxListViewFilterColumns.getItems().clear();
            checkboxListViewFilterWords.getItems().clear();
        }

        updateApplyFilterButtonState();
    }

    // Helper method to check if a cell reference is valid, e.g., "C3" or "D10"
    private boolean isValidCell(String cell) {
        return cell != null && cell.matches("^[A-Za-z]+\\d+$");
    }

    private void handleColumnSelectionChange() {
        if (checkboxListViewFilterColumns.getItems().stream().anyMatch(CheckBox::isSelected)) {
            String fromCell = fromCellFieldFilter.getText().trim();
            String toCell = toCellFieldFilter.getText().trim();
            populateCheckboxListViewFilterWords(fromCell, toCell);
        } else {
            checkboxListViewFilterWords.getItems().clear();
        }

        updateApplyFilterButtonState();
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

    private void populateCheckboxListViewFilterWords(String fromCellFieldFilter, String toCellFieldFilter) {
        checkboxListViewFilterWords.getItems().clear();  // Clear existing items

        boolean anyColumnSelected = checkboxListViewFilterColumns.getItems().stream().anyMatch(CheckBox::isSelected);
        if (!anyColumnSelected) {
            return;  // If no columns are selected, exit early
        }

        for (CheckBox columnCheckbox : checkboxListViewFilterColumns.getItems()) {
            if (columnCheckbox.isSelected()) {
                String column = columnCheckbox.getText();  // Get the column name

                // Use the updated asynchronous method
                getWordsForColumn(column, fromCellFieldFilter, toCellFieldFilter, words -> {
                    for (String word : words) {
                        CheckBox wordCheckbox = new CheckBox(word);

                        // Add a listener to update the set when the checkbox is selected/unselected
                        wordCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue) {
                                selectedWordsSet.add(word);
                            } else {
                                selectedWordsSet.remove(word);
                            }
                            updateApplyFilterButtonState();
                        });

                        checkboxListViewFilterWords.getItems().add(wordCheckbox);
                    }
                    updateApplyFilterButtonState();
                });
            }
        }
    }



    private void getWordsForColumn(String column, String fromCellFieldFilter, String toCellFieldFilter, Consumer<Set<String>> callback) {
        // Fetch words asynchronously
        centerController.getServerEngineService()
                .getWordsFromColumnAndRange(sharedModel.getSheetName(), column, fromCellFieldFilter, toCellFieldFilter)
                .thenAccept(words -> Platform.runLater(() -> callback.accept(words)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        System.out.println("Error retrieving words for column: " + ex.getMessage());
                        callback.accept(Collections.emptySet()); // Return empty set on error
                    });
                    return null;
                });
    }


    private void populateCheckboxListView(String fromCell, String toCell, ListView<CheckBox> listView, Runnable runnable, List<Integer> columnsToXBy) {
        if (!isValidCell(fromCell) || !isValidCell(toCell)) {
            return;
        }

        // Clear previous items
        listView.getItems().clear();

        // If columnsToXBy is null, initialize it. Do not reassign to a new list.
        if (columnsToXBy == null) {
            columnsToXBy = new ArrayList<>();
        } else {
            columnsToXBy.clear();  // Clear the columnsToSortBy list
        }

        // Fetch the range asynchronously
        List<Integer> finalColumnsToXBy1 = columnsToXBy;
        centerController.getServerEngineService().validateRange(sharedModel.getSheetName(), fromCell, toCell)
                .thenAccept(range -> Platform.runLater(() -> {
                    // Run this block on the JavaFX Application Thread to update the UI
                    Set<Integer> columnsSet = parseRangeToColumns(range);

                    for (Integer columnIndex : columnsSet) {
                        String columnName = getColumnName(columnIndex - 1);
                        CheckBox checkBox = new CheckBox(columnName);

                        // Add listener to track the state of each checkbox and update columnsToSortBy list
                        List<Integer> finalColumnsToXBy = finalColumnsToXBy1;
                        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                            if (newValue) {
                                // Checkbox is selected: add the column (in order) to columnsToSortBy
                                finalColumnsToXBy.add(columnIndex);
                            } else {
                                // Checkbox is deselected: remove the column from columnsToSortBy
                                finalColumnsToXBy.remove(Integer.valueOf(columnIndex));
                            }

                            runnable.run();
                        });

                        // Add the CheckBox to the ListView
                        listView.getItems().add(checkBox);
                    }
                }))
                .exceptionally(ex -> {
                    // Handle any exceptions
                    Platform.runLater(() -> showErrorPopup("Error", "Failed to populate columns due to an error: " + ex.getMessage()));
                    return null;
                });
    }


    private boolean isValidCellFormat(String cell) {
        return cell != null && cell.matches("^[A-Za-z]+\\d+$"); // e.g., "B2" or "C5"
    }

    private Set<Integer> parseRangeToColumns(List<Coordinate> range) {
        Set<Integer> columnsToSortBy = new HashSet<>();
        for (Coordinate coordinate : range) {
            columnsToSortBy.add(coordinate.getColumn());
        }
        return columnsToSortBy;
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
    private void handleRemoveSort() throws ExecutionException, InterruptedException {
        centerController.removeSorting();
        columnsToSortBy.clear();
        checkboxListView.getItems().forEach(checkBox -> checkBox.setSelected(false));
    }

    @FXML
    public void handleRemoveSortAndFilter() throws ExecutionException, InterruptedException {
        centerController.removeSorting();
        centerController.removeFiltering();
        columnsToSortBy.clear();
        selectedWordsSet.clear();
    }

    @FXML
    private void handleApplyFilter() throws InvalidXMLFormatException {
        try {
            if (!selectedWordsSet.isEmpty()) {
                centerController.applyFiltering(fromCellFieldFilter.getText().trim(), toCellFieldFilter.getText().trim(), selectedWordsSet);
                revertFilterButton.setDisable(false);
            }
        }catch (InvalidXMLFormatException e) {
            showErrorPopup("Range not valid", e.getMessage());
        } catch (RuntimeException e) {
            showErrorPopup("Runtime Error", e.getMessage());
        }
    }

    @FXML
    private void handleRemoveFilter() throws ExecutionException, InterruptedException {
        centerController.removeFiltering();
        selectedWordsSet.clear();
        checkboxListViewFilterColumns.getItems().forEach(checkBox -> checkBox.setSelected(false));
        checkboxListViewFilterWords.getItems().forEach(checkBox -> checkBox.setSelected(false));
        applyFilterButton.setDisable(true);
    }

    // Set the reference to CenterController and populate selectors after it's set
    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;
        //populateSelectors();  // Populate the selectors after setting centerController
    }

    @FXML
    private void showErrorPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();  // Shows the alert and waits for the user to close it
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
}