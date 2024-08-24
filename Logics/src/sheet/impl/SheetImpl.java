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

        // Step 1: Legitimacy Check
        try {
            // Parse the new dependencies
            Set<Coordinate> newDependsOnSet = FunctionParser.parseDependsOn(input);

            // Detect loops before applying the changes
            if (hasDependencyLoop(coordinate, coordinate, new HashSet<>())) {
                throw new IllegalStateException("Dependency loop detected at " + coordinate);
            }

            // Step 2: Save the current version (before making changes)
            SheetDTO prevSnapShot = createSnapshot();
            Set<Coordinate> oldDependsOnSet = cell.getDependsOn();

            // Step 3: Apply the update
            if (cell == null) {
                // Handle the case where the cell doesn't exist (e.g., create a new one)
                cell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), input);
                activeCells.put(coordinate, cell);
            } else {
                // Update the cell's value
                cell.setOriginalValue(input);
            }

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
            cell.calculateEffectiveValue(this);

            // Increment the version number for this cell and all influenced cells
            incrementVersionForCellAndInfluences(coordinate);

            versionHistory.put(prevSnapShot.getVersion(), prevSnapShot);
            // Finalize the version increment if everything is successful
            incrementVersion();

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
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
    @Override
    public void saveCurrentVersion() {
        versionHistory.put(version, createSnapshot());
    }

    // Method to create a snapshot of the current sheet state
    @Override
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
            Cell cell = new CellImpl(cellCopied.getCoordinate().getRow(),cellCopied.getCoordinate().getColumn(), cellCopied.getOriginalValue(), cellCopied.getEffectiveValue(),
                    cellCopied.getVersion(), cellCopied.getDependsOn(),cellCopied.getInfluencingOn());
            deepCopiedCells.put(entry.getKey(),cell);
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
