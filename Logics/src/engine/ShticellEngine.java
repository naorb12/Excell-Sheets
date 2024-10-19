package engine;

import sheet.manager.SheetManager;

import java.util.HashMap;
import java.util.Map;

public class ShticellEngine {

    Map<String, SheetManager> sheetManagerMap = new HashMap<String, SheetManager>();

    public Map<String, SheetManager> getSheetManagerMap() {
        return sheetManagerMap;
    }

    public void setSheetManagerMap(Map<String, SheetManager> sheetManagerMap) {
        this.sheetManagerMap = sheetManagerMap;
    }

}
