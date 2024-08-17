package sheet.cell.api;

import sheet.api.EffectiveValue;
import sheet.coordinate.Coordinate;

import java.util.List;
import java.util.Set;

public interface Cell {

    Coordinate getCoordinate();
    String getOriginalValue();
    void setCellOriginalValue(String value);
    EffectiveValue getEffectiveValue();
    void calculateEffectiveValue();
    int getVersion();
    Set<Cell> getDependsOn();
    Set<Cell> getInfluencingOn();
    void setOriginalValue(String stlOriginalValue);

    void setEffectiveValue(EffectiveValue effectiveValue);
}
