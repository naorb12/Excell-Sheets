package immutable.objects;

import sheet.coordinate.Coordinate;

import java.util.Map;
import java.util.Optional;

public interface SheetDTO {

    // Add a method to retrieve a specific version of the sheet
    SheetDTO peekVersion(int version);

    CellDTO getCellDTO(int row, int column);

    Map<Coordinate, CellDTO> getMapOfCellsDTO();

    int getColumnCount();

    int getRowCount();

    String getName();

    int getVersion();
    
    int getColumnsWidthUnits();

    int getRowHeightUnits();
}
