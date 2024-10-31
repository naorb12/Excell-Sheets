package engine;

import engine.manager.SheetManager;
import immutable.objects.SheetManagerDTO;
import exception.InvalidXMLFormatException;
import exception.OutOfBoundsException;
import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.Coordinate;
import sheet.impl.SheetImpl;
import xml.generated.STLCell;
import xml.generated.STLRange;
import xml.generated.STLSheet;

import java.util.*;

public class ShticellEngine {

    private static final int MAX_ROWS = 50;
    private static final int MAX_COLS = 20;

    Map<String, SheetManager> sheetManagerMap = new HashMap<String, SheetManager>();

    public Map<String, SheetManager> getSheetManagerMap() {
        return sheetManagerMap;
    }

    public void setSheetManagerMap(Map<String, SheetManager> sheetManagerMap) {
        this.sheetManagerMap = sheetManagerMap;
    }

    public static boolean isWithinBounds(STLSheet generatedSheet, String sheetName, int row, int column) throws Exception {

        int maxRow = generatedSheet.getSTLLayout().getRows();
        int maxColumn = generatedSheet.getSTLLayout().getColumns();

        if (row <= 0 || row > maxRow || column <= 0 || column > maxColumn) {
            throw new OutOfBoundsException(maxRow, maxColumn, row, column);
        }
        return true;
    }


