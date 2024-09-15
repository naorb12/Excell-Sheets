package sheet.api;

import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.coordinate.Coordinate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Sheet {



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

    void incrementVersion();

    void setVersion(int version);

    // Add a new range by name
    void addRange(String rangeName, List<Coordinate> coordinates);

    void setRanges(Map<String, List<Coordinate>> newRanges);

    // Delete a range by name
    void removeRange(String rangeName);

    // Retrieve a specific range by name
    List<Coordinate> getRange(String rangeName);

    // Retrieve all ranges
    Map<String, List<Coordinate>> getAllRanges();

    // Check if a specific coordinate is part of any range
    boolean isCoordinateInRange(Coordinate coord);

}
