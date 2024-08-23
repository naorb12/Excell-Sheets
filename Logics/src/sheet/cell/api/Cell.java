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
    Set<Coordinate> getDependsOn();
    Set<Coordinate> getInfluencingOn();
    void setOriginalValue(String stlOriginalValue);

    void setDependsOn(Set<Coordinate> dependencies);

    void incrementVersionNumber();
}
