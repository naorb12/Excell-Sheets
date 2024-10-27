package engine.manager;

import engine.permission.UserPermissions;
import engine.permission.dto.UserPermissionsDTO;
import engine.permission.property.PermissionStatus;
import engine.permission.property.PermissionType;
import exception.CalculationException;
import exception.InvalidXMLFormatException;
import exception.OutOfBoundsException;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import javafx.scene.paint.Color;
import sheet.api.Sheet;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;
import sheet.impl.SheetImpl;
import xml.generated.STLBoundaries;
import xml.generated.STLCell;
import xml.generated.STLRange;
import xml.generated.STLSheet;

import java.io.*;
import java.util.*;

public class SheetManager implements Serializable {

    private static final int MAX_ROWS = 50;
    private static final int MAX_COLS = 20;


    private SheetImpl sheet;
    // Version history
    private Map<Integer, SheetDTO> versionHistory = new HashMap<>();
    // UserPermissions list
    private Map<String, UserPermissions> userPermissionsMap = new HashMap<>();


    public SheetManager(int rowsCount, int colsCount, int rowsHeight, int colsWidth) {
        sheet = new SheetImpl(rowsCount, colsCount, rowsHeight, colsWidth);
    }

    public SheetManager(){
    }
    public SheetManager(SheetImpl sheet) {
        this.sheet = sheet;
    }

    public SheetDTO getSheet() {
        return sheet;
    }

    public Map<String, UserPermissionsDTO> getUserPermissionsMap() {
        Map<String, UserPermissionsDTO> userPermissionsList = new HashMap<>();
        for (UserPermissions userPermissions : userPermissionsMap.values()) {
            userPermissionsList.put(userPermissions.getUserName(), new UserPermissionsDTO(userPermissions));
        }
        return userPermissionsList;
    }

    public void setSheet(SheetImpl sheet) {
        this.sheet = sheet;
    }

    public CellDTO getCell(int row, int col) {
        return (CellDTO) sheet.getCellDTO(row, col);
    }

    public CellDTO getCellDTO(int row, int col) {
        return (CellDTO) sheet.getCellDTO(row, col);
    }

    public boolean isWithinBounds(int row, int column) throws OutOfBoundsException {
        int maxRow = sheet.getRowCount(); // Assuming method to get total rows
        int maxColumn = sheet.getColumnCount(); // Assuming method to get total columns

        if (row <= 0 || row > maxRow || column <= 0 || column > maxColumn) {
            throw new OutOfBoundsException(maxRow, maxColumn, row, column);
        }
        return true;
    }

