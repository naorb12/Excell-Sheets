package sheet.cell.api;

import immutable.objects.SheetDTO;
import sheet.api.Sheet;
import sheet.coordinate.Coordinate;

import java.util.Set;

public interface Cell {

    Coordinate getCoordinate();
    String getOriginalValue();
    void setCellOriginalValue(String value);
    EffectiveValue getEffectiveValue();
    void calculateEffectiveValue(SheetDTO sheet);

    int getVersion();
    Set<Cell> getDependsOn();
    Set<Cell> getInfluencingOn();
    void setOriginalValue(String stlOriginalValue);

    void setDependsOn(Set<Cell> dependencies);
}
