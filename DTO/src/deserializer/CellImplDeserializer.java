package deserializer;

import com.google.gson.*;
import javafx.scene.paint.Color;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellImpl;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;

import java.lang.reflect.Type;
import java.util.Set;

public class CellImplDeserializer implements JsonDeserializer<CellImpl> {

    @Override
    public CellImpl deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Deserialize the coordinate
        Coordinate coordinate = context.deserialize(jsonObject.get("coordinate"), Coordinate.class);

        // Deserialize the original value
        String originalValue = jsonObject.get("originalValue").getAsString();

        // Deserialize the effective value (which is an interface)
        EffectiveValue effectiveValue = context.deserialize(jsonObject.get("effectiveValue"), EffectiveValueImpl.class);

        // Deserialize the version number
        int versionNumber = jsonObject.get("versionNumber").getAsInt();

        // Deserialize dependsOn and influencingOn sets
        Set<Coordinate> dependsOn = context.deserialize(jsonObject.get("dependsOn"), Set.class);
        Set<Coordinate> influencingOn = context.deserialize(jsonObject.get("influencingOn"), Set.class);

        // Deserialize the colors (if present)
        Color backgroundColor = jsonObject.has("backgroundColor") && !jsonObject.get("backgroundColor").isJsonNull()
                ? context.deserialize(jsonObject.get("backgroundColor"), Color.class)
                : null;

        Color foregroundColor = jsonObject.has("foregroundColor") && !jsonObject.get("foregroundColor").isJsonNull()
                ? context.deserialize(jsonObject.get("foregroundColor"), Color.class)
                : null;

        // Create the CellImpl instance
        CellImpl cell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), originalValue, effectiveValue, versionNumber, dependsOn, influencingOn);

        // Set the background and foreground colors
        cell.setBackgroundColor(backgroundColor);
        cell.setForegroundColor(foregroundColor);

        return cell;
    }


}
