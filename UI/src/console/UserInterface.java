package console;

import engine.Engine;
import exception.OutOfBoundsException;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import sheet.cell.api.EffectiveValue;
import sheet.cell.impl.CellType;
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

    public void run() {
         scanner = new Scanner(System.in);
        boolean exit = false;

        while (!exit) {
            System.out.println();
            // Display the menu
            System.out.println("Menu:");
            System.out.println("1. Load Sheet from XML");
            System.out.println("2. Display Sheet");
            System.out.println("3. Display Cell");
            System.out.println("4. Update Cell");
            System.out.println("5. Display Versions");
            System.out.println("6. Save State");
            System.out.println("7. Load State");
            System.out.println("8. Exit");

            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice.trim()) {
                case "1":
                    // Load Sheet from XML
                    loadNewXML();
                    break;
                case "2":
                    // Present Sheet
                    presentCurrentSheet();
                    break;
                case "3":
                    // Display Cell
                    displayCell();
                    break;
                case "4":
                    // Update Cell
                    setCell();
                    break;
                case "5":
                    // Display Versions
                    displayPreviousVersions();
                    break;
                case "6":
                    saveState();
                    break;
                case "7":
                    loadState();
                    break;
                case "8":
                    System.out.println("Exiting the program.");
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }

    }

    private void displayPreviousVersions() {
        if(engine.getSheet() != null) {
            try {
                System.out.println("Enter the version number you want to peek at range of 1-" + engine.getSheet().getVersion() + ": ");
                int version = Integer.parseInt(scanner.nextLine());

                // Ask the engine to get the specified version of the sheet
                SheetDTO sheetVersion = engine.peekVersion(version);

                if (sheetVersion != null) {
                    int cellsChanged = engine.countAmountOfCellsChangedFromPreviousVersions(sheetVersion);
                    System.out.println("Displaying sheet version (" + version + ") With (" + cellsChanged + ") cells updated in that version:");
                    presentSpecificSheet(sheetVersion);  // Display the retrieved version
                } else {
                    System.out.println("Version " + version + " not found.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid version number. Please enter a valid integer.");
            } catch (Exception e) {
                System.out.println("An error occurred while retrieving the version: " + e.getMessage());
            }
        }
        else{
            System.out.println("No sheet found.");
        }
    }

    private void presentSpecificSheet(SheetDTO sheet) {
        // Display the sheet name and version
        System.out.println("Sheet Name: " + sheet.getName());
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
            System.out.print(String.format("%-" + columnWidth + "s|", (char) (i + 'A')));
        }
        System.out.println();

        // Display each row with its number and cell values
        for (int i = 0; i < rows; i++) {
            // Adjust the formatting for the row number to align properly
            System.out.printf("%02d |", i + 1);

            for (int j = 0; j < columns; j++) {
                Optional<CellDTO> cellOpt = Optional.ofNullable(sheet.getCellDTO(i+1, j+1));
                if (cellOpt.isPresent()) {
                    EffectiveValue effectiveValue = cellOpt.get().getEffectiveValue();
                    String displayValue = effectiveValue.formatValue(Optional.of(columnWidth)).trim();

                    // Ensure that displayValue fits within the columnWidth
                    if (displayValue.length() > columnWidth) {
                        displayValue = displayValue.substring(0, columnWidth); // Cut off the extra characters
                    }

                    System.out.print(String.format("%-" + columnWidth + "s|", displayValue));
                } else {
                    System.out.print(String.format("%-" + columnWidth + "s|", "")); // Empty cell
                }
            }
            System.out.println();

            // Now add vertical padding (if rowHeight > 1)
            for (int k = 1; k < rowHeight; k++) {
                System.out.print("   |"); // Align with row number
                for (int j = 0; j < columns; j++) {
                    System.out.print(String.format("%-" + columnWidth + "s|", "")); // Empty line for padding
                }
                System.out.println();
            }
        }
    }

    public void loadNewXML()
    {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter the full path to the XML file:");
        String filePath = scanner.nextLine();

        XMLSheetLoader loader = new XMLSheetLoaderImpl(); // MOVE THE LOADER TO ENGINE

        try {
            STLSheet sheet = loader.loadXML(filePath);
            engine.mapSTLSheet(sheet);
            System.out.println("XML file loaded and validated successfully.");
        } catch (Exception e) {
            System.out.println("Failed to load and validate XML: " + e.getMessage());
        }
    }

    public void presentCurrentSheet() {
        SheetDTO sheet = engine.getSheet();
        if(sheet != null) {
            presentSpecificSheet(sheet);
        }
        else{
            System.out.println("No sheet found.");
        }
    }

    public void displayCell() {
        SheetDTO sheet = engine.getSheet();
        try {
        if(sheet != null) {


                    Coordinate coord = inputCell();

                    // Retrieve the cell from the engine
                    CellDTO cell = engine.getCell(coord.getRow(), coord.getColumn()); // Adjusting row index (1-based to 0-based)

                    // Display the details of the cell
                    printCell(cell);


            }
        else{
            System.out.println("No sheet found.");
        }
    } catch (IllegalArgumentException e) {
        System.out.println(e.getMessage() + " Please try again.");
    }
    }

    private int parseColumn(String input) throws IllegalArgumentException {
        String columnPart = input.replaceAll("\\d", "");
        if (columnPart.isEmpty() || !columnPart.matches("[A-Z]+")) {
            throw new IllegalArgumentException("Invalid column part in cell reference.");
        }

        int column = 0;//
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

        if (cell != null && cell.getEffectiveValue().getValue() != null) {
            System.out.println("Original Value: " + cell.getOriginalValue());
            System.out.println("Effective Value: " + cell.getEffectiveValue().formatValue(Optional.empty()).trim());
            System.out.println("Version: " + cell.getVersion()); // Example method to get the cell's version
            // Assuming methods to get dependencies are implemented:
            System.out.println("Depends on: " + cell.getDependsOn().toString()); // Adjust based on actual method
            System.out.println("Affects: " + cell.getInfluencingOn()); // Adjust based on actual method
        } else {
            System.out.println("The cell is empty.");
        }
    }

    public void setCell() {
        SheetDTO sheet = engine.getSheet();
        if(sheet != null) {
            Coordinate coord = inputCell();

            while (true) {
                try {
                    System.out.println("Enter your input: ");
                    String input = scanner.nextLine();
                    engine.setCell(coord.getRow(), coord.getColumn(), input);
                    System.out.println("Cell: " + (char) (coord.getColumn() + 'A' - 1) + coord.getRow() + " has been updated in the sheet.");
                    System.out.println();
                    presentCurrentSheet();
                    break;
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage() + ". Please try again.");
                } catch (Exception e) {
                    System.out.println(e.getMessage() + ". Please try again.");
                }
            }
        }
        else{
            System.out.println("No sheet found.");
        }
    }

    public Coordinate inputCell()
    {
        System.out.println("Enter the cell reference (e.g., A4): ");
        String input;

            try{
                input = scanner.nextLine().trim().toUpperCase();
                int row = Integer.parseInt(input.substring(1)) ;
                int col = input.charAt(0) - 'A' + 1; // Convert column letter to index

                if (engine.isWithinBounds(row, col)) {
                    return new Coordinate(row, col);
                } else {
                    return null;
                }
            }
            catch(OutOfBoundsException e){
                throw new IllegalArgumentException(e.getMessage());
            }
            catch(NumberFormatException e){
                throw new IllegalArgumentException("Invalid cell. Please enter a valid cell reference (e.g., A4). ");
            }

    }

    private boolean isValidCoordinate(String cellReference) {
        return cellReference.matches("^[A-Z]+[0-9]+$");
    }


    private void saveState() {
        try {
            System.out.println("Enter the file path to save the state:");
            String filePath = scanner.nextLine();
            engine.saveStateToFile(filePath);
            System.out.println("State saved successfully.");
        }
        catch (Exception e) {
            System.out.println("An error occurred while saving the state: " + e.getMessage());
        }

    }

    private void loadState() {
        try{
            System.out.println("Enter the file path to load the state:");
            String filePath = scanner.nextLine();
            Engine newEngine = new Engine();
            newEngine = Engine.loadStateFromFile(filePath);
            engine = Engine.loadStateFromFile(filePath);
            System.out.println("State loaded successfully.");
        }
        catch (Exception e) {
            System.out.println("An error occurred while loading the state: " + e.getMessage());
        }
    }
}
