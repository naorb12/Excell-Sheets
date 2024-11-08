package shticell.servlets.sheet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import engine.permission.property.PermissionStatus;
import engine.permission.property.PermissionType;
import immutable.objects.SheetManagerDTO;
import immutable.objects.UserPermissionsDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/getsheetbyname")
public class GetSheetByNameServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        response.setContentType("application/json");
        Gson gson = new Gson();

        // Retrieve request parameters
        String sheetName = request.getParameter("sheetName");
        String userName = request.getParameter("userName"); // Can be null if not provided

        // Check if the sheet exists
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);
        if (sheetManager == null || sheetManager.getSheet() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Prepare the response map with the sheet data
        Map<String, Object> responseMap = new HashMap<>();
        synchronized (sheetManager) {
            responseMap.put("sheetDTO", sheetManager.getSheet());

            // Only check permissions if a userName was provided
            if (userName != null && !userName.isEmpty()) {
                // Retrieve user permissions
                UserPermissionsDTO userPermissionsDTO = sheetManager.getUserPermissionsMap().get(userName);
                String permissionType = null;

                // Check if the user has approved permissions
                if (userPermissionsDTO != null && userPermissionsDTO.getPermissionStatus() == PermissionStatus.APPROVED) {
                    permissionType = userPermissionsDTO.getPermissionType().toString();
                } else if (userPermissionsDTO != null && userPermissionsDTO.getLastApprovedPermissionType() != null) {
                    permissionType = userPermissionsDTO.getLastApprovedPermissionType().toString();
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    JsonObject error = new JsonObject();
                    error.addProperty("error", "Access denied.");
                    response.getWriter().write(gson.toJson(error));

                    return;
                }

                // Add permission type to the response map
                responseMap.put("permissionType", permissionType);
            } else {
                // If no userName provided, default permission to null
                responseMap.put("permissionType", null);
            }
        }

        // Send the response
        response.getWriter().write(gson.toJson(responseMap));
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

