package deserializer;

import com.google.gson.*;
import javafx.scene.paint.Color;
import java.lang.reflect.Type;

public class ColorTypeDeserializer implements JsonSerializer<Color>, JsonDeserializer<Color> {

    @Override
    public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
        // Serialize the Color object as a JSON string
        return new JsonPrimitive(src.toString()); // Returns color as string, e.g., "0xff0000ff"
    }

    @Override
    public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Deserialize the JSON string back to a Color object
        return Color.valueOf(json.getAsString()); // Parses color string back to Color object
    }
}
