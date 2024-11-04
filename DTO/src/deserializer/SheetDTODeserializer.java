package deserializer;

import com.google.gson.*;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.Coordinate;
import sheet.impl.SheetImpl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SheetDTODeserializer implements JsonDeserializer<SheetDTO> {

    @Override
    public SheetDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        // Name field (required)
        String name = jsonObject.has("name") && !jsonObject.get("name").isJsonNull() ?
                jsonObject.get("name").getAsString() : "Unnamed Sheet";
        // Owner field (optional)
        String owner = jsonObject.has("owner") && !jsonObject.get("owner").isJsonNull() ?
                jsonObject.get("owner").getAsString() : null;
        // Version (optional, default 0 if missing)
        int version = jsonObject.has("version") && !jsonObject.get("version").isJsonNull() ?
                jsonObject.get("version").getAsInt() : 0;
        // Column and Row counts (default 0 if missing)
        int columnCount = jsonObject.has("columnsCount") && !jsonObject.get("columnsCount").isJsonNull() ?
                jsonObject.get("columnsCount").getAsInt() : 0;
        int rowCount = jsonObject.has("rowsCount") && !jsonObject.get("rowsCount").isJsonNull() ?
                jsonObject.get("rowsCount").getAsInt() : 0;
        // Column width and row height units (default 0 if missing)
        int columnsWidthUnits = jsonObject.has("columnsWidth") && !jsonObject.get("columnsWidth").isJsonNull() ?
                jsonObject.get("columnsWidth").getAsInt() : 0;
        int rowHeightUnits = jsonObject.has("rowsHeight") && !jsonObject.get("rowsHeight").isJsonNull() ?
                jsonObject.get("rowsHeight").getAsInt() : 0;

        // Deserialize mapOfCells (which is a Map of CellImpl)
        Map<Coordinate, Cell> mapOfCells = new HashMap<>();
        if (jsonObject.has("activeCells") && !jsonObject.get("activeCells").isJsonNull()) {
            JsonObject mapOfCellsJson = jsonObject.getAsJsonObject("activeCells");

            // Iterate over entries in JSON object for each Coordinate-Cell pair
            for (Map.Entry<String, JsonElement> entry : mapOfCellsJson.entrySet()) {
                String coordinateString = entry.getKey();
                Coordinate coordinate = parseCoordinate(coordinateString);  // Custom method to parse coordinates

                // Deserialize the cell as CellImpl
                Cell cell = context.deserialize(entry.getValue(), CellImpl.class);
                mapOfCells.put(coordinate, cell);
            }
        }

        // Deserialize ranges (which is a Map of String -> List of Coordinates)
        Map<String, List<Coordinate>> ranges = new HashMap<>();
        if (jsonObject.has("ranges") && !jsonObject.get("ranges").isJsonNull()) {
            JsonObject rangesJson = jsonObject.getAsJsonObject("ranges");

            for (Map.Entry<String, JsonElement> entry : rangesJson.entrySet()) {
                List<Coordinate> coordinates = new ArrayList<>();
                for (JsonElement coordElement : entry.getValue().getAsJsonArray()) {
                    Coordinate coord = context.deserialize(coordElement, Coordinate.class);
                    coordinates.add(coord);
                }
                ranges.put(entry.getKey(), coordinates);
            }
        }

        // Create and return the SheetImpl instance
        return new SheetImpl(name, version, columnCount, rowCount, columnsWidthUnits, rowHeightUnits, mapOfCells, ranges, owner);
    }

    private Coordinate parseCoordinate(String coordinateStr) {
        // Remove the parentheses first
        String cleanStr = coordinateStr.replaceAll("[()]", "");

        // Extract the column (as a letter) and the row (as a number)
        char columnChar = cleanStr.charAt(0);
        int row = Integer.parseInt(cleanStr.substring(1));

        // Convert the column letter ('A' = 1, 'B' = 2, ..., 'C' = 3, etc.)
        int column = columnChar - 'A' + 1;

        return new Coordinate(row, column);
    }

}
