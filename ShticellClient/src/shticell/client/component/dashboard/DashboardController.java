package shticell.client.component.dashboard;

import com.google.gson.reflect.TypeToken;
import engine.manager.dto.SheetManagerDTO;
import engine.permission.dto.UserPermissionsDTO;
import engine.permission.property.PermissionStatus;
import engine.permission.property.PermissionType;
import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;
import okhttp3.*;
import shticell.client.component.api.ShticellCommands;
import shticell.client.component.main.ShticellAppMainController;
import shticell.client.util.Constants;
import shticell.client.util.http.HttpClientUtil;
import shticell.client.util.http.HttpMethod;
import xml.generated.STLSheet;
import xml.handler.XMLSheetLoader;
import xml.handler.XMLSheetLoaderImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DashboardController implements Cloneable, ShticellCommands {

    private ShticellAppMainController shticellAppMainController;

    private Timer timer;
    private TimerTask refresherTask;
    private final BooleanProperty autoUpdate;
    private final IntegerProperty totalSheets;

    // Sheets Dashboard
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
    // Sheets Buttons
    @FXML
    private Button viewSheetButton;
    @FXML
    private Button addSheetButton;

    private ObservableList<SheetDTO> sheetData = FXCollections.observableArrayList(); // Observable list for table data
    private Map<String, Map<String, UserPermissionsDTO>> currentUserPermissionsMap = new HashMap<>();

    // Permissions
    @FXML
    private TableView<UserPermissionsDTO> userPermissionsTableView;
    @FXML
    private TableColumn<UserPermissionsDTO, String> userPermissionsNameColumn;
    @FXML
    private TableColumn<UserPermissionsDTO, String> userPermissionsTypeColumn;
    @FXML
    private TableColumn<UserPermissionsDTO, String> userPermissionsStatusColumn;
    // Permissions Buttons
    @FXML
    private Button requestReaderButton;
    @FXML
    private Button requestWriterButton;
    @FXML
    private Button approveRequestButton;
    @FXML
    private Button denyRequestButton;
    private ObservableList<UserPermissionsDTO> userPermissionsData = FXCollections.observableArrayList();


    @FXML
    public Label errorMessageLabel;
    public final StringProperty errorMessageProperty = new SimpleStringProperty();


    public DashboardController() {
        autoUpdate = new SimpleBooleanProperty(true); // Auto-update enabled by default
        totalSheets = new SimpleIntegerProperty();
    }

    @FXML
    public void initialize() {

        // Bind columns to the data
        sheetNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        ownerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOwner()));
        sheetSizeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRowCount() + "x" + cellData.getValue().getColumnCount()));
        // Bind the permissionColumn to show the current user's permission for each sheet
        permissionColumn.setCellValueFactory(cellData -> {
            SheetDTO sheet = cellData.getValue();
            String currentUser = shticellAppMainController.getCurrentUserName(); // Get the current user's name

            // Fetch the current user's permissions for the sheet from currentUserPermissionsMap
            Map<String, UserPermissionsDTO> permissionsMap = currentUserPermissionsMap.get(sheet.getName());
            if (permissionsMap != null && permissionsMap.containsKey(currentUser)) {
                UserPermissionsDTO userPermission = permissionsMap.get(currentUser);
                return new SimpleStringProperty(userPermission.getPermissionType().toString());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });

        // Set the TableView's items to the ObservableList
        sheetTableView.setItems(sheetData);
        sheetTableView.setFixedCellSize(25);  // Set row height to 25 pixels
        sheetTableView.refresh();

        // Set up columns for the user permissions table view
        userPermissionsNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserName()));
        userPermissionsTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPermissionType().toString()));
        userPermissionsStatusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPermissionStatus().toString()));

        userPermissionsTableView.setItems(userPermissionsData);

        errorMessageLabel.textProperty().bind(errorMessageProperty);

        HttpClientUtil.setCookieManagerLoggingFacility(line ->
                Platform.runLater(() -> updateHttpStatusLine(line)));

        // Add listener to handle sheet selection
        sheetTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            onSheetSelected();  // Call method to update permissions when a sheet is selected
            viewSheetButton.setDisable(newSelection == null);
            requestReaderButton.setDisable(newSelection == null);
            requestWriterButton.setDisable(newSelection == null);
        });

        // Add Listener to handle giving out permissions
        userPermissionsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            onPermissionSelected(newSelection);});
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


                    String sheetJson = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.toJson(sheet);

                    // Pass the sheet name and username to the method
                    sendSheetJson(sheetJson, sheet.getName(), shticellAppMainController.getCurrentUserName()); // Replace with actual username retrieval logic

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

    private void sendSheetJson(String sheetJson, String sheetName, String username) {
        String url = Constants.ADD_SHEET + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&owner=" + URLEncoder.encode(username, StandardCharsets.UTF_8);

        RequestBody requestBody = RequestBody.create(sheetJson, MediaType.parse("application/json"));

        // Use the advanced runReqAsyncWithJson
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, requestBody, (responseBody) -> {
            Platform.runLater(() -> processAddSheetResponse(responseBody));
        });
    }

    // Refactored response processing logic
    private void processAddSheetResponse(String responseBody) {
        if (responseBody == null) {
            errorMessageProperty.set("Failed to add sheet: Empty response body");
            return;
        }

        // Deserialize to SheetManagerDTO (which contains the sheet and permission/version history maps)
        SheetManagerDTO sheetManagerDTO = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, SheetManagerDTO.class);

        if (sheetManagerDTO != null) {
            SheetDTO sheetDTO = sheetManagerDTO.getSheet();
            Map<String, UserPermissionsDTO> userPermissionsMap = sheetManagerDTO.getUserPermissionsDTOMap();

            // Store user permissions for the selected sheet
            currentUserPermissionsMap.put(sheetDTO.getName(), userPermissionsMap);

            // Add sheetDTO to your data
            sheetData.add(sheetDTO);
        } else {
            errorMessageProperty.set("Deserialization failed: sheetManagerDTO is null");
        }
    }


    @FXML
    public void onSheetSelected() {
        SheetDTO selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
        if (selectedSheet != null) {
            String url = Constants.GET_SHEET_USER_PERMISSIONS + "?sheetName=" + URLEncoder.encode(selectedSheet.getName(), StandardCharsets.UTF_8);

            // Make an asynchronous call to fetch the permissions for the selected sheet
            HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
                Platform.runLater(() -> processPermissionsResponse(responseBody));
            });
        }
    }

    @FXML
    public void onPermissionSelected(UserPermissionsDTO newSelection) {
        // Get the selected sheet from the sheetTableView
        SheetDTO selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();

        if (selectedSheet != null && currentUserPermissionsMap.containsKey(selectedSheet.getName())) {
            Map<String, UserPermissionsDTO> permissionsMap = currentUserPermissionsMap.get(selectedSheet.getName());
            // Get the current user's permission from the map
            UserPermissionsDTO currentUserPermission = permissionsMap.get(shticellAppMainController.getCurrentUserName());

            if (currentUserPermission != null && currentUserPermission.getPermissionType() == PermissionType.OWNER) {
                // Check if the selected permission in the userPermissionsTableView is pending
                if (newSelection != null && newSelection.getPermissionStatus() == PermissionStatus.PENDING) {
                    // Enable the buttons if the permission is pending
                    approveRequestButton.setDisable(newSelection == null);
                    denyRequestButton.setDisable(newSelection == null);
                } else {
                    // Disable the buttons if the permission is not pending
                    approveRequestButton.setDisable(true);
                    denyRequestButton.setDisable(true);
                }
            } else {
                // Disable the buttons if the current user is not the owner
                approveRequestButton.setDisable(true);
                denyRequestButton.setDisable(true);
            }
        } else {
            // Disable the buttons if no sheet or permissions map is found
            approveRequestButton.setDisable(true);
            denyRequestButton.setDisable(true);
        }
    }

    private void processPermissionsResponse(String responseBody) {
        if (responseBody == null) {
            errorMessageProperty.set("Failed to load permissions: Empty response");
            return;
        }


        // Deserialize the response to a Map<String, UserPermissionsDTO>
        Type permissionsMapType = new TypeToken<Map<String, UserPermissionsDTO>>() {}.getType();
        Map<String, UserPermissionsDTO> permissionsMap = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, permissionsMapType);

        // Clear the current permissions in the table
        userPermissionsData.clear();

        // Add the fetched permissions to the table
        if (permissionsMap != null) {
            userPermissionsData.addAll(permissionsMap.values());
        } else {
            errorMessageProperty.set("Failed to load permissions: Invalid response format");
        }

        // Refresh the table view
        userPermissionsTableView.setItems(userPermissionsData);
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
        startDashboardRefresher();
        //requestSheetsFromServer();
        sheetTableView.refresh();
    }

    private void updateSheetsList(List<SheetDTO> sheets) {
        Platform.runLater(() -> {
            sheetData.setAll(sheets);
            totalSheets.set(sheets.size());
        });
    }

    public void startDashboardRefresher() {
        refresherTask = new DashboardRefresher(sheetData, sheetTableView);  // Pass sheetData and sheetTableView
        timer = new Timer();
        timer.schedule(refresherTask, 0, Constants.REFRESH_RATE);  // Refresh every REFRESH_RATE milliseconds (e.g., 5 seconds)
    }

    public void stopDashboardRefresher() {
        if (refresherTask != null && timer != null) {
            refresherTask.cancel();
            timer.cancel();
        }
    }


    public void setInActive() {
        try {
            stopDashboardRefresher();
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

    public TableView<SheetDTO> getSheetTableView() {
        return sheetTableView;
    }
    public ObservableList<SheetDTO> getSheetData() {
        return sheetData;
    }

    // Action when the "Request Reader Permission" button is clicked
    @FXML
    private void onRequestReaderPermission() {
        // Check if a sheet is selected
        SheetDTO selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
        if (selectedSheet == null) {
            errorMessageProperty.set("No sheet selected. Please select a sheet to request permission.");
            return;
        }

        // Get the name of the selected sheet
        String sheetName = selectedSheet.getName();
        // Prepare the URL for the permission request
        String url = Constants.REQUEST_READER_PERMISSION + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&userName=" + URLEncoder.encode(shticellAppMainController.getCurrentUserName(), StandardCharsets.UTF_8) + "&permissionType=" + URLEncoder.encode(PermissionType.READER.toString(), StandardCharsets.UTF_8);

        RequestBody emptyBody = RequestBody.create("", MediaType.parse("application/json")); // Use an empty request body
        // Make an async HTTP request to request reader permission
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, emptyBody, (responseBody) -> {
            Platform.runLater(() -> {
                if (responseBody == null) {
                    errorMessageProperty.set("Failed to request reader permission: Empty response from server.");
                    return;
                }

                // Handle success or failure based on server response (assuming server sends a success/failure message)
                if (responseBody.contains("success")) {
                    errorMessageProperty.set("Reader permission requested successfully.");
                } else {
                    errorMessageProperty.set("Failed to request reader permission: " + responseBody);
                }
            });
        });
    }


    // Action when the "Request Writer Permission" button is clicked
    @FXML
    private void onRequestWriterPermission() {
        // Check if a sheet is selected
        SheetDTO selectedSheet = sheetTableView.getSelectionModel().getSelectedItem();
        if (selectedSheet == null) {
            errorMessageProperty.set("No sheet selected. Please select a sheet to request permission.");
            return;
        }

        // Get the name of the selected sheet
        String sheetName = selectedSheet.getName();
        // Prepare the URL for the permission request
        String url = Constants.REQUEST_READER_PERMISSION + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                + "&userName=" + URLEncoder.encode(shticellAppMainController.getCurrentUserName(), StandardCharsets.UTF_8) + "&permissionType=" + URLEncoder.encode(PermissionType.WRITER.toString(), StandardCharsets.UTF_8);

        RequestBody emptyBody = RequestBody.create("", MediaType.parse("application/json")); // Use an empty request body
        // Make an async HTTP request to request reader permission
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.POST, emptyBody, (responseBody) -> {
            Platform.runLater(() -> {
                if (responseBody == null) {
                    errorMessageProperty.set("Failed to request reader permission: Empty response from server.");
                    return;
                }

                // Handle success or failure based on server response (assuming server sends a success/failure message)
                if (responseBody.contains("success")) {
                    errorMessageProperty.set("Reader permission requested successfully.");
                } else {
                    errorMessageProperty.set("Failed to request reader permission: " + responseBody);
                }
            });
        });
    }

    // Action when the "Approve Permission Request" button is clicked
    @FXML
    private void onApprovePermissionRequest() {
        // Logic for approving a permission request
        System.out.println("Approve Permission Request clicked");
    }

    // Action when the "Deny Permission Request" button is clicked
    @FXML
    private void onDenyPermissionRequest() {
        // Logic for denying a permission request
        System.out.println("Deny Permission Request clicked");
    }

}
