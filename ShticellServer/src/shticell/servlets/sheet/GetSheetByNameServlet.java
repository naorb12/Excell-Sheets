package shticell.servlets.sheet;

import com.google.gson.Gson;
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
        String userName = request.getParameter("userName");

        // Check if the sheet exists
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);
        if (sheetManager.getSheet() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Retrieve the sheet and user permissions
        SheetManagerDTO sheetManagerDTO = new SheetManagerDTO(sheetManager);

        // Determine the user permissions
        UserPermissionsDTO userPermissionsDTO = sheetManager.getUserPermissionsMap().get(userName);
        String permissionType = null;

        if (userPermissionsDTO.getPermissionStatus() != PermissionStatus.APPROVED) {
            if (userPermissionsDTO.getLastApprovedPermissionType() == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write(gson.toJson("Access denied."));
                return;
            } else {
                permissionType = userPermissionsDTO.getLastApprovedPermissionType().toString();
            }
        } else {
            permissionType = userPermissionsDTO.getPermissionType().toString();
        }

        // Create a response map with sheetManagerDTO and permissionType as a string
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("sheetDTO", sheetManagerDTO.getSheet());
        responseMap.put("permissionType", permissionType);

        // Send the response
        response.getWriter().write(gson.toJson(responseMap));
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
