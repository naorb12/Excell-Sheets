package sheet.cell.api;

import sheet.coordinate.Coordinate;

import java.util.Set;

public interface Cell {

    Coordinate getCoordinate();
    String getOriginalValue();
    void setCellOriginalValue(String value);
    EffectiveValue getEffectiveValue();
    void calculateEffectiveValue();

    boolean isFormula();

    int getVersion();
    Set<Cell> getDependsOn();
    Set<Cell> getInfluencingOn();
    void setOriginalValue(String stlOriginalValue);

    void setDependsOn(Set<Cell> dependencies);
}
