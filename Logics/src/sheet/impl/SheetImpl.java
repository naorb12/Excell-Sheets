package sheet.impl;

import expression.parser.FunctionParser;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class SheetImpl implements sheet.api.Sheet, SheetDTO, Serializable {

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
    public Map<Coordinate, CellDTO> getMapOfCellsDTO() {
        // Create a new map to hold the result
        Map<Coordinate, CellDTO> cellDTOMap = new HashMap<>();

        // Iterate over the entries of activeCells
        for (Map.Entry<Coordinate, Cell> entry : activeCells.entrySet()) {
            // Add each entry to the new map, casting the value to CellDTO
            cellDTOMap.put(entry.getKey(), (CellDTO) entry.getValue());
        }

        // Return an unmodifiable copy of the new map
        return Map.copyOf(cellDTOMap);
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
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public void setCells(Map<Coordinate, Cell> cells) {
        // Step 1: Initialize activeCells with a copy of the provided cells map
        this.activeCells = new HashMap<>(cells);

        // Step 2: Loop through each cell to update dependencies and check for loops
        for (Map.Entry<Coordinate, Cell> entry : activeCells.entrySet()) {
            Coordinate coordinate = entry.getKey();
            Cell cell = entry.getValue();

            // Parse and set the dependencies (dependsOn) for the current cell
            Set<Coordinate> dependsOnSet = FunctionParser.parseDependsOn(cell.getOriginalValue());
            cell.setDependsOn(dependsOnSet);

            // Check for dependency loops starting from the current cell
            if (hasDependencyLoop(coordinate, coordinate, new HashSet<>())) {
                throw new IllegalStateException("Dependency loop detected at " + coordinate.toString());
            }

            // Update the influencingOn set for each dependent cell
            for (Coordinate dependsOnCoordinate : dependsOnSet) {
                Cell dependentCell = activeCells.get(dependsOnCoordinate);
                if (dependentCell != null) {
                    dependentCell.getInfluencingOn().add(coordinate);
                }
                else {
                    dependentCell = new CellImpl(dependsOnCoordinate.getRow(), dependsOnCoordinate.getColumn(), "");
                    activeCells.put(dependsOnCoordinate, dependentCell);
                    dependentCell.getInfluencingOn().add(coordinate); // Now safely add the influence
                }
            }
        }

        // Step 3: Loop through each cell again to calculate its effective value
        for (Cell cell : activeCells.values()) {
            cell.calculateEffectiveValue(this);
        }
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

        try {
            // Step 1: Validate Input and Detect Dependency Loops
            validateAndCheckLoops(coordinate, input);

            Set<Coordinate> oldDependsOnSet = cell != null ? cell.getDependsOn() : null;

            if (cell == null) {
                cell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), input);
                activeCells.put(coordinate, cell);
            } else {
                cell.setOriginalValue(input);
            }

            Set<Coordinate> newDependsOnSet = FunctionParser.parseDependsOn(input);
            cell.setDependsOn(newDependsOnSet);

            // Step 2: Update Dependencies
            updateDependencies(coordinate, cell, oldDependsOnSet, input);

            // Step 3: Recalculate Effective Values
            recalculateEffectiveValue(coordinate, cell);

            // Step 4: Update Version
            incrementVersion();

        } catch (Exception e) {
            throw e;
        }
    }

    private void validateAndCheckLoops(Coordinate coordinate, String input) {
        Set<Coordinate> newDependsOnSet = FunctionParser.parseDependsOn(input);
        if (hasDependencyLoop(coordinate, coordinate, new HashSet<>())) {
            throw new IllegalStateException("Dependency loop detected at " + coordinate);
        }
    }

    private void updateDependencies(Coordinate coordinate, Cell cell, Set<Coordinate> oldDependsOnSet, String input) {
        // Remove the cell's influence from old dependencies
        if (oldDependsOnSet != null) {
            for (Coordinate dependsOnCoordinate : oldDependsOnSet) {
                Cell dependentCell = activeCells.get(dependsOnCoordinate);
                if (dependentCell != null) {
                    dependentCell.getInfluencingOn().remove(coordinate);
                } else {
                    // If the cell is null but there's a reference, create it
                    dependentCell = new CellImpl(dependsOnCoordinate.getRow(), dependsOnCoordinate.getColumn(), "");
                    activeCells.put(dependsOnCoordinate, dependentCell);
                    dependentCell.getInfluencingOn().remove(coordinate); // Now safely remove the influence
                }
            }
        }

        // Update the influencingOn sets for the new dependencies
        Set<Coordinate> newDependsOnSet = FunctionParser.parseDependsOn(input);
        for (Coordinate dependsOnCoordinate : newDependsOnSet) {
            Cell dependentCell = activeCells.get(dependsOnCoordinate);
            if (dependentCell != null) {
                dependentCell.getInfluencingOn().add(coordinate);
            } else {
                // If the cell is null but there's a reference, create it
                dependentCell = new CellImpl(dependsOnCoordinate.getRow(), dependsOnCoordinate.getColumn(), "");
                activeCells.put(dependsOnCoordinate, dependentCell);
                dependentCell.getInfluencingOn().add(coordinate); // Now safely add the influence
            }
        }
    }

    private void recalculateEffectiveValue(Coordinate coordinate, Cell cell) {
        cell.calculateEffectiveValue(this);
        incrementVersionForCellAndInfluences(coordinate);
    }

    public void incrementVersionForCellAndInfluences(Coordinate coordinate) {
        Cell cell = activeCells.get(coordinate);
        if (cell == null) {
            return;
        }

        // Increment the version number for the current cell
        cell.incrementVersionNumber();

        // Recursively increment the version number for all influenced cells
        for (Coordinate influencedCoordinate : cell.getInfluencingOn()) {
            incrementVersionForCellAndInfluences(influencedCoordinate);
        }
    }

    @Override
    public void incrementVersion() {
        this.version++;
    }
}
