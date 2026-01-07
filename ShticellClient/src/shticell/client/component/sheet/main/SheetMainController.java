package shticell.client.component.sheet.main;

import engine.permission.property.PermissionType;
import immutable.objects.SheetDTO;
import immutable.objects.SheetManagerDTO;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import shticell.client.component.api.ShticellCommands;
import shticell.client.component.main.ShticellAppMainController;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.left.LeftController;
import shticell.client.component.sheet.top.TopController;
import shticell.client.util.Constants;
import shticell.client.util.http.HttpClientUtil;
import shticell.client.util.http.HttpMethod;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class SheetMainController implements ShticellCommands {

    private Timer timer;
    private TimerTask refresherTask;
    private final BooleanProperty autoUpdate;

    @FXML
    private ShticellAppMainController shticellAppMainController;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private TopController topController;
    @FXML
    private LeftController leftController;
    @FXML
    private CenterController centerController;

    @FXML
    private Button backToDashboardButton;

    private SharedModel sharedModel;

    public SheetMainController() {
        this.autoUpdate = new SimpleBooleanProperty(true);
    }


    public void initialize() {
        // Delay the rest of the initialization

        try {
            // Initialize the shared model
            sharedModel = new SharedModel();
            sharedModel.setLatestVersionSelected(true);

            // Load and set the center section
            FXMLLoader centerLoader = new FXMLLoader(getClass().getResource("/shticell/client/component/sheet/center/centerSection.fxml"));
            ScrollPane centerPane = centerLoader.load();
            centerController = centerLoader.getController();
            //System.out.println("CenterController loaded: " + centerController);
            mainBorderPane.setCenter(centerPane);
            centerController.setSharedModel(sharedModel);


            // Load and set the top section
            FXMLLoader topLoader = new FXMLLoader(getClass().getResource("/shticell/client/component/sheet/top/topSection.fxml"));
            AnchorPane topPane = topLoader.load();
            topController = topLoader.getController();
            topController.setCenterController(centerController);
            topController.setSharedModel(sharedModel);
            topController.setupBindings();
            centerController.setTopController(topController);
            //System.out.println("TopController loaded: " + topController);
            mainBorderPane.setTop(topPane);

            // Load and set the left section
            FXMLLoader leftLoader = new FXMLLoader(getClass().getResource("/shticell/client/component/sheet/left/leftSection.fxml"));
            VBox leftPane = leftLoader.load();
            leftController = leftLoader.getController();
            leftController.setCenterController(centerController);
            leftController.setSharedModel(sharedModel);
            leftController.setupBindings();
            //System.out.println("LeftController loaded: " + leftController);
            mainBorderPane.setLeft(leftPane);
            topController.setLeftController(leftController);

            // Set click event for background pane
            mainBorderPane.setOnMouseClicked(event -> {
                if (centerController.getSelectedCellLabel() != null) {
                    centerController.clearHighlights();
                    topController.resetLabelsAndText();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void setShticellAppMainController(ShticellAppMainController shticellAppMainController) {
        this.shticellAppMainController = shticellAppMainController;
    }

    @Override
    public void logout() {
        shticellAppMainController.switchToLogin();
    }

    @Override
    public void updateHttpLine(String line) {

    }

    public void setActive(String sheetName) {
        String url = Constants.GET_SHEET_BY_NAME + "?sheetName=" + URLEncoder.encode(sheetName, StandardCharsets.UTF_8) + "&userName=" + URLEncoder.encode(shticellAppMainController.getCurrentUserName(), StandardCharsets.UTF_8);

        // Make an asynchronous call to fetch the sheet data
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            Platform.runLater(() -> {
                if (responseBody == null) {
                    System.out.println("Failed to load sheetDTO: Empty response from server.");
                    return;
                }

                // Parse the response JSON into a Map to access "sheetDTO" and "permissionType"
                Map<String, Object> responseMap = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, Map.class);
                SheetDTO sheetDTO = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(Constants.GSON_INSTANCE_WITH_DESERIALIZERS.toJson(responseMap.get("sheetDTO")), SheetDTO.class);
                PermissionType permissionType = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(Constants.GSON_INSTANCE_WITH_DESERIALIZERS.toJson(responseMap.get("permissionType")), PermissionType.class);

                if (sheetDTO != null) {
                    centerController.renderGrid(sheetDTO);
                    leftController.populateSelectors(sheetDTO);
                    sharedModel.setSheetName(sheetName);
                    sharedModel.setUserName(shticellAppMainController.getCurrentUserName());
                    topController.populateVersionSelector();
                    startSheetMainRefresher();
                    // refresher.run();
                } else {
                    System.out.println("Failed to load sheetDTO: Deserialization error.");
                }

                if (permissionType == PermissionType.READER) {
                    sharedModel.setReadOnly(true);
                }
                else {
                    sharedModel.setReadOnly(false);
                }
            });
        });
    }

    public void setInactive(){
        try {
            stopSheetMainRefresher();
        } catch (Exception ignored) {}
    }

    private void startSheetMainRefresher() {
        refresherTask = new SheetMainRefresher(centerController, topController, sharedModel);
        timer = new Timer();
        timer.schedule(refresherTask, 0, Constants.REFRESH_RATE);
    }

    public void stopSheetMainRefresher() {
        if (refresherTask != null && timer != null) {
            refresherTask.cancel();
            timer.cancel();
        }
    }

    private void setReadOnly() {
        System.out.println("READONLY");
    }

    @FXML
    private void onBackToDashboardButton(){
        setInactive();
        shticellAppMainController.switchToDashboard();
    }
}
