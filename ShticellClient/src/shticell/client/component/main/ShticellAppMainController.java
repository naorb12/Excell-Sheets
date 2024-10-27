package shticell.client.component.main;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import shticell.client.component.api.HttpStatusUpdate;
import shticell.client.component.dashboard.DashboardController;
import shticell.client.component.login.LoginController;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;

import static shticell.client.util.Constants.*;

public class ShticellAppMainController implements Closeable, HttpStatusUpdate {

    @FXML
    private Parent httpStatusComponent;
   // @FXML private StatusController httpStatusComponentController;

    private GridPane loginComponent;
    private LoginController logicController;

    private Parent shticellDashboardComponent;
    private DashboardController dashboardComponentController;

    @FXML private Label userGreetingLabel;
    @FXML private AnchorPane mainPanel;


    private final StringProperty currentUserName;
    public ShticellAppMainController() {
        currentUserName = new SimpleStringProperty(JHON_DOE);
    }

    @FXML
    public void initialize() {
        userGreetingLabel.textProperty().bind(Bindings.concat("Hello ", currentUserName));

        // prepare components
        loadLoginPage();
        loadDashboard();
    }

    public void updateUserName(String userName) {
        currentUserName.set(userName);
    }

    public String getCurrentUserName() {
        return currentUserName.get();
    }

    private void setMainPanelTo(Parent pane) {
        mainPanel.getChildren().clear();
        mainPanel.getChildren().add(pane);
        AnchorPane.setBottomAnchor(pane, 1.0);
        AnchorPane.setTopAnchor(pane, 1.0);
        AnchorPane.setLeftAnchor(pane, 1.0);
        AnchorPane.setRightAnchor(pane, 1.0);
    }

    @Override
    public void close() throws IOException {
        dashboardComponentController.close();
    }

    private void loadLoginPage() {
        URL loginPageUrl = getClass().getResource(LOGIN_PAGE_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            loginComponent = fxmlLoader.load();
            logicController = fxmlLoader.getController();
            logicController.setShticellAppMainController(this);
            setMainPanelTo(loginComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboard() {
        URL loginPageUrl = getClass().getResource(DASHBOARD_FXML_RESOURCE_LOCATION);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(loginPageUrl);
            shticellDashboardComponent = fxmlLoader.load();
            dashboardComponentController = fxmlLoader.getController();
            dashboardComponentController.setShticellAppMainController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateHttpLine(String line) {
       // httpStatusComponentController.addHttpStatusLine(line);
    }

    public void switchToDashboard() {
        setMainPanelTo(shticellDashboardComponent);
        dashboardComponentController.setActive();
    }

    public void switchToLogin() {
        Platform.runLater(() -> {
            currentUserName.set(JHON_DOE);
            dashboardComponentController.setInActive();
            setMainPanelTo(loginComponent);
        });
    }
}
