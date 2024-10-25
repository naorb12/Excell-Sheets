package shticell.client.component.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import deserializer.CellImplDeserializer;
import deserializer.ColorTypeDeserializer;
import deserializer.SheetDTODeserializer;
import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import okhttp3.*;
import sheet.cell.impl.CellImpl;
import sheet.impl.SheetImpl;
import shticell.client.component.api.ShticellCommands;
import shticell.client.component.main.ShticellAppMainController;
import shticell.client.util.Constants;
import shticell.client.util.http.HttpClientUtil;
import xml.generated.STLSheet;
import xml.handler.XMLSheetLoader;
import xml.handler.XMLSheetLoaderImpl;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class DashboardController implements Cloneable, ShticellCommands {

    private ShticellAppMainController shticellAppMainController;

    @FXML
    private TableView<SheetDTO> sheetTableView;

    @FXML
    private TableColumn<SheetDTO, String> sheetNameColumn;

    @FXML
    private TableColumn<SheetDTO, String> ownerColumn;

    @FXML
    private TableColumn<SheetDTO, String> sheetSizeColumn;

    @FXML
    private TableColumn<SheetDTO, String> permissionColumn;

    private ObservableList<SheetDTO> sheetData = FXCollections.observableArrayList(); // Observable list for table data


    @FXML
    private Button viewSheetButton;

    @FXML
    public Label errorMessageLabel;
    private final StringProperty errorMessageProperty = new SimpleStringProperty();


    @FXML
    public void initialize() {

        // Bind columns to the data
        sheetNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        ownerColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Owner"));
        sheetSizeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRowCount() + "x" + cellData.getValue().getColumnCount()));
        permissionColumn.setCellValueFactory(cellData -> new SimpleStringProperty("Permission"));

        // Set the TableView's items to the ObservableList
        sheetTableView.setItems(sheetData);
        sheetTableView.setFixedCellSize(25);  // Set row height to 25 pixels
        sheetTableView.refresh();

        errorMessageLabel.textProperty().bind(errorMessageProperty);
        HttpClientUtil.setCookieManagerLoggingFacility(line ->
                Platform.runLater(() ->
                        updateHttpStatusLine(line)));

        sheetTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewSheetButton.setDisable(newSelection == null);
        });

        // Listener to enable/disable buttons based on table selection
        sheetTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewSheetButton.setDisable(newSelection == null);
        });


    }


    @FXML
    private void onAddSheet() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile == null) {
            return; // No file selected, exit early
        }

        // Create the Task for loading the XML file
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load the XML file and map it to the sheet in the engine
                XMLSheetLoader loader = new XMLSheetLoaderImpl();
                try {
                    // Load the XML file
                    STLSheet sheet = loader.loadXML(selectedFile.getAbsolutePath());

                    // Convert the STLSheet object to JSON
                    Gson gson = new Gson();
                    String sheetJson = gson.toJson(sheet);

                    // Send the JSON via HTTP POST request
                    sendSheetJson(sheetJson, sheet.getName());

                } catch (Exception e) {
                    throw new RuntimeException("Error loading or sending sheet: " + e.getMessage(), e);
                }

                return null;
            }
        };

        // Run the task in a background thread
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);  // Mark the thread as daemon to terminate it when the application exits
        loadThread.start();
    }

    private void sendSheetJson(String sheetJson, String sheetName) {
        String url = Constants.ADD_SHEET;  // The base URL for your servlet

        // Create the request body with the JSON data
        RequestBody requestBody = RequestBody.create(
                sheetJson, MediaType.parse("application/json")
        );

        // Build the request as a POST request
        Request request = new Request.Builder()
                .url(url + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8))
                .post(requestBody)  // Make sure this POST request is for adding STLSheet
                .build();

        // Send the request asynchronously
        HttpClientUtil.runAsyncRequest(request, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Failed to send sheet: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.code() != 200) {
                    String responseBody = response.body().string();
                    Platform.runLater(() -> System.out.println("Received error response: " + responseBody));
                } else {
                    String responseBody = response.body().string();  // Get the actual response body (the JSON)

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(SheetImpl.class, new SheetDTODeserializer())  // Handle SheetDTO deserialization
                            .registerTypeAdapter(CellImpl.class, new CellImplDeserializer())  // Handle CellImpl deserialization
                            .setPrettyPrinting()
                            .create();

                    SheetDTO sheetDTO = gson.fromJson(responseBody, SheetImpl.class);  // Deserialize to SheetDTOImpl

                    Platform.runLater(() -> {
                        if (sheetDTO != null) {
                            sheetData.add(sheetDTO);
                        } else {
                          errorMessageProperty.set("Deserialization failed: sheetDTO is null");
                        }
                    });
                }
            }

        });
    }


    @FXML
    public void onViewSheet() {
        // Implement logic to view the selected sheet
        // Sheet selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
        // if (selectedSheet != null) {
        //     shticellAppMainController.switchToSheetView(selectedSheet);
        // }
    }

    @FXML
    @Override
    public void logout() {
        shticellAppMainController.switchToLogin();
    }

    public void close() throws IOException {
        shticellAppMainController.close();
    }

    public void setActive() {
        //chatAreaComponentController.startListRefresher();
    }

    public void setInActive() {
        try {
            // chatAreaComponentController.close();
        } catch (Exception ignored) {}
    }

    public void setShticellAppMainController(ShticellAppMainController shticellAppMainController) {
        this.shticellAppMainController = shticellAppMainController;
    }

    @Override
    public void updateHttpLine(String line) {
        // Handle HTTP updates if needed
    }

    private void updateHttpStatusLine(String data) {
        shticellAppMainController.updateHttpLine(data);
    }
}
