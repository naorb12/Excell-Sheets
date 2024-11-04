package shticell.client.component.dashboard;

import com.google.gson.reflect.TypeToken;
import immutable.objects.UserPermissionsDTO;
import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import shticell.client.util.Constants;
import shticell.client.util.http.HttpClientUtil;
import immutable.objects.SheetManagerDTO;
import shticell.client.util.http.HttpMethod;

import java.lang.reflect.Type;
import java.util.*;

public class DashboardRefresher extends TimerTask {
    private final ObservableList<SheetDTO> sheetData;
    private final TableView<SheetDTO> sheetTableView;
    private final Map<String, Map<String, UserPermissionsDTO>> currentUserPermissionsMap;
    private final String userName;
    private int sheetCount = 0;  // Initialize with a default version

    public DashboardRefresher(ObservableList<SheetDTO> sheetData,
                              TableView<SheetDTO> sheetTableView,
                              Map<String, Map<String, UserPermissionsDTO>> currentUserPermissionsMap, String userName) {
        this.sheetData = sheetData;
        this.sheetTableView = sheetTableView;
        this.currentUserPermissionsMap = currentUserPermissionsMap;
        this.userName = userName;
    }

    @Override
    public void run() {
        String url = Constants.REQUEST_SHEETS;

        // Make an async request to fetch the sheets and permissions data
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            Platform.runLater(() -> {
                if (responseBody == null) {
                    System.out.println("No sheets found or response body is null.");
                    return;
                }

                // Deserialize response into a map of SheetManagerDTO
                Type sheetManagerMapType = new TypeToken<Map<String, SheetManagerDTO>>() {}.getType();
                Map<String, SheetManagerDTO> sheetManagers = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, sheetManagerMapType);

                // Temporary structures to hold incoming data
                List<SheetDTO> updatedSheetData = new ArrayList<>();
                Map<String, Map<String, UserPermissionsDTO>> updatedPermissionsMap = new HashMap<>();

                // Populate temporary structures
                for (SheetManagerDTO managerDTO : sheetManagers.values()) {
                    SheetDTO sheetDTO = managerDTO.getSheet();
                    updatedSheetData.add(sheetDTO);
                    updatedPermissionsMap.put(sheetDTO.getName(), managerDTO.getUserPermissionsDTOMap());
                }

                // Check if the number of sheets has changed
                boolean sheetCountChanged = updatedSheetData.size() != sheetCount;
                if (sheetCountChanged) {
                    //System.out.println("Sheet count changed. used to be " + sheetCount + " sheets, now its " + updatedSheetData.size() + " sheets.");
                    sheetCount = updatedSheetData.size();
                }

                // Check if permissions have changed for any sheet
                boolean permissionsChanged = false;
                for (String sheetName : updatedPermissionsMap.keySet()) {
                    if(updatedPermissionsMap.get(sheetName).containsKey(userName)) {
                        if(!currentUserPermissionsMap.get(sheetName).containsKey(userName)) {
                            currentUserPermissionsMap.put(sheetName, updatedPermissionsMap.get(sheetName));
                            permissionsChanged = true;
                            break;
                        }
                        if (!updatedPermissionsMap.get(sheetName).get(userName).getPermissionStatus().equals(currentUserPermissionsMap.get(sheetName).get(userName).getPermissionStatus())) {
                            permissionsChanged = true;
                            break;
                        }
                    }
                }

                // Only update if there’s a change in sheet count or permissions
                if (sheetCountChanged || permissionsChanged) {
                    sheetData.setAll(updatedSheetData);
                    currentUserPermissionsMap.clear();
                    currentUserPermissionsMap.putAll(updatedPermissionsMap);

                    // Refresh table view
                    sheetTableView.refresh();
                }
            });
        });
    }

}