    public void mapSTLSheet(STLSheet generatedSheet) throws InvalidXMLFormatException {
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
            if(sheet == null)
            {
                sheet = mappedSheet;
            }

            // Step 3: Translate cells and add them to the sheet
            Map<Coordinate, Cell> cells = new HashMap<>();
            for (STLCell generatedCell : generatedSheet.getSTLCells().getSTLCell()) {
                // Validate that the cell is within bounds
                isWithinBounds(generatedCell.getRow(),
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

            sheet = mappedSheet;
            versionHistory = new HashMap<>();

            // Step 5: Save the current version of the sheet (snapshot)
            saveCurrentVersion(createSnapshot());

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

    private void validateAndAddRanges(STLSheet generatedSheet, Sheet sheet) throws InvalidXMLFormatException {
        if (generatedSheet.getSTLRanges() == null || generatedSheet.getSTLRanges().getSTLRange().isEmpty()) {
            return;
        }

        // Step 1: Ensure all ranges have a valid name and are within bounds
        Set<Coordinate> allRangeCoordinates = new HashSet<>();  // To track overlaps
        for (STLRange range : generatedSheet.getSTLRanges().getSTLRange()) {
            String rangeName = range.getName();
            List<Coordinate> rangeCoordinates = new ArrayList<>();

            if (rangeName == null || rangeName.trim().isEmpty()) {
                throw new InvalidXMLFormatException("Range with no valid name found.");
            }

            if(sheet.getAllRanges().containsKey(rangeName)){
                throw new InvalidXMLFormatException("Range with name " + rangeName + " already exists.");
            }
            // Step 2: Validate each boundary and add coordinates
            // Assuming that STLRange contains STLBoundaries objects
            STLBoundaries boundaries = range.getSTLBoundaries();
            rangeCoordinates = validateRange(boundaries.getFrom(), boundaries.getTo());

            // Step 3: Add range to the sheet
            sheet.addRange(rangeName, rangeCoordinates);
        }
    }

    public List<Coordinate> validateRange(String fromCell, String toCell) throws InvalidXMLFormatException {
        List<Coordinate> rangeCoordinates = new ArrayList<>();
        Coordinate fromCoord = parseCoordinate(fromCell.trim().toUpperCase());
        Coordinate toCoord = parseCoordinate(toCell.trim().toUpperCase());

        // Step 1: Check that boundaries are within the sheet bounds
        isWithinBounds(fromCoord.getRow(), fromCoord.getColumn());
        isWithinBounds(toCoord.getRow(), toCoord.getColumn());

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

        return List.copyOf(rangeCoordinates);  // Return an immutable list of the coordinates
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


    public void setCell(int row, int col, String input) {
        try {
            sheet.setCell(row, col, input, this);
            saveCurrentVersion(createSnapshot());
        }
        catch (CalculationException e)
        {
            throw new RuntimeException(e.getMessage());
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public SheetDTO peekVersion(int version) {
        return versionHistory.get(version);
    }

    public void saveCurrentVersion(SheetDTO snapShot) {
        versionHistory.put(sheet.getVersion(), snapShot);
    }

    // Method to create a snapshot of the current sheet state
    public SheetDTO createSnapshot() {
        // Create a deep copy of the current sheet
        SheetImpl snapshot = new SheetImpl(sheet.getRowCount(), sheet.getColumnCount(), sheet.getRowHeightUnits(), sheet.getColumnsWidthUnits());
        snapshot.setName(sheet.getName());
        snapshot.setVersion(sheet.getVersion());

        // Create a deep copy of the activeCells map
        Map<Coordinate, Cell> deepCopiedCells = new HashMap<>();
        for (Map.Entry<Coordinate, Cell> entry : sheet.getMapOfCells().entrySet()) {
            // Copying cells
            Cell cellCopied = entry.getValue();
            Cell cell = new CellImpl(
                    cellCopied.getCoordinate().getRow(),
                    cellCopied.getCoordinate().getColumn(),
                    new String(cellCopied.getOriginalValue()),
                    new EffectiveValueImpl((EffectiveValueImpl) cellCopied.getEffectiveValue()),  // Deep copy the EffectiveValue
                    cellCopied.getVersion(),
                    new HashSet<>(cellCopied.getDependsOn()),
                    new HashSet<>(cellCopied.getInfluencingOn())
            );
            deepCopiedCells.put(entry.getKey(), cell);
        }

        // Create a deep copy of the ranges map
        Map<String, List<Coordinate>> deepCopiedRanges = new HashMap<>();
        for (Map.Entry<String, List<Coordinate>> entry : sheet.getAllRanges().entrySet()) {
            // Deep copy of the list of coordinates for each range
            List<Coordinate> copiedCoordinates = new ArrayList<>(entry.getValue()); // Shallow copy of the list (the coordinates are immutable or we assume they won't change)
            deepCopiedRanges.put(entry.getKey(), copiedCoordinates);
        }
        snapshot.setRanges(deepCopiedRanges);  // Ensure SheetImpl has a setRanges method for setting ranges

        snapshot.setCells(deepCopiedCells);

        return (SheetDTO) snapshot;
    }


    public int countAmountOfCellsChangedFromPreviousVersions(SheetDTO sheetVersion) {
        int currentVersion = sheetVersion.getVersion();
        int count = 0;

        // Get the previous version from the history
        SheetDTO previousVersion = versionHistory.get(currentVersion - 1);
        if (previousVersion == null) {
            // If there's no previous version, all cells in the current version are considered "new"
            return sheetVersion.getMapOfCellsDTO().size();
        }

        // Compare each cell in the current version with the corresponding cell in the previous version
        for (Map.Entry<Coordinate, CellDTO> entry : sheetVersion.getMapOfCellsDTO().entrySet()) {
            Coordinate coord = entry.getKey();
            CellDTO currentCell = entry.getValue();
            CellDTO previousCell = previousVersion.getMapOfCellsDTO().get(coord);

            // If the cell exists in both versions but has different version numbers, it has changed
            if (previousCell == null || currentCell.getVersion() != previousCell.getVersion()) {
                count++;
            }
        }

        return count;
    }

    // EX 1 BONUS
//    public void saveStateToFile(String filePath) {
//        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
//            oos.writeObject(this);
//            oos.writeObject(sheet); // Serialize the static sheet separately
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to save state to file: " + e.getMessage());
//        }
//    }
//
//    public static SheetManager loadStateFromFile(String filePath) {
//        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
//            SheetManager sheetManager = (SheetManager) ois.readObject();
//            sheet = (SheetImpl) ois.readObject(); // Deserialize the static sheet separately
//            return sheetManager;
//        } catch (IOException | ClassNotFoundException e) {
//            throw new RuntimeException("Failed to load engine state from file: " + e.getMessage(), e);
//        }
//    }

    public Map<Integer, SheetDTO> getVersionHistory() {
        return versionHistory;
    }

    public List<Coordinate> createNewRange(String rangeName, String fromCell, String toCell) throws InvalidXMLFormatException {
        List<Coordinate> range = new ArrayList<>();
        range = validateRange(fromCell, toCell);
        sheet.addRange(rangeName, range);
        return range;
    }

    public void removeRange(String rangeToRemove){
        sheet.removeRange(rangeToRemove);
    }

    public SheetDTO sortSheet(String fromCell, String toCell, List<Integer> columnsToSortBy) throws InvalidXMLFormatException {
        try {
            List<Coordinate> range = validateRange(fromCell, toCell);
            SheetDTO sortedSheet = sheet.sortSheet(range, columnsToSortBy);

            return sortedSheet;
        }
        catch (InvalidXMLFormatException e) {
            throw new InvalidXMLFormatException(e.getMessage());
        }
        catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public SheetDTO filterSheet(String fromCell, String toCell, Set<String> selectedWordsSet ) throws InvalidXMLFormatException {
        List<Coordinate> range = validateRange(fromCell, toCell);
        SheetDTO filteredSheet = sheet.filterSheet(range, selectedWordsSet );

        return filteredSheet;
    }

    public SheetDTO applyDynamicAnalysis(Coordinate coordinate, Number newValue) {
        SheetDTO sheetDTO = sheet.applyDynamicAnalysis(coordinate, newValue, this);

        return sheetDTO;
    }

    public void setBackgroundColor(int row, int col, Color color) {
        sheet.setBackgroundColor(row, col, color);
    }

    public void setTextColor(int row, int col, Color color) {
        sheet.setTextColor(row, col, color);
    }

    public void undoColor(int row, int col) {
        sheet.undoColor(row, col);
    }

    public Set<String> getWordsFromColumnAndRange(String column, String fromCellFieldFilter, String toCellFieldFilter) throws InvalidXMLFormatException {
        List<Coordinate> range = validateRange(fromCellFieldFilter, toCellFieldFilter);
        return sheet.getWordsFromColumnAndRange(column, range);
    }


    public List<Double> getRangeNumericValues(List<Coordinate> range) {
        return sheet.getRangeNumericValues(range);
    }


    public String getOwner(){
        return sheet.getOwner();
    }

    public void setOwner(String owner) {
        sheet.setOwner(owner);
        UserPermissions ownerPermission = new UserPermissions(owner, PermissionType.OWNER, PermissionStatus.APPROVED);
        userPermissionsMap.put(owner, ownerPermission);
    }

    public void addUserPermissions(UserPermissions userPermissions) {
        userPermissionsMap.put(userPermissions.getUserName(), userPermissions);
    }

    public void resetUserPermissions() {
        this.userPermissionsMap = new HashMap<>();
    }

    public void handlePermissions(String userName, PermissionStatus status) {
        userPermissionsMap.get(userName).setUserPermissionStatus(status);
    }
}
