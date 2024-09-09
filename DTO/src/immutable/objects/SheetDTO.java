package immutable.objects;

import sheet.coordinate.Coordinate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SheetDTO {


    CellDTO getCellDTO(int row, int column);

    Map<Coordinate, CellDTO> getMapOfCellsDTO();

    int getColumnCount();

    int getRowCount();

    String getName();

    int getVersion();
    
    int getColumnsWidthUnits();

    int getRowHeightUnits();

    List<Coordinate> getRange(String rangeName);

    Map<String, List<Coordinate>> getAllRanges();

    boolean isCoordinateInRange(Coordinate coord);
}
