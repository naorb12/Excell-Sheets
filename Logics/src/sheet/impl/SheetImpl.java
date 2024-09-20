package sheet.impl;

import engine.Engine;
import expression.api.Expression;
import expression.api.RangeBasedExpression;
import expression.parser.FunctionParser;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import javafx.scene.paint.Color;
import sheet.cell.api.Cell;
import sheet.cell.impl.CellImpl;
import sheet.cell.impl.CellType;
import sheet.cell.impl.EffectiveValueImpl;
import sheet.coordinate.Coordinate;

import java.awt.*;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class SheetImpl implements sheet.api.Sheet, SheetDTO, Serializable {

    private Map<Coordinate, Cell> activeCells;
    private String name;
    private int version = 1;
    private int columnsCount;
    private int rowsCount;
    private int columnsWidth;
    private int rowsHeight;

    private Map<String, List<Coordinate>> ranges;

    public SheetImpl(int rows, int columns, int rowsHeight, int columnsWidth) {
        this.rowsCount = rows;
        this.columnsCount = columns;
        this.columnsWidth = columnsWidth;
        this.rowsHeight = rowsHeight;
        activeCells = new HashMap<Coordinate, Cell>();
        ranges = new HashMap<String, List<Coordinate>>();
    }

    @Override
    public Map<Coordinate, Cell> copyActiveCells(){
        Map<Coordinate, Cell> copiedActiveCells = new HashMap<Coordinate,Cell>();
        for(Map.Entry<Coordinate, Cell> entry : activeCells.entrySet()){
            Cell copiedCell = new CellImpl<>(entry.getValue());
            copiedActiveCells.put(copiedCell.getCoordinate(), copiedCell);
        }
        return copiedActiveCells;
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

            // Step 3: Update the dependencies for the current cell
            updateDependencies(coordinate, cell, cell.getDependsOn(), cell.getOriginalValue());

            // Check for dependency loops starting from the current cell
            if (hasDependencyLoop(coordinate, coordinate, new HashSet<>())) {
                throw new IllegalStateException("Dependency loop detected at " + coordinate.toString());
            }
        }

        // Step 4: Loop through each cell again to calculate its effective value
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
        Cell oldCell;
        if(cell != null) {
             oldCell = new CellImpl(
                    cell.getCoordinate().getRow(),
                    cell.getCoordinate().getColumn(),
                    new String(cell.getOriginalValue()),
                    new EffectiveValueImpl((EffectiveValueImpl) cell.getEffectiveValue()),  // Deep copy the EffectiveValue
                    cell.getVersion(),
                    new HashSet<>(cell.getDependsOn()),
                    new HashSet<>(cell.getInfluencingOn())
            );
        }
        else{
            oldCell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), input);
        }

        SheetImpl oldSheet = (SheetImpl) Engine.createSnapshot();

        try {
            Set<Coordinate> oldDependsOnSet = cell != null ? cell.getDependsOn() : null;

            if (cell == null) {
                cell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), input);
                activeCells.put(coordinate, cell);
            } else {
                cell.setOriginalValue(input);
            }

            Set<Coordinate> newDependsOnSet = FunctionParser.parseDependsOn(input);
            cell.setDependsOn(newDependsOnSet);

            updateDependencies(coordinate, cell, oldDependsOnSet, input);

            validateAndCheckLoops(coordinate, input);

            recalculateEffectiveValue(coordinate, cell);

            incrementVersionForCellAndInfluences(coordinate);

            incrementVersion();

        } catch (Exception e) {
            activeCells.put(coordinate,cell);
            this.activeCells = oldSheet.activeCells;
            this.version = oldSheet.version;
            throw new RuntimeException(e.getMessage());
        }
    }

    private void validateAndCheckLoops(Coordinate coordinate, String input) {
        if (hasDependencyLoop(coordinate, coordinate, new HashSet<>())) {
            throw new IllegalStateException("Dependency loop detected at " + coordinate);
        }
    }

    private void updateDependencies(Coordinate coordinate, Cell cell, Set<Coordinate> oldDependsOnSet, String input) {
        // Step 1: Remove old dependencies
        removeOldDependencies(coordinate, oldDependsOnSet);

        // Step 2: Parse and add new dependencies
        Set<Coordinate> newDependsOnSet = FunctionParser.parseDependsOn(input);

        if (isRangeFunction(input)) {
            addRangeDependencies(coordinate, input, newDependsOnSet);  // Handle ranges
        } else {
            addNewDependencies(coordinate, newDependsOnSet);  // Handle regular references
        }

        // Step 3: Set the new dependencies for the cell
        cell.setDependsOn(newDependsOnSet);
    }

    private void removeOldDependencies(Coordinate coordinate, Set<Coordinate> oldDependsOnSet) {
        if (oldDependsOnSet != null) {
            for (Coordinate dependsOnCoordinate : oldDependsOnSet) {
                Cell dependentCell = activeCells.get(dependsOnCoordinate);
                if (dependentCell != null) {
                    dependentCell.getInfluencingOn().remove(coordinate);
                } else {
                    // Create and remove the influence if the cell was null
                    dependentCell = createEmptyCell(dependsOnCoordinate);
                    dependentCell.getInfluencingOn().remove(coordinate);
                }
            }
        }
    }

    private void addNewDependencies(Coordinate coordinate, Set<Coordinate> newDependsOnSet) {
        for (Coordinate dependsOnCoordinate : newDependsOnSet) {
            Cell dependentCell = activeCells.get(dependsOnCoordinate);
            if (dependentCell != null) {
                dependentCell.getInfluencingOn().add(coordinate);
            } else {
                // Create and add the influence if the cell was null
                dependentCell = createEmptyCell(dependsOnCoordinate);
                dependentCell.getInfluencingOn().add(coordinate);
            }
        }
    }

    private void addRangeDependencies(Coordinate coordinate, String input, Set<Coordinate> newDependsOnSet) {
        String rangeName = extractRangeNameFromInput(input);
        List<Coordinate> rangeCoordinates = getRange(rangeName);  // Use the getRange method

        if (rangeCoordinates == null || rangeCoordinates.isEmpty()) {
            throw new IllegalArgumentException("Invalid range: " + rangeName);
        }

        newDependsOnSet.addAll(rangeCoordinates);  // Add all coordinates from the range to the dependsOn set
        for (Coordinate dependsOnCoordinate : rangeCoordinates) {
            Cell dependentCell = activeCells.get(dependsOnCoordinate);
            if (dependentCell != null) {
                dependentCell.getInfluencingOn().add(coordinate);
            } else {
                // Create and add the influence if the cell was null
                dependentCell = createEmptyCell(dependsOnCoordinate);
                dependentCell.getInfluencingOn().add(coordinate);
            }
        }
    }

    private boolean isRangeFunction(String input) {
        return input.toUpperCase().contains("SUM") || input.toUpperCase().contains("AVG");  // Add more as needed
    }

    private String extractRangeNameFromInput(String input) {
        String[] parts = input.substring(1, input.length() - 1).split(",");
        return parts.length > 1 ? parts[1].trim() : null;
    }

    private Cell createEmptyCell(Coordinate coordinate) {
        Cell newCell = new CellImpl(coordinate.getRow(), coordinate.getColumn(), "");
        activeCells.put(coordinate, newCell);
        return newCell;
    }

    private void recalculateEffectiveValue(Coordinate coordinate, Cell cell) {
        cell.calculateEffectiveValue(this);
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

    // Add a new range by name
    @Override
    public void addRange(String rangeName, List<Coordinate> coordinates) {
        ranges.put(rangeName, coordinates);  // Add the range to the map
    }

    @Override
    public void setRanges(Map<String, List<Coordinate>> newRanges)
    {
        ranges = new HashMap<>(newRanges);
    }

    @Override
    public void removeRange(String rangeName) {
        // Step 1: Check if any cell uses the range
        boolean isRangeUsed = isRangeInUse(rangeName);

        // Step 2: If the range is being used, throw an exception to prevent deletion
        if (isRangeUsed) {
            throw new IllegalArgumentException("Cannot remove range " + rangeName + " because it is being used by one or more cells.");
        }

        // Step 3: If the range is not in use, proceed to remove it
        ranges.remove(rangeName);  // Remove the range from the map
    }

    // Helper method to check if a range is being used by any cell in the sheet
    private boolean isRangeInUse(String rangeName) {
        // Loop through all the active cells
        for (Cell cell : activeCells.values()) {
            String originalValue = cell.getOriginalValue();
            if (originalValue != null) {
                // Check if the range name is directly referenced in the original value
                if (originalValue.contains(rangeName)) {
                    // Parse the expression to ensure it's a valid reference
                    Expression expression = FunctionParser.parseExpression(originalValue);

                    // If the expression involves the range, return true
                    if (expressionUsesRange(expression, rangeName)) {
                        return true;  // The range is being used in this cell
                    }
                }
            }
        }
        return false;
    }

    // Helper method to check if the expression involves a specific range
    private boolean expressionUsesRange(Expression expression, String rangeName) {
        // If the expression is a function that directly uses the range, return true
        if (expression instanceof RangeBasedExpression) {
            return true;
        }
        // Add more checks if necessary to ensure all range-based expressions are detected
        return false;
    }

    // Retrieve a specific range by name
    @Override
    public List<Coordinate> getRange(String rangeName) {
        return ranges.get(rangeName);  // Return the list of coordinates for the range
    }

    // Retrieve all ranges
    @Override
    public Map<String, List<Coordinate>> getAllRanges() {
        return Collections.unmodifiableMap(ranges);  // Return an unmodifiable view of the ranges
    }

    // Check if a specific coordinate is part of any range
    @Override
    public boolean isCoordinateInRange(Coordinate coord) {
        for (List<Coordinate> coordinates : ranges.values()) {
            if (coordinates.contains(coord)) {
                return true;  // Coordinate is in one of the ranges
            }
        }
        return false;  // Coordinate not found in any range
    }

    @Override
    public SheetDTO sortSheet(List<Coordinate> range, List<Integer> columnsToSortBy) {
        List<List<Cell>> rowsToSort = prepareRowsToSort(range);  // Extract rows from range

        filterNonNumericColumns(range, columnsToSortBy);  // Filter out non-numeric columns

        Comparator<List<Cell>> comparator = createComparator(columnsToSortBy);  // Create a comparator for sorting

        rowsToSort.sort(comparator);  // Sort the rows

        return createSortedSheet(rowsToSort, range);  // Create and return the sorted sheet
    }

    // Helper method to prepare rows to sort based on the range
    private List<List<Cell>> prepareRowsToSort(List<Coordinate> range) {
        List<List<Cell>> rowsToSort = new ArrayList<>();

        for (int row = 1; row <= rowsCount; row++) {
            List<Cell> rowCells = new ArrayList<>();
            for (Coordinate coord : range) {
                if (coord.getRow() == row) {
                    Cell cell = activeCells.get(coord);
                    rowCells.add(cell);
                }
            }
            rowsToSort.add(rowCells);
        }

        return rowsToSort;
    }

    // Helper method to filter out non-numeric columns from the sort list
    private void filterNonNumericColumns(List<Coordinate> range, List<Integer> columnsToSortBy) {
        List<Integer> columnsToRemove = new ArrayList<>();

        for (int column : columnsToSortBy) {
            for (Coordinate coord : range) {
                if(coord.getColumn() == column) {
                    Cell cell = activeCells.get(coord);
                    if(cell != null && cell.getEffectiveValue().getCellType() != CellType.NUMERIC) {
                        columnsToRemove.add(column);
                    }
                }
            }
        }

        // Now remove the collected columns after the iteration
        columnsToSortBy.removeAll(columnsToRemove);

        if (columnsToSortBy.isEmpty()) {
            throw new RuntimeException("Please provide at least one numeric column");
        }
    }

    // Helper method to create a comparator for sorting rows
    private Comparator<List<Cell>> createComparator(List<Integer> columnsToSortBy) {
        return (row1, row2) -> {
            for (Integer col : columnsToSortBy) {
                Cell cell1 = row1.stream().filter(c -> c != null && c.getCoordinate().getColumn() == col).findFirst().orElse(null);
                Cell cell2 = row2.stream().filter(c -> c != null && c.getCoordinate().getColumn() == col).findFirst().orElse(null);

                if (cell1 != null && cell2 != null) {
                    Double value1 = cell1.getEffectiveValue().extractValueWithExpectation(Double.class);
                    Double value2 = cell2.getEffectiveValue().extractValueWithExpectation(Double.class);

                    int compareResult = value1.compareTo(value2);
                    if (compareResult != 0) {
                        return compareResult;
                    }
                }
            }
            return 0;  // Maintain original order if comparisons are equal
        };
    }

    private SheetDTO createSortedSheet(List<List<Cell>> rowsToSort, List<Coordinate> range) {
        SheetImpl sortedSheet = new SheetImpl(rowsCount, columnsCount, rowsHeight, columnsWidth);
        Map<Coordinate, Cell> mutableMap = copyActiveCells();

        for (int rowIndex = 0; rowIndex < rowsToSort.size(); rowIndex++) {
            List<Cell> sortedRow = rowsToSort.get(rowIndex);

            for (int colIndex = 0; colIndex < sortedRow.size(); colIndex++) {
                Cell cell = sortedRow.get(colIndex);

                if (cell != null) {
                    // Calculate the new coordinate for the cell
                    Coordinate newCoordinate = new Coordinate(rowIndex + 1, cell.getCoordinate().getColumn());
                    cell.setCoordinate(newCoordinate);  // Set new coordinate
                    mutableMap.put(newCoordinate, new CellImpl(cell));  // Deep copy the cell
                } else {
                    // Handle null cells
                    int column = range.get(colIndex).getColumn();  // Get column from the original range
                    Coordinate newCoordinate = new Coordinate(rowIndex + 1, column);
                    mutableMap.put(newCoordinate, new CellImpl(newCoordinate.getRow(), newCoordinate.getColumn(), null));
                }
            }
        }

        sortedSheet.setCellsForSortAndFilter(mutableMap);
        return (SheetDTO) sortedSheet;
    }



    private void setCellsForSortAndFilter(Map<Coordinate, Cell> cells) {
        // Step 1: Initialize activeCells with a copy of the provided cells map
        this.activeCells = new HashMap<>(cells);

        // Step 2: Loop through each cell to update dependencies and check for loops
        for (Map.Entry<Coordinate, Cell> entry : activeCells.entrySet()) {
            Coordinate coordinate = entry.getKey();
            Cell cell = entry.getValue();
        }
    }

    @Override
    public void setBackgroundColor(int row, int col, Color color) {
        activeCells.get(new Coordinate(row,col)).setBackgroundColor(color);
    }
}
