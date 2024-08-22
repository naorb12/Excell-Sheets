package immutable.objects;

import sheet.cell.api.EffectiveValue;
import sheet.cell.api.Cell;
import sheet.coordinate.Coordinate;

import java.util.Set;

public interface CellDTO {

    EffectiveValue getEffectiveValue();

    Coordinate getCoordinate();

    String getOriginalValue();

    int getVersion();

    Set<Cell> getDependsOn();

    Set<Cell> getInfluencingOn();

    void calculateEffectiveValue(SheetDTO sheet);

}
