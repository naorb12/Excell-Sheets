package sheet.impl;

import expression.parser.FunctionParser;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.cell.impl.EffectiveValueImpl;
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

    // Version history
    private Map<Integer, SheetDTO> versionHistory = new HashMap<>();

    public SheetImpl(int rows, int columns, int rowsHeight, int columnsWidth) {
        this.rowsCount = rows;
        this.columnsCount = columns;
        this.columnsWidth = columnsWidth;
        this.rowsHeight = rowsHeight;
        activeCells = new HashMap<Coordinate, Cell>();
    }


    // Add a method to retrieve a specific version of the sheet
    @Override
    public SheetDTO peekVersion(int version) {
        return versionHistory.get(version);
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
    public void setCells(Map<Coordinate, Cell> cells) {
        this.activeCells = new HashMap<>(cells);

        // First loop: Update dependsOn and influencingOn sets, and detect loops
        activeCells.forEach((coordinate, cell) -> {
            Set<Coordinate> dependsOnSet = FunctionParser.parseDependsOn(cell.getOriginalValue());
            cell.setDependsOn(dependsOnSet);

            // Detect loops
            if (hasDependencyLoop(coordinate, coordinate, new HashSet<>())) {
                throw new IllegalStateException("Dependency loop detected at " + coordinate.toString());
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
            cell.calculateEffectiveValue(this);
        });

        saveCurrentVersion();
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

            // Step 2: Save Current Version Snapshot
            SheetDTO prevSnapShot = createSnapshot();

            // Step 3: Apply Cell Update
            Set<Coordinate> oldDependsOnSet = updateCell(coordinate, cell, input);

            // Step 4: Update Dependencies
            updateDependencies(coordinate, cell, oldDependsOnSet, input);

            // Step 5: Recalculate Effective Values
            recalculateEffectiveValue(coordinate, cell);

            // Step 6: Update Versions
            updateVersions(coordinate, prevSnapShot);

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

    private Set<Coordinate> updateCell(Coordinate coordinate, Cell cell, String input) {
        Set<Coordinate> oldDependsOnSet = cell != null ? cell.getDependsOn() : null;

        if (cell == null) {
            cell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), input);
            activeCells.put(coordinate, cell);
        } else {
            cell.setOriginalValue(input);
        }

        return oldDependsOnSet;
    }

    private void updateDependencies(Coordinate coordinate, Cell cell, Set<Coordinate> oldDependsOnSet, String input) {
        // Remove the cell's influence from old dependencies
        if (oldDependsOnSet != null) {
            for (Coordinate dependsOnCoordinate : oldDependsOnSet) {
                Cell dependentCell = activeCells.get(dependsOnCoordinate);
                if (dependentCell != null) {
                    dependentCell.getInfluencingOn().remove(coordinate);
                }
            }
        }

        // Update the influencingOn sets for the new dependencies
        Set<Coordinate> newDependsOnSet = FunctionParser.parseDependsOn(input);
        for (Coordinate dependsOnCoordinate : newDependsOnSet) {
            Cell dependentCell = activeCells.get(dependsOnCoordinate);
            if (dependentCell != null) {
                dependentCell.getInfluencingOn().add(coordinate);
            }
        }
    }

    private void recalculateEffectiveValue(Coordinate coordinate, Cell cell) {
        cell.calculateEffectiveValue(this);
        incrementVersionForCellAndInfluences(coordinate);
    }

    private void updateVersions(Coordinate coordinate, SheetDTO prevSnapShot) {
        versionHistory.put(prevSnapShot.getVersion(), prevSnapShot);
        incrementVersion();
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

    // Add a method to save the current version of the sheet
    public void saveCurrentVersion() {
        versionHistory.put(version, createSnapshot());
    }

    // Method to create a snapshot of the current sheet state
    public SheetDTO createSnapshot() {
        // Create a deep copy of the current sheet
        SheetImpl snapshot = new SheetImpl(rowsCount, columnsCount, rowsHeight, columnsWidth);
        snapshot.name = this.name;
        snapshot.version = this.version;

        // Create a deep copy of the activeCells map
        Map<Coordinate, Cell> deepCopiedCells = new HashMap<>();
        for (Map.Entry<Coordinate, Cell> entry : this.activeCells.entrySet()) {
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
        snapshot.activeCells = deepCopiedCells;

        return snapshot;
    }

    @Override
    public void incrementVersion() {
        this.version++;
        saveCurrentVersion();
    }
}
