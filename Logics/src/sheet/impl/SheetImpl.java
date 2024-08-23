package sheet.impl;

import expression.parser.FunctionParser;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.coordinate.Coordinate;

import java.util.*;

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
        this.activeCells = new HashMap<>(cells);

        // First loop: Update dependsOn and influencingOn sets, and detect loops
        activeCells.forEach((coordinate, cell) -> {
            Set<Coordinate> dependsOnSet = FunctionParser.parseDependsOn(cell.getOriginalValue());
            cell.setDependsOn(dependsOnSet);

            // Detect loops
            if (hasDependencyLoop(coordinate, coordinate, new HashSet<>())) {
                throw new IllegalStateException("Dependency loop detected at " + coordinate);
            }

            // Update the influencingOn set for each dependent cell
            dependsOnSet.forEach(dependsOnCoordinate -> {
                Cell dependentCell = activeCells.get(dependsOnCoordinate);
                if (dependentCell != null) {
                    dependentCell.getInfluencingOn().add(coordinate);
                }
            });
        });

        // Second loop: Calculate effective values
        activeCells.forEach((coordinate, cell) -> {
            cell.calculateEffectiveValue((SheetDTO) this);
        });
    }

    private boolean hasDependencyLoop(Coordinate start, Coordinate current, Set<Coordinate> visited) {
        if (visited.contains(current)) {
            return true;  // Loop detected
        }

        visited.add(current);

        Cell currentCell = activeCells.get(current);
        if (currentCell != null) {
            for (Coordinate dependency : currentCell.getDependsOn()) {
                if (hasDependencyLoop(start, dependency, visited)) {
                    return true;
                }
            }
        }

        visited.remove(current);
        return false;
    }

    @Override
    public void setCell(int row, int col, String input) {
        Coordinate coordinate = new Coordinate(row, col);
        Cell cell = activeCells.get(coordinate);

        if (cell == null) {
            // Handle the case where the cell doesn't exist (e.g., create a new one)
            cell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), input);
            activeCells.put(coordinate, cell);
        } else {
            // Update the cell's value
            cell.setOriginalValue(input);
        }

        // Parse the new dependencies
        Set<Coordinate> newDependsOnSet = FunctionParser.parseDependsOn(cell.getOriginalValue());

        // Detect loops before applying the changes
        if (hasDependencyLoop(coordinate, coordinate, new HashSet<>())) {
            throw new IllegalStateException("Dependency loop detected at " + coordinate);
        }

        // Update the cell's dependsOn set
        Set<Coordinate> oldDependsOnSet = cell.getDependsOn();
        cell.setDependsOn(newDependsOnSet);

        // Remove the current cell's influence from the old dependencies
        if (oldDependsOnSet != null) {
            oldDependsOnSet.forEach(dependsOnCoordinate -> {
                Cell dependentCell = activeCells.get(dependsOnCoordinate);
                if (dependentCell != null) {
                    dependentCell.getInfluencingOn().remove(coordinate);
                }
            });
        }

        // Update the influencingOn sets for the new dependencies
        newDependsOnSet.forEach(dependsOnCoordinate -> {
            Cell dependentCell = activeCells.get(dependsOnCoordinate);
            if (dependentCell != null) {
                dependentCell.getInfluencingOn().add(coordinate);
            }
        });

        // Recalculate the effective value for this cell
        cell.calculateEffectiveValue((SheetDTO) this);
    }

    @Override
    public void incrementVersion() {
        this.version++;
    }
}
