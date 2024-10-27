package shticell.client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import deserializer.CellImplDeserializer;
import deserializer.SheetDTODeserializer;
import immutable.objects.SheetDTO;
import sheet.cell.impl.CellImpl;

public class Constants {

    // global constants
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static String JHON_DOE = "<Anonymous>";
    public final static int REFRESH_RATE = 2000;
    public final static String CHAT_LINE_FORMATTING = "%tH:%tM:%tS | %.10s: %s%n";

    // TODO change: fxml locations
    public final static String MAIN_PAGE_FXML_RESOURCE_LOCATION = "/shticell/client/component/main/shticell-main.fxml";
    public final static String LOGIN_PAGE_FXML_RESOURCE_LOCATION = "/shticell/client/component/login/login.fxml";
    public final static String DASHBOARD_FXML_RESOURCE_LOCATION = "/shticell/client/component/dashboard/dashboard.fxml";

    // Server resources locations
    public final static String BASE_DOMAIN = "localhost";
    private final static String BASE_URL = "http://" + BASE_DOMAIN + ":8080";
    private final static String CONTEXT_PATH = "/shticellApp";
    private final static String FULL_SERVER_PATH = BASE_URL + CONTEXT_PATH;

    public final static String LOGIN_PAGE = FULL_SERVER_PATH + "/login";
    public final static String USERS_LIST = FULL_SERVER_PATH + "/userslist";
    public final static String LOGOUT = FULL_SERVER_PATH + "/shticell/logout";
    public final static String ADD_SHEET = FULL_SERVER_PATH + "/addsheet";
    public final static String REQUEST_SHEETS = FULL_SERVER_PATH + "/getsheets";
    public final static String GET_SHEET_USER_PERMISSIONS = FULL_SERVER_PATH + "/getsheetuserpermissions";
    public final static String REQUEST_READER_PERMISSION = FULL_SERVER_PATH + "/requestpermission";

    // GSON instance
    public final static Gson GSON_INSTANCE = new Gson();
    public final static Gson GSON_INSTANCE_WITH_DESERIALIZERS = new GsonBuilder().registerTypeAdapter(SheetDTO.class, new SheetDTODeserializer())  // Handle SheetDTO deserialization
            .registerTypeAdapter(CellImpl.class, new CellImplDeserializer())  // Handle CellImpl deserialization
            .setPrettyPrinting()
            .create();
}
