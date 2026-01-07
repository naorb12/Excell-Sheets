package shticell.client.component.sheet.left.graph;

import exception.InvalidXMLFormatException;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sheet.coordinate.Coordinate;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.main.SharedModel;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class GraphPopUpController {

    @FXML
    private TextField fromCellFieldX;

    @FXML
    private TextField toCellFieldX;

    @FXML
    private TextField fromCellFieldY;

    @FXML
    private TextField toCellFieldY;

    @FXML
    private Button barChartButton;

    @FXML
    private Button lineChartButton;

    // Reference to the CenterController
    private CenterController centerController;

    public void setSharedModel(SharedModel sharedModel) {
        this.sharedModel = sharedModel;
    }

    private SharedModel sharedModel;

    // Inject the CenterController (call this method from outside to pass the reference)
    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;
    }

    @FXML
    private void initialize() {

        barChartButton.setOnAction(event -> {
            try {
                handleBarChartButtonAction();
            } catch (InvalidXMLFormatException e) {
                throw new RuntimeException(e);
            } catch (Exception e){
                showErrorPopup("Error Showing Chart", e.getMessage());
            }

        });

        lineChartButton.setOnAction(event -> {
            try {
                handleLineChartButtonAction();
            } catch (InvalidXMLFormatException e) {
                throw new RuntimeException(e);
            } catch (Exception e){
                showErrorPopup("Error Showing Chart", e.getMessage());
            }
        });
        // Bind the button disable properties to the text fields' state
        barChartButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> fromCellFieldX.getText().trim().isEmpty() || toCellFieldX.getText().trim().isEmpty()
                                || fromCellFieldY.getText().trim().isEmpty() || toCellFieldY.getText().trim().isEmpty(),
                        fromCellFieldX.textProperty(), toCellFieldX.textProperty(),
                        fromCellFieldY.textProperty(), toCellFieldY.textProperty()
                )
        );

        lineChartButton.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> fromCellFieldX.getText().trim().isEmpty() || toCellFieldX.getText().trim().isEmpty()
                                || fromCellFieldY.getText().trim().isEmpty() || toCellFieldY.getText().trim().isEmpty(),
                        fromCellFieldX.textProperty(), toCellFieldX.textProperty(),
                        fromCellFieldY.textProperty(), toCellFieldY.textProperty()
                )
        );
    }

    @FXML
    private void handleBarChartButtonAction() throws InvalidXMLFormatException {
        try {
            String fromX = fromCellFieldX.getText();
            String toX = toCellFieldX.getText();
            String fromY = fromCellFieldY.getText();
            String toY = toCellFieldY.getText();

            // Call the method to get data from the SheetManager
            List<Double> xData = getDataFromRange(fromX, toX);
            List<Double> yData = getDataFromRange(fromY, toY);

            // Call a method to generate the BarChart using the input ranges
            generateBarChart(xData, yData);
        } catch (Exception e){
            showErrorPopup("Error Showing Bar Chart", e.getMessage());
        }
    }

    @FXML
    private void handleLineChartButtonAction() throws InvalidXMLFormatException {
        try {
            String fromX = fromCellFieldX.getText();
            String toX = toCellFieldX.getText();
            String fromY = fromCellFieldY.getText();
            String toY = toCellFieldY.getText();

            // Call the method to get data from the SheetManager
            List<Double> xData = getDataFromRange(fromX, toX);
            List<Double> yData = getDataFromRange(fromY, toY);

            // Call a method to generate the LineChart using the input ranges
            generateLineChart(xData, yData);
        }
        catch (Exception e){
            showErrorPopup("Error Showing Chart", e.getMessage());
        }
    }

    private List<Double> getDataFromRange(String fromCell, String toCell) throws InvalidXMLFormatException, ExecutionException, InterruptedException {
        // Use the CenterController's engine to get the data for the range
        List<Coordinate> range = centerController.getServerEngineService().validateRange(sharedModel.getSheetName(), fromCell, toCell).get();
        return centerController.getServerEngineService().getRangeNumericValues(sharedModel.getSheetName(), range).get();
    }

    private void generateBarChart(List<Double> xData, List<Double> yData) {
        final CategoryAxis xAxis = new CategoryAxis();  // Use CategoryAxis for bar chart
        final NumberAxis yAxis = new NumberAxis();      // Use NumberAxis for numerical values

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Bar Chart Example");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Data");

        for (int i = 0; i < xData.size(); i++) {
            // Convert xData to String for the CategoryAxis
            series.getData().add(new XYChart.Data<>(String.valueOf(xData.get(i)), yData.get(i)));
        }

        barChart.getData().add(series);

        // Create a new Stage (window) to show the chart
        Stage chartStage = new Stage();
        chartStage.setTitle("Bar Chart");

        // Set the chart in a Scene
        Scene chartScene = new Scene(barChart, 800, 600);  // Customize width and height as needed
        chartStage.setScene(chartScene);

        // Show the chart window
        chartStage.show();
    }

    private void generateLineChart(List<Double> xData, List<Double> yData) {
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Line Chart Example");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Data");

        for (int i = 0; i < xData.size(); i++) {
            series.getData().add(new XYChart.Data<>(xData.get(i), yData.get(i)));
        }

        lineChart.getData().add(series);

        // Create a new Stage (window) to show the chart
        Stage chartStage = new Stage();
        chartStage.setTitle("Line Chart");

        // Set the chart in a Scene
        Scene chartScene = new Scene(lineChart, 800, 600);  // Customize width and height as needed
        chartStage.setScene(chartScene);

        // Show the chart window
        chartStage.show();
    }

    @FXML
    private void showErrorPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();  // Shows the alert and waits for the user to close it
    }
}