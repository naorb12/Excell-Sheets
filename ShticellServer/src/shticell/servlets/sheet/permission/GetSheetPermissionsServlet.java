package shticell.servlets.sheet.permission;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import engine.ShticellEngine;
import immutable.objects.UserPermissionsDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet("/getsheetuserpermissions")
public class GetSheetPermissionsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();  // Pretty printing for better readability

        // Get the sheet name from the request parameter
        String sheetName = request.getParameter("sheetName");

        // Get the user permissions map from the engine
        Map<String, UserPermissionsDTO> userPermissionsDTOMap = engine.getSheetManagerMap().get(sheetName).getUserPermissionsMap();

        if (userPermissionsDTOMap == null) {
            // If no permissions are found for the sheet, return an error
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\":\"No permissions found for sheet: " + sheetName + "\"}");
            return;
        }

        // Convert the Map to JSON
        String json = gson.toJson(userPermissionsDTOMap);

        // Set the content type of the response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Write the JSON to the response body
        response.getWriter().write(json);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
