package main;

import javafx.beans.property.SimpleBooleanProperty;

public class SharedModel {

    // This boolean property will indicate if a sheet is loaded
    private final SimpleBooleanProperty sheetLoaded = new SimpleBooleanProperty(false);

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
