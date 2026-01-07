package deserializer;

import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorTypeAdapter extends TypeAdapter<Color> {
    @Override
    public void write(JsonWriter out, Color color) throws IOException {
        if (color == null) {
            out.nullValue();
        } else {
            out.value(color.toString()); // Converts to string (e.g., "#FF0000")
        }
    }

    @Override
    public Color read(JsonReader in) throws IOException {
        // Check if the JSON is a string or object
        if (in.peek() == JsonToken.STRING) {
            // Simple color string, e.g., "#FF0000"
            String colorString = in.nextString();
            return Color.web(colorString); // Convert string to Color
        } else if (in.peek() == JsonToken.BEGIN_OBJECT) {
            // JSON object format, e.g., { "red": 1.0, "green": 0.0, "blue": 0.0 }
            in.beginObject();
            double red = 0.0, green = 0.0, blue = 0.0, opacity = 1.0;
            while (in.hasNext()) {
                switch (in.nextName()) {
                    case "red":
                        red = in.nextDouble();
                        break;
                    case "green":
                        green = in.nextDouble();
                        break;
                    case "blue":
                        blue = in.nextDouble();
                        break;
                    case "opacity":
                        opacity = in.nextDouble();
                        break;
                }
            }
            in.endObject();
            return new Color(red, green, blue, opacity); // Convert RGB values to Color
        } else {
            throw new JsonIOException("Unexpected JSON format for Color");
        }
    }
}
