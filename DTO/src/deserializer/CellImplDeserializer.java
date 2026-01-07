package deserializer;

import com.google.gson.*;
import immutable.objects.CellDTO;
import javafx.scene.paint.Color;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellImpl;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import java.util.HashSet;

public class CellImplDeserializer implements JsonDeserializer<CellImpl> {

    @Override
    public CellImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Deserialize the coordinate
        Coordinate coordinate = context.deserialize(jsonObject.get("coordinate"), Coordinate.class);

        // Deserialize the original value
        String originalValue = jsonObject.has("originalValue") && !jsonObject.get("originalValue").isJsonNull() ? jsonObject.get("originalValue").getAsString() : null; ;

        // Deserialize the effective value
        EffectiveValue effectiveValue = context.deserialize(jsonObject.get("effectiveValue"), EffectiveValueImpl.class);

        // Deserialize the version number
        int versionNumber = jsonObject.get("versionNumber").getAsInt();

        // Deserialize dependsOn and influencingOn sets
        Set<Coordinate> dependsOn = deserializeCoordinateSet(jsonObject.getAsJsonArray("dependsOn"));
        Set<Coordinate> influencingOn = deserializeCoordinateSet(jsonObject.getAsJsonArray("influencingOn"));

        // Deserialize the colors (with ColorTypeAdapter)
        Color backgroundColor = jsonObject.has("backgroundColor") && !jsonObject.get("backgroundColor").isJsonNull()
                ? context.deserialize(jsonObject.get("backgroundColor"), Color.class)
                : null;

        Color foregroundColor = jsonObject.has("foregroundColor") && !jsonObject.get("foregroundColor").isJsonNull()
                ? context.deserialize(jsonObject.get("foregroundColor"), Color.class)
                : null;

        String userNameUpdated = jsonObject.has("userNameUpdated") && !jsonObject.get("userNameUpdated").isJsonNull() ? jsonObject.get("userNameUpdated").getAsString() : null; ;


        // Create and populate the CellImpl instance
        CellImpl cell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), originalValue, effectiveValue, versionNumber, dependsOn, influencingOn, userNameUpdated);
        cell.setBackgroundColor(backgroundColor);
        cell.setForegroundColor(foregroundColor);

        return cell;
    }

    private Set<Coordinate> deserializeCoordinateSet(JsonArray array) {
        Set<Coordinate> coordinates = new HashSet<>();
        if (array != null) {
            for (JsonElement element : array) {
                JsonObject coordObj = element.getAsJsonObject();
                coordinates.add(parseCoordinate(coordObj));
            }
        }
        return coordinates;
    }

    // Helper method to parse a JsonObject into a Coordinate object
    private Coordinate parseCoordinate(JsonObject coordObj) {
        int row = coordObj.get("row").getAsInt();
        int column = coordObj.get("column").getAsInt();
        return new Coordinate(row, column);
    }
}
