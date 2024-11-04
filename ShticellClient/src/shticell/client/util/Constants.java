package shticell.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import deserializer.CellImplDeserializer;
import deserializer.ColorTypeAdapter;
import deserializer.SheetDTODeserializer;
import immutable.objects.SheetDTO;
import javafx.scene.paint.Color;
import sheet.cell.impl.CellImpl;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String JHON_DOE = "<Anonymous>";
    public final static int REFRESH_RATE = 2000;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";

    // fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/shticell/client/component/main/shticell-main.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/shticell/client/component/login/login.fxml";
    public final static String DASHBOARD_FXML_RESOURCE_LOCATION = "/shticell/client/component/dashboard/dashboard.fxml";
    public final static String SHEET_MAIN_FXML_RESOURCE_LOCATION = "/shticell/client/component/sheet/main/sheet-main-controller.fxml";


    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/shticellApp";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    // Users Management URL
    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String LOGOUT = FULL_SERVER_PATH + "/shticell/logout";

    // Sheet URL
    public final static String ADD_SHEET = FULL_SERVER_PATH + "/addsheet";
    public final static String REQUEST_SHEETS = FULL_SERVER_PATH + "/getsheets";
    public final static String PEEK_VERSION_URL = FULL_SERVER_PATH + "/peekversion";
    public final static String GET_SHEET_BY_NAME = FULL_SERVER_PATH + "/getsheetbyname";
    public final static String GET_CELL_URL = FULL_SERVER_PATH + "/getcell";
    public final static String SET_CELL_URL = FULL_SERVER_PATH + "/setcell";
    public final static String GET_VERSION_HISTORY_URL = FULL_SERVER_PATH + "/getversionhistory";

    // Cell Design URL
    public final static String SET_BACKGROUND_COLOR = FULL_SERVER_PATH + "/setbackgroundcolor";
    public final static String SET_TEXT_COLOR = FULL_SERVER_PATH + "/settextcolor";
    public final static String UNDO_COLOR = FULL_SERVER_PATH + "/undocolor";

    // Permission URL
    public final static String GET_SHEET_USER_PERMISSIONS = FULL_SERVER_PATH + "/getsheetuserpermissions";
    public final static String REQUEST_PERMISSION = FULL_SERVER_PATH + "/requestpermission";
    public final static String HANDLE_PERMISSION = FULL_SERVER_PATH + "/handlepermission";

    // Range URL
    public final static String CREATE_NEW_RANGE_URL = FULL_SERVER_PATH + "/createnewrange";
    public final static String REMOVE_RANGE_URL = FULL_SERVER_PATH + "/removerange";
    public final static String VALIDATE_RANGE_URL = FULL_SERVER_PATH + "/validaterange";
    public final static String GET_RANGE_NUMERIC_VALUES_URL = FULL_SERVER_PATH + "/getrangenumericvalues";
    public final static String GET_WORDS_FROM_COLUMN_AND_RANGE_URL = FULL_SERVER_PATH + "/getwordsfromcolumnandrange";

    // Readonly URL
    public final static String SORT_SHEET_URL = FULL_SERVER_PATH + "/sortsheet";
    public final static String FILTER_SHEET_URL = FULL_SERVER_PATH + "/filtersheet";
    public final static String APPLY_DYNAMIC_ANALYSIS_URL = FULL_SERVER_PATH + "/applydynamicanalysis";







    // GSON instance
    public final static Gson GSON_INSTANCE_WITH_DESERIALIZERS = new GsonBuilder().registerTypeAdapter(SheetDTO.class, new SheetDTODeserializer())  // Handle SheetDTO deserialization
            .registerTypeAdapter(CellImpl.class, new CellImplDeserializer())  // Handle CellImpl deserialization
            .registerTypeAdapter(Color.class, new ColorTypeAdapter())
            .setPrettyPrinting()
            .create();
}
