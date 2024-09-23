package center;

import engine.Engine;
import exception.InvalidXMLFormatException;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import sheet.cell.impl.CellType;
import sheet.coordinate.Coordinate;
import top.TopController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CenterController {

    @FXML
    private GridPane spreadsheetGridPane;

    @FXML
    private Label selectedCellLabel;  // Track the currently selected cell

    @FXML
    private Set<Label> dependsOnCellLabel = new HashSet<>();  // Track the currently highlighted dependsOn cell

    @FXML
    private Set<Label> influencingOnCellLabel = new HashSet<>();  // Track the currently highlighted influencingOn cell


    private TopController topController;

    private Engine engine;

    // Track original states of the grid for undoing sort
    private Map<Coordinate, CellDTO> originalCells;
    private Set<Label> originalStyledLabels;

    // Track sorted state
    private boolean isSorted = false;
    // Track filtered state
    private boolean isFiltered = false;

    private SimpleBooleanProperty isFileSelected = new SimpleBooleanProperty(false);

    public void initialize() {
        engine = new Engine();
    }

    @FXML
    public void renderGridPane() {
        // Render the grid from the engine's sheet data
        renderGrid(engine.getSheet());
    }

    public void renderGrid(SheetDTO sheet) {
        isFileSelected.set(true);
        spreadsheetGridPane.getChildren().clear();
        spreadsheetGridPane.getColumnConstraints().clear();
        spreadsheetGridPane.getRowConstraints().clear();

        if (sheet == null) {
            System.out.println("No sheet data available to render.");
            return;
        }

        int rows = engine.getSheet().getRowCount();
        int cols = engine.getSheet().getColumnCount();
        int rowHeight = engine.getSheet().getRowHeightUnits();  // Assuming you have these methods in the engine
        int colWidth = engine.getSheet().getColumnsWidthUnits();

        // Set up row and column constraints
        for (int i = 0; i <= cols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(colWidth);
            spreadsheetGridPane.getColumnConstraints().add(colConstraints);
        }
        for (int i = 0; i <= rows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(rowHeight);
            spreadsheetGridPane.getRowConstraints().add(rowConstraints);
        }

        // Add column headers (A, B, C, etc.)
        for (int col = 1; col <= cols; col++) {
            Label colHeader = new Label(Character.toString((char) ('A' + col - 1)));
            colHeader.getStyleClass().add("header");
            colHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            spreadsheetGridPane.add(colHeader, col, 0);
        }

        // Add row headers (1, 2, 3, etc.)
        for (int row = 1; row <= rows; row++) {
            Label rowHeader = new Label(Integer.toString(row));
            rowHeader.getStyleClass().add("header");
            rowHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            spreadsheetGridPane.add(rowHeader, 0, row);
        }


        // Add cells with click event
        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= cols; col++) {
                CellDTO cell = sheet.getCellDTO(row, col);
                Label cellLabel;
                if (cell == null) {
                    cellLabel = new Label("");
                }
                else {
                    cellLabel = new Label(cell.getEffectiveValue().getValue() != null ? cell.getEffectiveValue().getValue().toString() : "");
                    if(cell.getEffectiveValue().getCellType().equals(CellType.BOOLEAN)){
                        cellLabel.setText(cellLabel.getText().toUpperCase());
                    }
                    if(cell.getBackgroundColor() != null){
                        cellLabel.setBackground(new Background(new BackgroundFill(cell.getBackgroundColor(), CornerRadii.EMPTY, new Insets(0))));
                        System.out.println("Rendering " + cell.getCoordinate() + " background color: " + cell.getBackgroundColor());
                    }
                    if(cell.getForegroundColor() != null){
                        cellLabel.setTextFill(cell.getForegroundColor());
                        System.out.println("Rendering " + cell.getCoordinate() + " textFill color: " + cell.getForegroundColor());
                    }
                }


                // Apply the 'cell' style class
                cellLabel.getStyleClass().add("cell");

                // Ensure the label takes up the full size of its cell
                cellLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // Handle the cell click event to update the TopController
                final int selectedRow = row;
                final int selectedCol = col;
                cellLabel.setOnMouseClicked(event -> {
                    // Prevent the event from propagating to the parent (mainBorderPane)
                    event.consume();

                    // Clear the highlights before setting new ones
                    clearHighlights();

                    String selectedCellID = Character.toString((char) ('A' + selectedCol - 1)) + selectedRow; // e.g., A1, B2, etc.

                    // Update selected cell color and revert the previous one
                    if (selectedCellLabel != null) {
                        selectedCellLabel.getStyleClass().remove("selected-cell");
                    }
                    cellLabel.getStyleClass().add("selected-cell");
                    selectedCellLabel = cellLabel;  // Store the currently selected cell
                    topController.enableCellOriginalValueTExtField();

                    // Highlight dependsOn and influencingOn cells
                    if (cell != null) {
                        highlightDependsOnCells(cell.getDependsOn());
                        highlightInfluencingOnCells(cell.getInfluencingOn());

                        // Update the top controller with selected cell ID and value
                        topController.updateSelectedCell(selectedCellID, cell.getOriginalValue(), cell.getVersion());
                    } else {
                        topController.updateSelectedCell(selectedCellID, null, 1);  // Handle case for empty cells
                    }
                });

                spreadsheetGridPane.add(cellLabel, col, row);
            }
        }

    }

    // Method to clear previous highlights
    public void clearHighlights() {
        for (Node node : spreadsheetGridPane.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).getStyleClass().removeAll("selected-cell", "depends-on-cell", "influencing-on-cell");  // Clear all highlight styles
            }
        }
    }

    // Method to highlight cells in the dependsOn set (light blue)
    private void highlightDependsOnCells(Set<Coordinate> dependsOn) {
        for (Coordinate coord : dependsOn) {
            Label cellLabel = getNodeByRowColumnIndex(coord.getRow(), coord.getColumn());  // Get the cell by coordinate
            if (cellLabel != null) {
                dependsOnCellLabel.add(cellLabel);
                cellLabel.getStyleClass().add("depends-on-cell");  // Add the CSS class for dependsOn cells
            }
        }
    }

    // Method to highlight cells in the influencingOn set (light green)
    private void highlightInfluencingOnCells(Set<Coordinate> influencingOn) {
        for (Coordinate coord : influencingOn) {
            Label cellLabel = getNodeByRowColumnIndex(coord.getRow(), coord.getColumn());  // Get the cell by coordinate
            if (cellLabel != null) {
                influencingOnCellLabel.add(cellLabel);
                cellLabel.getStyleClass().add("influencing-on-cell");  // Add the CSS class for influencingOn cells
            }
        }
    }


    public void updateColumnWidth(String column, double width) {
        int colIndex = getColumnIndex(column);  // Convert column letter to index (A -> 0, B -> 1, etc.)
        colIndex++;
        if (colIndex >= 0 && colIndex < spreadsheetGridPane.getColumnConstraints().size()) {
            ColumnConstraints colConstraints = spreadsheetGridPane.getColumnConstraints().get(colIndex);
            colConstraints.setPrefWidth(width);  // Update column width
        }
    }

    public void updateRowHeight(String row, double height) {
        int rowIndex = Integer.parseInt(row);  // Convert row number (1-based) to index (0-based)
        if (rowIndex >= 0 && rowIndex < spreadsheetGridPane.getRowConstraints().size()) {
            RowConstraints rowConstraints = spreadsheetGridPane.getRowConstraints().get(rowIndex);
            rowConstraints.setPrefHeight(height);  // Update row height
        }
    }

    public void updateColumnAlignment(String column, String alignment) {
        int colIndex = getColumnIndex(column);  // Convert column letter to index
        colIndex++;
        if (colIndex >= 0) {
            for (int row = 1; row <= spreadsheetGridPane.getRowCount(); row++) {
                Label label = (Label) getNodeByRowColumnIndex(row, colIndex);  // Get the Label node in each cell
                if (label != null) {
                    switch (alignment.toLowerCase()) {
                        case "left":
                            label.setStyle("-fx-alignment: center-left;");
                            break;
                        case "center":
                            label.setStyle("-fx-alignment: center;");
                            break;
                        case "right":
                            label.setStyle("-fx-alignment: center-right;");
                            break;
                    }
                }
            }
        }
    }

    public void updateCellBackgroundColor(String cell, Color color) {
        int row = Integer.parseInt(cell.substring(1));  // Extract row number (e.g., A1 -> row 1) and convert to index
        int col = getColumnIndex(cell.substring(0, 1)) + 1;  // Extract column letter (e.g., A1 -> column A)

        Label label = (Label) getNodeByRowColumnIndex(row, col);
        if (label != null) {
            label.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, new Insets(0))));
        }

        engine.setBackgroundColor(row, col, color);
    }

    public void updateCellTextColor(String cell, Color color) {
        int row = Integer.parseInt(cell.substring(1));  // Extract row number (e.g., A1 -> row 1) and convert to index
        int col = getColumnIndex(cell.substring(0, 1)) + 1;  // Extract column letter (e.g., A1 -> column A)

        Label label = (Label) getNodeByRowColumnIndex(row, col);
        if (label != null) {
            label.setTextFill(color);
        }

        engine.setTextColor(row, col, color);
    }

    public void undoCellColor(String cell) {
        int row = Integer.parseInt(cell.substring(1));  // Extract row number
        int col = getColumnIndex(cell.substring(0, 1)) + 1;  // Extract column letter

        Label label = (Label) getNodeByRowColumnIndex(row, col);
        if (label != null) {
            label.setBackground(null);  // Reset the background color
            label.getStyleClass().add("label");    // Reset the text color, allowing the theme to apply
            engine.undoColor(row,col);
        }
    }

    public void applySorting(String fromCell, String toCell, List<Integer> columnsToSortBy) throws InvalidXMLFormatException {
        try {
            SheetDTO sortedSheet = engine.sortSheet(fromCell, toCell, columnsToSortBy);
            // render gridpane by sortedSheet.
            renderGrid(sortedSheet);

            isSorted = true; // Mark that sorting has been applied
        }
        catch (InvalidXMLFormatException e) {
            throw new InvalidXMLFormatException(e.getMessage());
        }
        catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Save original state of the grid (before sorting)
    private void saveOriginalState() {
        // Save the original cell data and their styles
        originalCells = engine.getSheet().getMapOfCellsDTO(); // Assuming the engine provides this method

        // Save styled labels (e.g., with colors)
        originalStyledLabels = new HashSet<>();
        for (Node node : spreadsheetGridPane.getChildren()) {
            if (node instanceof Label && !((Label) node).getStyleClass().isEmpty()) {
                originalStyledLabels.add((Label) node); // Save labels with custom styles
            }
        }
    }

    // Method to remove sorting and restore the original grid state
    public void removeSorting() {
        if (isSorted) {
            // Re-render the original grid
            renderGrid(engine.getSheet());

            isSorted = false; // Mark that sorting has been removed
        }
    }

    public void removeFiltering() {
        if (isFiltered) {
            renderGrid(engine.getSheet());
            isFiltered = false;
        }
    }


    public void applyFiltering(String fromCell, String toCell, Set<String> selectedWordsSet ) throws InvalidXMLFormatException {
        SheetDTO filteredSheet = engine.filterSheet(fromCell, toCell, selectedWordsSet );
        // render gridpane by sortedSheet.
        renderGrid(filteredSheet);

        isFiltered = true;
    }

    // Restore original cell styles (e.g., colors)
    private void restoreOriginalStyles() {
        // Clear any existing styles
        for (Node node : spreadsheetGridPane.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).getStyleClass().clear(); // Clear styles
            }
        }

        // Re-apply original styles to the labels
        for (Label styledLabel : originalStyledLabels) {
            Label currentLabel = getNodeByRowColumnIndex(
                    GridPane.getRowIndex(styledLabel), GridPane.getColumnIndex(styledLabel));

            if (currentLabel != null) {
                currentLabel.getStyleClass().addAll(styledLabel.getStyleClass());
            }
        }
    }

    private int getColumnIndex(String columnLetter) {
        return columnLetter.toUpperCase().charAt(0) - 'A';
    }

    private Label getNodeByRowColumnIndex(final int row, final int column) {
        for (Node node : spreadsheetGridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) {
                return (Label) node;
            }
        }
        return null;  // Return null if no node is found at the specified position
    }

    public Engine getEngine() {
        return engine;
    }

    @FXML
    public Label getSelectedCellLabel() {
        return selectedCellLabel;
    }

    @FXML
    public void setSelectedCellLabel(String label) {
        selectedCellLabel.setText(label);
    }


    // Method to get the current width of a column
    public double getColumnWidth(int colIndex) {
        if (colIndex >= 0 && colIndex < spreadsheetGridPane.getColumnConstraints().size()) {
            return spreadsheetGridPane.getColumnConstraints().get(colIndex).getPrefWidth();
        }
        return 100; // Default value if column index is invalid
    }

    // Method to get the current height of a row
    public double getRowHeight(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < spreadsheetGridPane.getRowConstraints().size()) {
            return spreadsheetGridPane.getRowConstraints().get(rowIndex).getPrefHeight();
        }
        return 30; // Default value if row index is invalid
    }

    public void setTopController(TopController topController) {
        this.topController = topController;
    }

    public Set<Label> getDependsOnCellLabel() {
        return dependsOnCellLabel;
    }
    public Set<Label> getInfluencingOnCellLabel() {
        return influencingOnCellLabel;
    }

    public void lock(){
        topController.resetLabelsAndText();
    }

    public void setDisabled() {
        spreadsheetGridPane.setMouseTransparent(true);
    }

    public void setEnabled() {
        spreadsheetGridPane.setMouseTransparent(false);
    }

}
