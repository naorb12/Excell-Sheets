package console;

import engine.Engine;
import exception.OutOfBoundsException;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.api.EffectiveValue;
import sheet.coordinate.Coordinate;
import xml.generated.STLSheet;
import xml.handler.XMLSheetLoader;
import xml.handler.XMLSheetLoaderImpl;

import java.util.Optional;
import java.util.Scanner;

public class UserInterface {
    private Engine engine;
    private Scanner scanner;


    public UserInterface(Engine engine) {
        this.engine = engine;
        this.scanner = new Scanner(System.in);
    }

    public void loadNewXML()
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the full path to the XML file:");
        String filePath = scanner.nextLine();

        XMLSheetLoader loader = new XMLSheetLoaderImpl(); // MOVE THE LOADER TO ENGINE

        try {
            STLSheet sheet = loader.loadAndValidateXML(filePath);
            engine.mapSTLSheet(sheet);
            System.out.println("XML file loaded and validated successfully.");
        } catch (Exception e) {
            System.out.println("Failed to load and validate XML: " + e.getMessage());
        }
    }

    public void presentSheet() {
        SheetDTO sheet = engine.getSheet();

        // Display the sheet name and version
        System.out.println("sheet.impl.Sheet Name: " + sheet.getName());
        System.out.println("Version: " + sheet.getVersion());
        System.out.println();

        // Determine the number of rows and columns
        int rows = sheet.getRowCount();
        int columns = sheet.getColumnCount();

        // Get the column width and row height from the layout size
        int columnWidth = sheet.getColumnsWidthUnits();
        int rowHeight = sheet.getRowHeightUnits();

        // Display the column headers (A, B, C, ...)
        System.out.print("    "); // Starting offset for row numbers
        for (int i = 0; i < columns; i++) {
            System.out.print(String.format("%-" + columnWidth + "s |", (char) (i + 'A')));
        }
        System.out.println();

        for (int i = 0; i < rows; i++) {
            System.out.printf("%02d | ", i + 1);

            for (int j = 0; j < columns; j++) {
                Optional<CellDTO> cellOpt = Optional.ofNullable(sheet.getCellDTO(i, j));
                if (cellOpt.isPresent()) {
                    EffectiveValue effectiveValue = cellOpt.get().getEffectiveValue();
                    String displayValue = effectiveValue.formatValue(columnWidth);
                    System.out.print(String.format("%-" + columnWidth + "s | ", displayValue));
                } else {
                    System.out.print(String.format("%-" + columnWidth + "s | ", "")); // Empty cell
                }
            }
            System.out.println();

            // Add vertical space (padding) based on the row height units
            for (int k = 1; k < rowHeight; k++) {
                System.out.printf("%-3s", ""); // Maintain the row number column alignment
                for (int j = 0; j < columns; j++) {
                    System.out.print(String.format("%-" + columnWidth + "s | ", "")); // Empty line for padding
                }
                System.out.println();
            }
        }
    }

    public void detailCell() {
        while (true) {
            try {

                Coordinate coord = inputCell();

                // Retrieve the cell from the engine
                CellDTO cell = engine.getCell(coord.getRow() - 1, coord.getColumn()); // Adjusting row index (1-based to 0-based)

                // Display the details of the cell
                printCell(cell);

                break; // Exit the loop after successful input
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage() + " Please try again.");
            }
        }
    }

    private int parseColumn(String input) throws IllegalArgumentException {
        String columnPart = input.replaceAll("\\d", "");
        if (columnPart.isEmpty() || !columnPart.matches("[A-Z]+")) {
            throw new IllegalArgumentException("Invalid column part in cell reference.");
        }

        int column = 0;
        for (int i = 0; i < columnPart.length(); i++) {
            column = column * 26 + (columnPart.charAt(i) - 'A' + 1);
        }
        return column - 1; // Convert to 0-based index
    }

    private int parseRow(String input) throws IllegalArgumentException {
        String rowPart = input.replaceAll("[A-Z]", "");
        if (rowPart.isEmpty() || !rowPart.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid row part in cell reference.");
        }

        return Integer.parseInt(rowPart);
    }

    private void printCell(CellDTO cell) {
        if (cell != null) {
            System.out.println("sheet.cell.impl.Cell Reference: " );
            System.out.println("Original Value: " + cell.getOriginalValue());
            System.out.println("Effective Value: " + cell.getEffectiveValue());
            System.out.println("Version: " + cell.getVersion()); // Example method to get the cell's version
            // Assuming methods to get dependencies are implemented:
            System.out.println("Depends on: " + cell.getDependsOn()); // Adjust based on actual method
            System.out.println("Affects: " + cell.getInfluencingOn()); // Adjust based on actual method
        } else {
            System.out.println("The cell is empty.");
        }
    }

    public void setCell() {
        Coordinate coord = inputCell();
        System.out.println("Enter your input: ");
        String input = scanner.nextLine();
        engine.setCell(coord.getRow(), coord.getColumn(), input);
    }

    public Coordinate inputCell()
    {
        System.out.println("Enter the cell reference (e.g., A4): ");
        String input;
        while (true) {
            try{
                input = scanner.nextLine().trim().toUpperCase();
                int row = Integer.parseInt(input.substring(1)) - 1; // Assuming rows start from 1
                int col = input.charAt(0) - 'A'; // Convert column letter to index

                if (engine.isWithinBounds(row, col)) {
                    return new Coordinate(row, col);
                }
            }
            catch(OutOfBoundsException e){
                System.out.println(e.getMessage());
            }
            catch(NumberFormatException e){
                System.out.println("Invalid cell. Please enter a valid cell reference (e.g., A4): ");
            }
        }
    }

    private boolean isValidCoordinate(String cellReference) {
        return cellReference.matches("^[A-Z]+[0-9]+$");
    }
}
