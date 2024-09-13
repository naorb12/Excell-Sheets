package top;

import center.CenterController;
import immutable.objects.SheetDTO;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import left.LeftController;
import main.SharedModel;
import xml.generated.STLSheet;
import xml.handler.XMLSheetLoader;
import xml.handler.XMLSheetLoaderImpl;

import java.io.File;
import java.util.Map;

public class TopController {

    @FXML
    private ComboBox<String> styleSelector;
    @FXML
    private Label filePathLabel;
    @FXML
    private Button loadFileButton;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Label selectedCellIDLabel;
    @FXML
    private Label cellOriginalValueLabel;
    @FXML
    private Button updateValueButton;
    @FXML
    private Label lastUpdateCellVersionLabel;
    @FXML
    private ComboBox<String> sheetVersionSelector;

    private Stage primaryStage;
    private CenterController centerController; // For communication with the center grid

    private SharedModel sharedModel;

    // Method to inject the shared model into this controller
    public void setSharedModel(SharedModel sharedModel) {
        this.sharedModel = sharedModel;
    }

    public void initialize() {
        styleSelector.getItems().addAll("Light Theme", "Dark Theme", "Blue Theme");
        styleSelector.setValue("Light Theme");

        // Apply default style after initializing the controller (if primaryStage is set)
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyStyle(styleSelector.getValue());
        }
        // Set action for changing style
        styleSelector.setOnAction(event -> changeStyle());

        // Set action for file loading
        loadFileButton.setOnAction(event -> handleLoadFileButtonAction());


        sheetVersionSelector.setValue("Select Sheet Version");
        sheetVersionSelector.setOnAction(event -> centerController.renderGridPane());

    }

    public void setupBindings() {
        // Bind buttons to the sheetLoaded property from sharedModel
        updateValueButton.disableProperty().bind(sharedModel.isSheetLoaded().not());
        sheetVersionSelector.disableProperty().bind(sharedModel.isSheetLoaded().not());
        // Bind the update button to file selection
        updateValueButton.disableProperty().bind(sharedModel.isSheetLoaded().not());
        sheetVersionSelector.disableProperty().bind(sharedModel.isSheetLoaded().not());
    }

    @FXML
    private void changeStyle() {
        applyStyle(styleSelector.getValue());
    }

    @FXML
    private void applyStyle(String selectedStyle) {
        // Clear existing stylesheets
        primaryStage.getScene().getStylesheets().clear();

        // Apply the selected stylesheet
        switch (selectedStyle) {
            case "Dark Theme":
                primaryStage.getScene().getStylesheets().add(getClass().getResource("/Resources/css/Dark_Theme.css").toExternalForm());
                break;
            case "Blue Theme":
                primaryStage.getScene().getStylesheets().add(getClass().getResource("/Resources/css/Blue_Theme.css").toExternalForm());
                break;
            default:
                primaryStage.getScene().getStylesheets().add(getClass().getResource("/Resources/css/Light_Theme.css").toExternalForm());
                break;
        }
    }

    @FXML
    private void handleLoadFileButtonAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select XML file");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml files", "*.xml"));
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile == null) {
            return;
        }

        // Set progress bar to visible and reset its progress
        progressBar.progressProperty().unbind();
        progressBar.setVisible(true);
        progressBar.setProgress(0);

        // Create the Task for loading the XML file
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Load the XML file and map it to the sheet in the engine
                XMLSheetLoader loader = new XMLSheetLoaderImpl();

                try {
                    updateProgress(0.2, 1.0); // Set progress to 20%

                    // Load the XML file
                    STLSheet sheet = loader.loadXML(selectedFile.getAbsolutePath());
                    Thread.sleep(500);
                    updateProgress(0.6, 1.0); // Set progress to 60%

                    // Map the sheet to the engine
                    centerController.getEngine().mapSTLSheet(sheet);

                    updateProgress(1.0, 1.0); // Set progress to 100%
                    Thread.sleep(500);
                } catch (Exception e) {
                    updateProgress(0, 1.0); // Reset progress if an error occurs
                    throw new RuntimeException(e);
                }

                return null;
            }
        };
        // Bind the progress bar's progress property to the task's progress property
        progressBar.progressProperty().bind(loadTask.progressProperty());

        // When the task is complete, hide the progress bar and update UI
        loadTask.setOnSucceeded(event -> {
            populateVersionSelector();
            System.out.println("XML file loaded and validated successfully.");
            sharedModel.setSheetLoaded(true);
            centerController.renderGridPane();  // Trigger rendering of the grid

            // Hide the progress bar
            progressBar.setVisible(false);
        });

        loadTask.setOnFailed(event -> {
            // Show error if task fails
            showErrorPopup("Failed to load and validate XML", loadTask.getException().getMessage());
            progressBar.setVisible(false);  // Hide the progress bar on failure
        });

        // Run the task in a background thread
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);  // Mark the thread as daemon to terminate it when the application exits
        loadThread.start();

        filePathLabel.setText(selectedFile.getAbsolutePath());
    }

    @FXML
    private void showErrorPopup(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        alert.showAndWait();  // Shows the alert and waits for the user to close it
    }


    // Method to set the primary stage for CSS switching
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    // Method to set the reference to CenterController
    public void setCenterController(CenterController centerController) {
        this.centerController = centerController;
    }

    @FXML
    public void populateVersionSelector() {
        // Clear previous items
        sheetVersionSelector.getItems().clear();

        // Get version history from the engine's version history map
        Map<Integer, SheetDTO> versionHistory = centerController.getEngine().getVersionHistory();

        // Add version numbers (keys) as strings to the ComboBox
        for (Integer version : versionHistory.keySet()) {
            sheetVersionSelector.getItems().add("Version " + version);
        }

        // Optionally, select the latest version by default
        if (!sheetVersionSelector.getItems().isEmpty()) {
            sheetVersionSelector.getSelectionModel().selectLast(); // Select the latest version
        }
    }

}
