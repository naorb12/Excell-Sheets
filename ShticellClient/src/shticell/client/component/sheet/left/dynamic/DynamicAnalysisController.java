package shticell.client.component.sheet.left.dynamic;

import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import sheet.coordinate.Coordinate;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.main.SharedModel;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class DynamicAnalysisController {

    @FXML
    private ComboBox<String> cellSelector;

    @FXML
    private TextField stepSizeTextField;

    @FXML
    private TextField minValueTextField;

    @FXML
    private TextField maxValueTextField;

    @FXML
    private Slider valuesSlider;

    @FXML
    private Button updateValueButton;


    private CenterController centerController;

    private SharedModel sharedModel;

    public void setSharedModel(SharedModel sharedModel) {
        this.sharedModel = sharedModel;
    }

    @FXML
    private void initialize() {
        // Bind the disable property of the text fields to the selection of the cellSelector
        minValueTextField.disableProperty().bind(
                Bindings.createBooleanBinding(() -> cellSelector.getValue() == null || cellSelector.getValue().isEmpty(), cellSelector.valueProperty()));
        maxValueTextField.disableProperty().bind(
                Bindings.createBooleanBinding(() -> cellSelector.getValue() == null || cellSelector.getValue().isEmpty(), cellSelector.valueProperty()));
        stepSizeTextField.disableProperty().bind(
                Bindings.createBooleanBinding(() -> cellSelector.getValue() == null || cellSelector.getValue().isEmpty(), cellSelector.valueProperty()));

        // Bind the disable property of the slider to the state of all text fields being filled
        valuesSlider.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> minValueTextField.getText().isEmpty() ||
                                maxValueTextField.getText().isEmpty() ||
                                stepSizeTextField.getText().isEmpty(),
                        minValueTextField.textProperty(),
                        maxValueTextField.textProperty(),
                        stepSizeTextField.textProperty())
        );

        // Bind the disable property of the update button to the state of all text fields being filled
        updateValueButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> minValueTextField.getText().isEmpty() ||
                                maxValueTextField.getText().isEmpty() ||
                                stepSizeTextField.getText().isEmpty(),
                        minValueTextField.textProperty(),
                        maxValueTextField.textProperty(),
                        stepSizeTextField.textProperty())
        );

        // Add listeners to update slider min, max, and block increment dynamically
        minValueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSliderMinValue();
            adjustSliderValueWithinBounds();
            if (cellSelector.getValue() != null && !cellSelector.getValue().isEmpty()) {
                try {
                    setSliderValueFromCell(cellSelector.getValue());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        maxValueTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSliderMaxValue();
            adjustSliderValueWithinBounds();
            if (cellSelector.getValue() != null && !cellSelector.getValue().isEmpty()) {
                try {
                    setSliderValueFromCell(cellSelector.getValue());
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        stepSizeTextField.textProperty().addListener((observable, oldValue, newValue) -> updateSliderStepSize());

        // Add a listener to update the slider value based on the selected cell's value
        cellSelector.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    setSliderValueFromCell(newValue);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Add a listener to handle slider value changes
        valuesSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                handleSliderValueChange(newValue);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // Update slider value on button click
        updateValueButton.setOnAction(event -> {
            try {
                handleUpdateValueButton();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setSliderValueFromCell(String cellName) throws ExecutionException, InterruptedException {
        Coordinate coord = parseCellNameToCoordinate(cellName);
        Optional<CellDTO> cell = centerController.getServerEngineService().getCell(sharedModel.getSheetName(), coord.getRow(), coord.getColumn()).get();
        if (cell != null && isDouble(cell.get().getOriginalValue())) {
            double cellValue = Double.parseDouble(cell.get().getOriginalValue());

            // Ensure the cell value is within bounds before setting it to the slider
            double minValue = valuesSlider.getMin();
            double maxValue = valuesSlider.getMax();

            if (cellValue < minValue) {
                valuesSlider.setValue(minValue);
            } else if (cellValue > maxValue) {
                valuesSlider.setValue(maxValue);
            } else {
                valuesSlider.setValue(cellValue);
            }
        }
    }

    private void updateSliderMinValue() {
        try {
            double minValue = Double.parseDouble(minValueTextField.getText());
            valuesSlider.setMin(minValue);
        } catch (NumberFormatException e) {
            // Handle invalid input gracefully
        }
    }

    private void updateSliderMaxValue() {
        try {
            double maxValue = Double.parseDouble(maxValueTextField.getText());
            valuesSlider.setMax(maxValue);
        } catch (NumberFormatException e) {
            // Handle invalid input gracefully
        }
    }

    private void updateSliderStepSize() {
        try {
            double stepSize = Double.parseDouble(stepSizeTextField.getText());

            // Ensure the step size is valid (positive number)
            if (stepSize > 0) {
                valuesSlider.setBlockIncrement(stepSize);
                valuesSlider.setMajorTickUnit(stepSize);  // Optional: Add major ticks for better user experience

                // Add a listener to snap slider value to step increments
                valuesSlider.valueProperty().removeListener(sliderValueListener);
                sliderValueListener = (observable, oldValue, newValue) -> snapSliderToStep(stepSize);
                valuesSlider.valueProperty().addListener(sliderValueListener);
            } else {
                // If step size is not valid, set a reasonable default value
                valuesSlider.setBlockIncrement(1);
                valuesSlider.setMajorTickUnit(1);
            }
        } catch (NumberFormatException e) {
            // Handle invalid input gracefully by setting a reasonable default step size
            valuesSlider.setBlockIncrement(1);
            valuesSlider.setMajorTickUnit(1);
        }
    }

    // Listener for snapping the slider value to the nearest step
    private ChangeListener<Number> sliderValueListener = (observable, oldValue, newValue) -> {};

    // Method to snap the slider to the nearest step based on the step size
    private void snapSliderToStep(double stepSize) {
        double currentValue = valuesSlider.getValue();
        double minValue = valuesSlider.getMin();

        // Calculate the nearest step value
        double snappedValue = minValue + Math.round((currentValue - minValue) / stepSize) * stepSize;

        // Prevent unnecessary updates to avoid infinite loops
        if (snappedValue != currentValue) {
            valuesSlider.setValue(snappedValue);
        }
    }


    // Utility method to adjust slider value to remain within bounds
    private void adjustSliderValueWithinBounds() {
        double minValue = valuesSlider.getMin();
        double maxValue = valuesSlider.getMax();
        double currentValue = valuesSlider.getValue();

        if (currentValue < minValue) {
            valuesSlider.setValue(minValue);
        } else if (currentValue > maxValue) {
            valuesSlider.setValue(maxValue);
        }
    }

    public void setCenterController(CenterController centerController) throws ExecutionException, InterruptedException {
        this.centerController = centerController;
        populateSelectors();
    }

    private void populateSelectors() {
        if (centerController != null) {
            centerController.getServerEngineService().getSheet(sharedModel.getSheetName())
                    .thenAccept(sheet -> {
                        int columnCount = sheet.getColumnCount();
                        int rowCount = sheet.getRowCount();

                        List<CompletableFuture<Optional<String>>> cellFutures = new ArrayList<>();

                        for (int row = 1; row <= rowCount; row++) {
                            for (int col = 0; col < columnCount; col++) {
                                String cellName = getColumnName(col) + row;
                                Coordinate coord = parseCellNameToCoordinate(cellName);

                                CompletableFuture<Optional<String>> cellFuture = centerController.getServerEngineService()
                                        .getCell(sharedModel.getSheetName(), coord.getRow(), coord.getColumn())
                                        .handle((optionalCell, ex) -> {
                                            if (ex != null) {
                                                System.err.println("Error fetching cell: " + ex.getMessage());
                                                return Optional.empty();
                                            }
                                            return optionalCell.map(cell -> isDouble(cell.getOriginalValue()) ? cellName : null);
                                        });

                                cellFutures.add(cellFuture);
                            }
                        }

                        CompletableFuture.allOf(cellFutures.toArray(new CompletableFuture[0]))
                                .thenAccept(v -> {
                                    List<String> cellNames = cellFutures.stream()
                                            .map(CompletableFuture::join)
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .collect(Collectors.toList());

                                    if (!cellNames.isEmpty()) {
                                        cellNames.sort((cellName1, cellName2) -> {
                                            Coordinate coord1 = parseCellNameToCoordinate(cellName1);
                                            Coordinate coord2 = parseCellNameToCoordinate(cellName2);

                                            int columnComparison = Integer.compare(coord1.getColumn(), coord2.getColumn());
                                            return columnComparison != 0 ? columnComparison : Integer.compare(coord1.getRow(), coord2.getRow());
                                        });

                                        Platform.runLater(() -> {
                                            cellSelector.getItems().setAll(cellNames);
                                            System.out.println("Populated cellSelector successfully!");
                                        });
                                    } else {
                                        System.out.println("No valid numeric cells found.");
                                    }
                                })
                                .exceptionally(ex -> {
                                    System.err.println("Error populating selectors: " + ex.getMessage());
                                    return null;
                                });
                    })
                    .exceptionally(ex -> {
                        System.err.println("Failed to fetch sheet: " + ex.getMessage());
                        return null;
                    });
        }
    }



    // Utility method to parse a cell name into a Coordinate (A3 -> row=3, column=1)
    private Coordinate parseCellNameToCoordinate(String cellName) {
        try {
            // Extract the column part (letters)
            String columnPart = cellName.replaceAll("\\d", "");
            // Extract the row part (numbers)
            int row = Integer.parseInt(cellName.replaceAll("[A-Z]", ""));

            // Convert column letters to a number (A -> 1, B -> 2, etc.)
            int column = columnToNumber(columnPart);

            // Return a Coordinate object with the parsed row and column
            return new Coordinate(row, column);
        } catch (Exception e) {
            // Return null if parsing fails
            return null;
        }
    }

    // Helper method to convert column letters (A, B, ...) to 1-based column numbers (A=1, B=2, ...)
    private int columnToNumber(String columnPart) {
        int columnNumber = 0;
        for (int i = 0; i < columnPart.length(); i++) {
            columnNumber = columnNumber * 26 + (columnPart.charAt(i) - 'A' + 1);
        }
        return columnNumber;
    }


    // Helper method to convert 0-based column index to column name (0 -> A, 1 -> B, 25 -> Z, 26 -> AA)
    private String getColumnName(int index) {
        StringBuilder columnName = new StringBuilder();
        while (index >= 0) {
            columnName.insert(0, (char) ('A' + index % 26));
            index = (index / 26) - 1;  // Decrease index for the next loop
        }
        return columnName.toString();
    }

    // Utility method to check if a string can be parsed into an double
    private boolean isDouble(String value) {
        if (value == null) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @FXML
    public void handleRemoveDynamicAnalysis() {
        centerController.renderGridPane();
    }

    @FXML
    private void handleUpdateValueButton() throws ExecutionException, InterruptedException {
        Coordinate coordinate = parseCellNameToCoordinate(cellSelector.getValue());
        centerController.updateValueBySlider(coordinate, valuesSlider.getValue());
    }

    @FXML
    private void handleSliderValueChange(Number newValue) throws ExecutionException, InterruptedException {
        Coordinate coordinate = parseCellNameToCoordinate(cellSelector.getValue());
        centerController.applyDynamicAnalysis(coordinate, newValue);
    }
}