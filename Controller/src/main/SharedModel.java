package main;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

public class SharedModel {

    // This boolean property will indicate if a sheet is loaded
    private final SimpleBooleanProperty sheetLoaded = new SimpleBooleanProperty(false);

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public SimpleBooleanProperty sheetLoadedProperty() {
        return sheetLoaded;
    }

    public SimpleBooleanProperty isSheetLoaded() {
        return sheetLoaded;
    }

    public void setSheetLoaded(boolean loaded) {
        sheetLoaded.set(loaded);
    }
}
