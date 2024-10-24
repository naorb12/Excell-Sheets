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

        String name = jsonObject.get("name").getAsString();
        int version = jsonObject.get("version").getAsInt();
        int columnCount = jsonObject.get("columnsCount").getAsInt();
        int rowCount = jsonObject.get("rowsCount").getAsInt();
        int columnsWidthUnits = jsonObject.get("columnsWidth").getAsInt();
        int rowHeightUnits = jsonObject.get("rowsHeight").getAsInt();

        // Deserialize mapOfCells (which will be a Map of CellImpl)
        JsonObject mapOfCellsJson = jsonObject.getAsJsonObject("activeCells");
        Map<Coordinate, Cell> mapOfCells = new HashMap<>();

        // Iterate over the entries in the JSON object to manually deserialize each Coordinate-Cell pair
        for (Map.Entry<String, JsonElement> entry : mapOfCellsJson.entrySet()) {
            // Parse the coordinate string (assuming it's a string) and convert it to a Coordinate object
            String coordinateString = entry.getKey();
            Coordinate coordinate = parseCoordinate(coordinateString);  // Custom method to parse coordinate

            // Deserialize the cell as CellDTOImpl
            Cell cell = context.deserialize(entry.getValue(), CellImpl.class);  // Assuming CellImpl is the implementation
            mapOfCells.put(cell.getCoordinate(), cell);
        }

        // Deserialize ranges (as usual)
        JsonObject rangesJson = jsonObject.getAsJsonObject("ranges");
        Map<String, List<Coordinate>> ranges = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : rangesJson.entrySet()) {
            List<Coordinate> coordinates = new ArrayList<>();
            for (JsonElement coordElement : entry.getValue().getAsJsonArray()) {
                Coordinate coord = context.deserialize(coordElement, Coordinate.class);
                coordinates.add(coord);
            }
            ranges.put(entry.getKey(), coordinates);
        }

        // Create and return the SheetImpl instance
        return new SheetImpl(name, version, columnCount, rowCount, columnsWidthUnits, rowHeightUnits, mapOfCells, ranges);
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
