package center;

import engine.Engine;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.awt.*;
import java.util.Optional;


public class CenterController {

    @FXML
    private GridPane spreadsheetGridPane;

    private Engine engine;

    private SimpleBooleanProperty isFileSelected = new SimpleBooleanProperty(false);

    public void initialize() {
        engine = new Engine();
    }

    @FXML
    public void renderGridPane() {
        isFileSelected.set(true);
        spreadsheetGridPane.getChildren().clear();
        spreadsheetGridPane.getColumnConstraints().clear();
        spreadsheetGridPane.getRowConstraints().clear();

        SheetDTO sheet = engine.getSheet();
        if (sheet == null) {
            System.out.println("No sheet data available to render.");
            return;
        }

        // Get sheet dimensions
        int rows = engine.getSheet().getRowCount();
        int cols = engine.getSheet().getColumnCount();
        int rowHeight = engine.getSheet().getRowHeightUnits();  // Assuming these methods exist
        int colWidth = engine.getSheet().getColumnsWidthUnits();

        // Set row and column constraints based on the size of the sheet
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
            colHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Make label fill the cell
            spreadsheetGridPane.add(colHeader, col, 0); // Place in the first row (index 0)
        }

        // Add row headers (1, 2, 3, etc.)
        for (int row = 1; row <= rows; row++) {
            Label rowHeader = new Label(Integer.toString(row));
            rowHeader.getStyleClass().add("header");
            rowHeader.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Make label fill the cell
            spreadsheetGridPane.add(rowHeader, 0, row); // Place in the first column (index 0)
        }

        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= cols; col++) {
                CellDTO cell = engine.getCell(row, col);
                Label cellLabel = new Label(cell != null ? cell.getEffectiveValue().getValue().toString() : "");

                // Apply the 'cell' style class
                cellLabel.getStyleClass().add("cell");

                // Ensure the label takes up the full size of its cell
                cellLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE); // Allow label to expand fully

                // Ensure the text is aligned and wraps properly
                cellLabel.setWrapText(true);
                cellLabel.setTextOverrun(OverrunStyle.ELLIPSIS);

                spreadsheetGridPane.add(cellLabel, col, row);
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
    }

    public void undoCellBackgroundColor(String cell) {
        int row = Integer.parseInt(cell.substring(1));  // Extract row number
        int col = getColumnIndex(cell.substring(0, 1)) + 1;  // Extract column letter

        Label label = (Label) getNodeByRowColumnIndex(row, col);
        if (label != null) {
            label.setBackground(null);  // Reset the background color
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
}
