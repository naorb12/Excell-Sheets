package immutable.objects;

import javafx.scene.paint.Color;
import sheet.cell.api.EffectiveValue;
import sheet.coordinate.Coordinate;

import java.util.Set;

public interface CellDTO {

    EffectiveValue getEffectiveValue();

    Coordinate getCoordinate();

    String getOriginalValue();

    int getVersion();

    Set<Coordinate> getDependsOn();

    Set<Coordinate> getInfluencingOn();

    void calculateEffectiveValue(SheetDTO sheet);

    Color getBackgroundColor();

    javafx.scene.paint.Color getForegroundColor();

}
