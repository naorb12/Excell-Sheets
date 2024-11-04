package shticell.client.component.sheet.main;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;

public class SharedModel {

    // This boolean property will indicate if a sheet is loaded
    //private final SimpleBooleanProperty sheetLoaded = new SimpleBooleanProperty(false);

    private final SimpleBooleanProperty latestVersionSelected = new SimpleBooleanProperty(false);

    private final SimpleBooleanProperty readOnly = new SimpleBooleanProperty(false);

    private Stage primaryStage;

    private String sheetName;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

//    public SimpleBooleanProperty sheetLoadedProperty() {
//        return sheetLoaded;
//    }
//
//    public SimpleBooleanProperty isSheetLoaded() {
//        return sheetLoaded;
//    }

//    public void setSheetLoaded(boolean loaded) {
//        sheetLoaded.set(loaded);
//    }

    public SimpleBooleanProperty latestVersionSelectedProperty() {
        return latestVersionSelected;
    }

    public void setLatestVersionSelected(boolean selected) {
        latestVersionSelected.setValue(selected);
    }

    public SimpleBooleanProperty readOnlyProperty() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly.set(readOnly);
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }


}
