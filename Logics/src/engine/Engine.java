package engine;

import exception.InvalidXMLFormatException;
import exception.OutOfBoundsException;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.cell.impl.CellType;
import sheet.coordinate.Coordinate;
import sheet.impl.SheetImpl;
import xml.generated.STLCell;
import xml.generated.STLSheet;

import java.util.*;

public class Engine {
    private static SheetImpl sheet;

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
                isWithinBounds(generatedCell.getRow(), generatedCell.getColumn().trim().charAt(0) - 'A' + 1);

                // Translate and validate the cell
                Cell cell = translateSTLCellToCell(generatedCell);

                // Store the cell in the sheet's cell map
                Coordinate coord = new Coordinate(cell.getCoordinate().getRow(), cell.getCoordinate().getColumn());
                cells.put(coord, cell);
            }
            sheet.setCells(cells);

        } catch (OutOfBoundsException e) {
            throw new RuntimeException(e);
        }
    }

    private void validateSheetDimensions(STLSheet generatedSheet) throws InvalidXMLFormatException {
        int rows = generatedSheet.getSTLLayout().getRows();
        int columns = generatedSheet.getSTLLayout().getColumns();

        if (rows < 1 || rows > 50 || columns < 1 || columns > 20) {
            throw new InvalidXMLFormatException("Sheet dimensions are out of bounds: " + rows + "x" + columns);
        }
    }

    private Cell translateSTLCellToCell(STLCell generatedCell) throws InvalidXMLFormatException {
        Cell cell = new CellImpl(generatedCell.getRow() , generatedCell.getColumn().trim().charAt(0) - 'A' + 1, generatedCell.getSTLOriginalValue());

        // Set the original value from the generated XML object
        cell.setOriginalValue(generatedCell.getSTLOriginalValue());
        // Optionally calculate the effective value based on your logic
        //cell.calculateEffectiveValue();

        if(cell.isFormula())
        {
            Set<Cell> dependencies = parseFormulaForDependencies(cell.getOriginalValue());
            cell.setDependsOn(dependencies);

            validateDependencies(dependencies);
        }

        // Return the fully constructed and populated cell
        return cell;
    }

    private void validateDependencies(Set<Cell> dependencies) throws InvalidXMLFormatException {
        for (Cell cell : dependencies) {
            Optional<Cell> dependentCellOpt = Optional.ofNullable(cell);
            if (!dependentCellOpt.isPresent()) {
                throw new InvalidXMLFormatException("Dependent cell at " + cell + " is missing or has an invalid value.");
            }
            Cell dependentCell = dependentCellOpt.get();
            if (dependentCell.getEffectiveValue().getCellType() == CellType.STRING) {
                throw new InvalidXMLFormatException("Dependent cell at " + cell + " has an invalid type: STRING");
            }
        }
    }

    private Set<Cell> parseFormulaForDependencies(String originalValue) {
        Set<Cell> dependencies = new HashSet<>();
        // Parsing logic here...
        return dependencies;
    }

    public void setCell(int row, int col, String input) {
        sheet.setCell(row, col, input);
        sheet.incrementVersion();
    }
}
