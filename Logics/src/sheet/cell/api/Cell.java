package sheet.cell.api;

import immutable.objects.SheetDTO;
import javafx.scene.paint.Color;
import sheet.coordinate.Coordinate;

import java.util.Set;

public interface Cell {

    Coordinate getCoordinate();
    String getOriginalValue();
    void setCellOriginalValue(String value);

    Color getBackgroundColor();

    Color getForegroundColor();

    void setBackgroundColor(javafx.scene.paint.Color backgroundColor);

    void setForegroundColor(javafx.scene.paint.Color foregroundColor);

    EffectiveValue getEffectiveValue();
    void calculateEffectiveValue(SheetDTO sheet);

    int getVersion();
    Set<Coordinate> getDependsOn();
    Set<Coordinate> getInfluencingOn();
    void setOriginalValue(String stlOriginalValue);

    void setDependsOn(Set<Coordinate> dependencies);

    void incrementVersionNumber();

    void setCoordinate(Coordinate newCoordinate);
}
