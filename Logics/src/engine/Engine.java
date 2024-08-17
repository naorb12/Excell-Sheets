package engine;

import exception.OutOfBoundsException;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.Coordinate;
import sheet.impl.SheetImpl;
import xml.generated.STLCell;
import xml.generated.STLSheet;

import java.util.HashMap;
import java.util.Map;

public class Engine {
    private SheetImpl sheet;

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

    public boolean isWithinBounds(int row, int column) throws OutOfBoundsException {
        int maxRow = sheet.getRowCount(); // Assuming method to get total rows
        int maxColumn = sheet.getColumnCount(); // Assuming method to get total columns

        if (row < 0 || row >= maxRow || column < 0 || column >= maxColumn) {
            throw new OutOfBoundsException(maxRow, maxColumn);
        }
        return true;
    }

    public void mapSTLSheet(STLSheet generatedSheet) {
        sheet = new SheetImpl(generatedSheet.getSTLLayout().getRows(), generatedSheet.getSTLLayout().getColumns(),
                generatedSheet.getSTLLayout().getSTLSize().getRowsHeightUnits(), generatedSheet.getSTLLayout().getSTLSize().getColumnWidthUnits());
        sheet.setName(generatedSheet.getName().trim());

        Map<Coordinate, Cell> cells = new HashMap<>();
        for (STLCell generatedCell : generatedSheet.getSTLCells().getSTLCell()) {
            Cell cell = translateSTLCellToCell(generatedCell);
            Coordinate coord = new Coordinate(generatedCell.getRow() - 1,  generatedCell.getColumn().trim().charAt(0) - 'A');
            cells.put(coord, cell);
        }
        sheet.setCells(cells);
    }

    private Cell translateSTLCellToCell(STLCell generatedCell) {
        Cell cell = new CellImpl(generatedCell.getRow() - 1, generatedCell.getColumn().trim().charAt(0) - 'A' - 1, generatedCell.getSTLOriginalValue());

        // Set the original value from the generated XML object
        cell.setOriginalValue(generatedCell.getSTLOriginalValue());
        // Optionally calculate the effective value based on your logic
        cell.calculateEffectiveValue();

        // Return the fully constructed and populated cell
        return cell;
    }

    public void setCell(int row, int col, String input) {
        sheet.setCell(row, col, input);
    }
}
