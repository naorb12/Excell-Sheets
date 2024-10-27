package engine.manager.dto;

import engine.manager.SheetManager;
import engine.permission.dto.UserPermissionsDTO;
import immutable.objects.SheetDTO;

import java.util.Map;

public class SheetManagerDTO {
    private SheetDTO sheet;
    private Map<String, UserPermissionsDTO> userPermissionsDTOMap;
    private Map<Integer, SheetDTO> versionHistoryDTOMap;

    public SheetManagerDTO(SheetManager sheetManager) {
        this.sheet = sheetManager.getSheet();
        this.userPermissionsDTOMap = sheetManager.getUserPermissionsMap();
        this.versionHistoryDTOMap = sheetManager.getVersionHistory();
    }

    public SheetManagerDTO(){}

    public SheetDTO getSheet() {
        return sheet;
    }
    public Map<String, UserPermissionsDTO> getUserPermissionsDTOMap() {
        return userPermissionsDTOMap;
    }
    public Map<Integer, SheetDTO> getVersionHistoryDTOMap() {
        return versionHistoryDTOMap;
    }
}
