package engine;

import exception.CalculationException;
import exception.InvalidXMLFormatException;
import exception.OutOfBoundsException;
import expression.parser.FunctionParser;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;
import sheet.impl.SheetImpl;
import xml.generated.STLCell;
import xml.generated.STLSheet;

import java.io.*;
import java.util.*;

public class Engine implements Serializable {
    private static SheetImpl sheet;
    // Version history
    private Map<Integer, SheetDTO> versionHistory = new HashMap<>();

    public Engine(int rowsCount, int colsCount, int rowsHeight, int colsWidth) {
        sheet = new SheetImpl(rowsCount, colsCount, rowsHeight, colsWidth);
    }

    public Engine(){

    }
    public Engine(SheetImpl sheet) {
        this.sheet = sheet;
    }

    public SheetDTO getSheet() {
        return sheet;
    }


    public CellDTO getCell(int row, int col) {
        return (CellDTO) sheet.getCellDTO(row, col);
    }

    public static CellDTO getCellDTO(int row, int col) {
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
            // Validate sheet dimensions
            validateSheetDimensions(generatedSheet);

            // Initialize the custom SheetImpl object with the validated size
            sheet = new SheetImpl(generatedSheet.getSTLLayout().getRows(), generatedSheet.getSTLLayout().getColumns(),
                    generatedSheet.getSTLLayout().getSTLSize().getRowsHeightUnits(), generatedSheet.getSTLLayout().getSTLSize().getColumnWidthUnits());
            sheet.setName(generatedSheet.getName());

            Map<Coordinate, Cell> cells = new HashMap<>();
            for (STLCell generatedCell : generatedSheet.getSTLCells().getSTLCell()) {
                // Validate that the cell is within the sheet's bounds
                isWithinBounds(generatedCell.getRow(), generatedCell.getColumn().toUpperCase().trim().charAt(0) - 'A' + 1);

                // Translate and validate the cell
                Cell cell = translateSTLCellToCell(generatedCell);

                // Store the cell in the sheet's cell map
                Coordinate coord = new Coordinate(cell.getCoordinate().getRow(), cell.getCoordinate().getColumn());
                cells.put(coord, cell);
            }
            sheet.setCells(cells);
            saveCurrentVersion(createSnapshot());

        } catch (OutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateSheetDimensions(STLSheet generatedSheet) throws InvalidXMLFormatException {
        int rows = generatedSheet.getSTLLayout().getRows();
        int columns = generatedSheet.getSTLLayout().getColumns();
        int rowHeight = generatedSheet.getSTLLayout().getSTLSize().getRowsHeightUnits();
        int columnWidth = generatedSheet.getSTLLayout().getSTLSize().getColumnWidthUnits();

        if (rows < 1 || rows > 50 || columns < 1 || columns > 20) {
            throw new InvalidXMLFormatException("Sheet dimensions are out of bounds: " + rows + "x" + columns);
        }
        if(rowHeight <= 0 || columnWidth <= 0){
            throw new InvalidXMLFormatException("Cell dimensions are non-existing: " + rowHeight + "," + columnWidth);
        }
    }

    private Cell translateSTLCellToCell(STLCell generatedCell) throws InvalidXMLFormatException {
        Cell cell = new CellImpl(generatedCell.getRow() , generatedCell.getColumn().toUpperCase().trim().charAt(0) - 'A' + 1, generatedCell.getSTLOriginalValue());

        // Set the original value from the generated XML object
        cell.setOriginalValue(generatedCell.getSTLOriginalValue());
        // Optionally calculate the effective value based on your logic
        //cell.calculateEffectiveValue();

        // Return the fully constructed and populated cell
        return cell;
    }

    public void setCell(int row, int col, String input) {
        try {
            sheet.setCell(row, col, input);
            saveCurrentVersion(createSnapshot());
        }
        catch (CalculationException e)
        {
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
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
        snapshot.setCells(deepCopiedCells);

        return (SheetDTO) snapshot;
    }

    public int countAmountOfCellsChangedFromPreviousVersions(SheetDTO sheetVersion) {
        int currentVersion = sheetVersion.getVersion();
        int count = 0;

        for(CellDTO cell : sheetVersion.getMapOfCellsDTO().values()) {
            if(cell.getVersion() == currentVersion){
                count++;
            }
        }

        return count;
    }

    public void saveStateToFile(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save state to file: " + e.getMessage());
        }
    }

    public static Engine loadStateFromFile(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (Engine) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load engine state from file: " + e.getMessage(), e);
        }
    }

}
