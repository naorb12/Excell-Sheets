package shticell.client.component.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import shticell.client.component.api.ShticellCommands;
import shticell.client.component.main.ShticellAppMainController;

import java.io.IOException;

public class DashboardController implements Cloneable, ShticellCommands {

    private ShticellAppMainController shticellAppMainController;

    @FXML
    private TableView<?> sheetTableView; // You'll replace '?' with the actual type of your data model

    @FXML
    private TableColumn<?, String> sheetNameColumn;

    @FXML
    private TableColumn<?, String> ownerColumn;

    @FXML
    private TableColumn<?, String> sheetSizeColumn;

    @FXML
    private TableColumn<?, String> permissionColumn;

    @FXML
    private Button viewSheetButton;

    @FXML
    public void initialize() {
        // Assuming columns will be bound to the properties in your data model class
        // Example:
        // sheetNameColumn.setCellValueFactory(new PropertyValueFactory<>("sheetName"));
        // ownerColumn.setCellValueFactory(new PropertyValueFactory<>("owner"));
        // sheetSizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
        // permissionColumn.setCellValueFactory(new PropertyValueFactory<>("permission"));

        sheetTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            viewSheetButton.setDisable(newSelection == null);
        });
    }

    @FXML
    public void onAddSheet() {
        // Implement logic to add a new sheet
        // shticellAppMainController.openAddSheetWindow(); // Example call
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
}
