package immutable.objects;

import sheet.coordinate.Coordinate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface SheetDTO {


    CellDTO getCellDTO(int row, int column);

    Map<Coordinate, CellDTO> getMapOfCellsDTO();

    int getColumnCount();

    int getRowCount();

    String getName();

    String getOwner();

    int getVersion();
    
    int getColumnsWidthUnits();

    int getRowHeightUnits();

    List<Coordinate> getRange(String rangeName);

    Map<String, List<Coordinate>> getAllRanges();

    boolean isCoordinateInRange(Coordinate coord);

    Set<String> getWordsFromColumnAndRange(String column, List<Coordinate> range);

    List<Double> getRangeNumericValues(List<Coordinate> range);

}
