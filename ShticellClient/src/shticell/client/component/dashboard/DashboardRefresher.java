package shticell.client.component.dashboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import deserializer.CellImplDeserializer;
import deserializer.SheetDTODeserializer;
import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import shticell.client.util.Constants;
import shticell.client.util.http.HttpClientUtil;
import sheet.cell.impl.CellImpl;
import engine.manager.dto.SheetManagerDTO;
import shticell.client.util.http.HttpMethod;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.TimerTask;

public class DashboardRefresher extends TimerTask {

    private final ObservableList<SheetDTO> sheetData;
    private final TableView<SheetDTO> sheetTableView;

    public DashboardRefresher(ObservableList<SheetDTO> sheetData, TableView<SheetDTO> sheetTableView) {
        this.sheetData = sheetData;
        this.sheetTableView = sheetTableView;
    }

    @Override
    public void run() {
        String url = Constants.REQUEST_SHEETS;

        // Use the advanced runReqAsyncWithJson
        HttpClientUtil.runReqAsyncWithJson(url, HttpMethod.GET, null, (responseBody) -> {
            Platform.runLater(() -> {
                if (responseBody == null) {
                    System.out.println("No sheets found or response body is null.");
                    return;
                }

                // Deserialize response into a map of SheetManagerDTO
                Type sheetManagerMapType = new TypeToken<Map<String, SheetManagerDTO>>() {}.getType();
                Map<String, SheetManagerDTO> sheetManagers = Constants.GSON_INSTANCE_WITH_DESERIALIZERS.fromJson(responseBody, sheetManagerMapType);

                // Clear current data
                sheetData.clear();
                // Add sheets from all SheetManagerDTOs
                for (SheetManagerDTO managerDTO : sheetManagers.values()) {
                    sheetData.add(managerDTO.getSheet());
                }

                // Refresh the table view
                sheetTableView.refresh();
            });
        });
    }

}

