package sheet.api;

import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.coordinate.Coordinate;

import java.util.Map;
import java.util.Optional;

public interface Sheet {

    // Add a method to save the current version of the sheet
    void saveCurrentVersion();

    // Method to create a snapshot of the current sheet state
    SheetDTO createSnapshot();

    Map<Coordinate, Cell> getMapOfCells();

    Cell getCell(int row, int column);

    int getColumnCount();

    int getRowCount();

    int getColumnsWidthUnits();

    int getRowHeightUnits();

    String getName();

    int getVersion();

    void setName(String name);

    void setCells(Map<Coordinate, Cell> cells);

    void setCell(int row, int col, String input);

    void incrementVersionForCellAndInfluences(Coordinate coordinate);

    void incrementVersion();
}
