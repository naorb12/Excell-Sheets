package sheet.impl;

import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.Coordinate;

import java.util.HashMap;
import java.util.Map;

public class SheetImpl implements sheet.api.Sheet, SheetDTO {

    private Map<Coordinate, Cell> activeCells;
    private String name;
    private int version = 1;
    private int columnsCount;
    private int rowsCount;
    private int columnsWidth;
    private int rowsHeight;

    public SheetImpl(int rows, int columns, int rowsHeight, int columnsWidth) {
        this.rowsCount = rows;
        this.columnsCount = columns;
        this.columnsWidth = columnsWidth;
        this.rowsHeight = rowsHeight;
        activeCells = new HashMap<Coordinate, Cell>();
    }


    @Override
    public Map<Coordinate, Cell> getMapOfCells() {
        return Map.copyOf(activeCells);
    }

    @Override
    public Cell getCell(int rowCount, int columnCount) {
        return activeCells.get(new Coordinate(rowCount, columnCount));
    }

    @Override
    public CellDTO getCellDTO(int row, int column) {
        return (CellDTO) activeCells.get(new Coordinate(row, column));
    }

    @Override
    public int getColumnCount() {return columnsCount;}

    @Override
    public int getRowCount() {return rowsCount;}

    @Override
    public int getColumnsWidthUnits() {
        return columnsWidth;
    }

    @Override
    public int getRowHeightUnits() {
        return rowsHeight;
    }

    @Override
    public String getName() {return name;}

    @Override
    public int getVersion() {return version;}

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setCells(Map<Coordinate, Cell> cells) {
        this.activeCells = Map.copyOf(cells);
    }

    @Override
    public void setCell(int row, int col, String input) {
        Coordinate coord = new Coordinate(row, col);
        Cell cell = activeCells.getOrDefault(coord, new CellImpl(row, col, input));
        cell.setOriginalValue(input);

        // Put the cell in the map
        activeCells.put(coord, cell);
    }
}
