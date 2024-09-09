package center;

import engine.Engine;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.util.Optional;

public class CenterController {

    @FXML
    private GridPane spreadsheetGridPane;

    private Engine engine;

    public void initialize() {
        engine = new Engine();
    }

    @FXML
    public void renderGridPane() {
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
        for (int i = 0; i < cols; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPrefWidth(colWidth);
            spreadsheetGridPane.getColumnConstraints().add(colConstraints);
        }

        for (int i = 0; i < rows; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(rowHeight);
            spreadsheetGridPane.getRowConstraints().add(rowConstraints);
        }

        // Add column headers (A, B, C, etc.)
        for (int col = 1; col <= cols; col++) {
            Label colHeader = new Label(Character.toString((char) ('A' + col - 1)));
            colHeader.setStyle("-fx-border-color: black; -fx-alignment: center;");
            spreadsheetGridPane.add(colHeader, col, 0); // Place in the first row (index 0)
        }

        // Add row headers (1, 2, 3, etc.)
        for (int row = 1; row <= rows; row++) {
            Label rowHeader = new Label(Integer.toString(row));
            rowHeader.setStyle("-fx-border-color: black; -fx-alignment: center;");
            spreadsheetGridPane.add(rowHeader, 0, row); // Place in the first column (index 0)
        }

        // Add the cells (with empty cells as well)
        for (int row = 1; row <= rows; row++) {
            for (int col = 1; col <= cols; col++) {
                CellDTO cell = engine.getCell(row, col);
                Label cellLabel = new Label(cell != null ? cell.getEffectiveValue().getValue().toString() : "");
                cellLabel.setStyle("-fx-border-color: black; -fx-alignment: center;");
                spreadsheetGridPane.add(cellLabel, col, row);
            }
        }
    }

    public Engine getEngine() {
        return engine;
    }
}