    public void mapSTLSheet(String sheetName, STLSheet generatedSheet, String owner) throws InvalidXMLFormatException {
        try {
            // Step 1: Validate sheet dimensions
            validateSheetDimensions(generatedSheet);
            // Step 2: Initialize the custom SheetImpl object with the validated size
            SheetImpl mappedSheet;
            mappedSheet = new SheetImpl(generatedSheet.getSTLLayout().getRows(),
                    generatedSheet.getSTLLayout().getColumns(),
                    generatedSheet.getSTLLayout().getSTLSize().getRowsHeightUnits(),
                    generatedSheet.getSTLLayout().getSTLSize().getColumnWidthUnits());
            mappedSheet.setName(generatedSheet.getName());

            // Step 3: Translate cells and add them to the sheet
            Map<Coordinate, Cell> cells = new HashMap<>();
            for (STLCell generatedCell : generatedSheet.getSTLCells().getSTLCell()) {
                isWithinBounds(generatedSheet, sheetName, generatedCell.getRow(),
                        generatedCell.getColumn().toUpperCase().trim().charAt(0) - 'A' + 1);

                // Translate and validate the cell
                Cell cell = translateSTLCellToCell(generatedCell);

                // Store the cell in the sheet's cell map
                Coordinate coord = new Coordinate(cell.getCoordinate().getRow(), cell.getCoordinate().getColumn());
                cells.put(coord, cell);
            }

            // Step 4: Handle Ranges - Validate and add ranges
            validateAndAddRanges(generatedSheet, mappedSheet);

            mappedSheet.setCells(cells);

            if (sheetManagerMap.containsKey(sheetName)) {
                sheetManagerMap.get(sheetName).resetUserPermissions();
            }
            // Step 5: Store the sheet in the appropriate SheetManager
            SheetManager sheetManager = sheetManagerMap.computeIfAbsent(sheetName, k -> new SheetManager());
            sheetManager.setSheet(mappedSheet);
            sheetManager.setOwner(owner);
            // Step 6: Save the current version of the sheet in the SheetManager's version history
            sheetManager.saveCurrentVersion(sheetManager.createSnapshot());

        } catch (OutOfBoundsException e) {
            throw new RuntimeException(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void validateSheetDimensions(STLSheet generatedSheet) throws InvalidXMLFormatException {
        int rows = generatedSheet.getSTLLayout().getRows();
        int columns = generatedSheet.getSTLLayout().getColumns();
        int rowHeight = generatedSheet.getSTLLayout().getSTLSize().getRowsHeightUnits();
        int columnWidth = generatedSheet.getSTLLayout().getSTLSize().getColumnWidthUnits();

        if (rows < 1 || rows > MAX_ROWS || columns < 1 || columns > MAX_COLS) {
            throw new InvalidXMLFormatException("Sheet dimensions are out of bounds: " + rows + "x" + columns + ". Please enter rows from 1 to " + MAX_ROWS + " ,and columns from 1 to " + MAX_COLS);
        }
        if(rowHeight <= 0 || columnWidth <= 0){
            throw new InvalidXMLFormatException("Cell dimensions are non-existing: " + rowHeight + "," + columnWidth);
        }
    }

    private Cell translateSTLCellToCell(STLCell generatedCell) throws InvalidXMLFormatException {
        Cell cell = new CellImpl(generatedCell.getRow() , generatedCell.getColumn().toUpperCase().trim().charAt(0) - 'A' + 1, generatedCell.getSTLOriginalValue());

        // Set the original value from the generated XML object
        cell.setOriginalValue(generatedCell.getSTLOriginalValue());

        return cell;
    }

    private void validateAndAddRanges(STLSheet generatedSheet, Sheet sheet) throws Exception {
        // Ensure that the STL ranges exist in the generated sheet
        if (generatedSheet.getSTLRanges() == null || generatedSheet.getSTLRanges().getSTLRange().isEmpty()) {
            return; // No ranges to process, exit early
        }

        // Iterate through each range from the generated sheet
        for (STLRange range : generatedSheet.getSTLRanges().getSTLRange()) {
            String rangeName = range.getName();

            if (rangeName == null || rangeName.trim().isEmpty()) {
                throw new InvalidXMLFormatException("Range with no valid name found.");
            }

            // Validate and extract the coordinates for the range
            List<Coordinate> rangeCoordinates = validateRange(generatedSheet, sheet.getName(), range.getSTLBoundaries().getFrom(), range.getSTLBoundaries().getTo());

            // Check if the range already exists in the sheet
            if (sheet.getAllRanges().containsKey(rangeName)) {
                throw new InvalidXMLFormatException("Range with name " + rangeName + " already exists in the sheet.");
            }

            // Add the range to the current sheet
            sheet.addRange(rangeName, rangeCoordinates);
        }
    }

    private List<Coordinate> validateRange(STLSheet generatedSheet, String sheetName, String fromCell, String toCell) throws Exception {
        List<Coordinate> rangeCoordinates = new ArrayList<>();
        Coordinate fromCoord = parseCoordinate(fromCell.trim().toUpperCase());
        Coordinate toCoord = parseCoordinate(toCell.trim().toUpperCase());

        // Step 1: Check that boundaries are within the sheet bounds
        isWithinBounds(generatedSheet, sheetName, fromCoord.getRow(), fromCoord.getColumn());
        isWithinBounds(generatedSheet, sheetName, toCoord.getRow(), toCoord.getColumn());

        // Step 2: Determine valid range type (left to right, top to bottom, or top left to bottom right)
        // Left to right (same row, from left column to right column)
        if (fromCoord.getRow() == toCoord.getRow() && fromCoord.getColumn() <= toCoord.getColumn()) {
            for (int col = fromCoord.getColumn(); col <= toCoord.getColumn(); col++) {
                rangeCoordinates.add(new Coordinate(fromCoord.getRow(), col));
            }
        }
        // Top to bottom (same column, from top row to bottom row)
        else if (fromCoord.getColumn() == toCoord.getColumn() && fromCoord.getRow() <= toCoord.getRow()) {
            for (int row = fromCoord.getRow(); row <= toCoord.getRow(); row++) {
                rangeCoordinates.add(new Coordinate(row, fromCoord.getColumn()));
            }
        }
        // Top left to bottom right (diagonal range: row and column both increase)
        else if (fromCoord.getRow() <= toCoord.getRow() && fromCoord.getColumn() <= toCoord.getColumn()) {
            for (int row = fromCoord.getRow(); row <= toCoord.getRow(); row++) {
                for (int col = fromCoord.getColumn(); col <= toCoord.getColumn(); col++) {
                    rangeCoordinates.add(new Coordinate(row, col));
                }
            }
        }
        // Step 3: Invalid case (right to left, bottom to top, etc.)
        else {
            throw new InvalidXMLFormatException("Invalid range boundaries: " + fromCell + " to " + toCell);
        }

        return List.copyOf(rangeCoordinates);  // Return immutable list of coordinates
    }


    /**
     * Parses a string representation of a coordinate (e.g., "A1", "B2")
     * into a Coordinate object.
     */
    private Coordinate parseCoordinate(String coordStr) throws InvalidXMLFormatException {
        try {
            // Assume the string is in the format "A1", "B5", etc.
            char columnChar = coordStr.toUpperCase().trim().charAt(0);
            int row = Integer.parseInt(coordStr.substring(1));

            int column = columnChar - 'A' + 1;  // Convert 'A' to 1, 'B' to 2, etc.

            return new Coordinate(row, column);
        } catch (Exception e) {
            throw new InvalidXMLFormatException("Invalid coordinate format: " + coordStr);
        }
    }

    public SheetManagerDTO getSheetManagerDTO(String sheetName) {
        SheetManagerDTO sheetManagerDTO = new SheetManagerDTO(this.getSheetManagerMap().get(sheetName));
        return sheetManagerDTO;
    }
}
