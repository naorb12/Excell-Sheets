package immutable.objects;

import java.util.Optional;

public interface SheetDTO {

    CellDTO getCellDTO(int row, int column);

    int getColumnCount();

    int getRowCount();

    String getName();

    int getVersion();
    
    int getColumnsWidthUnits();

    int getRowHeightUnits();
}
