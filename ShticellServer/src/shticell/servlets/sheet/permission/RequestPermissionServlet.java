package shticell.servlets.sheet.permission;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import engine.permission.UserPermissions;
import immutable.objects.UserPermissionsDTO;
import engine.permission.property.PermissionStatus;
import engine.permission.property.PermissionType;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;

@WebServlet("/requestpermission")
public class RequestPermissionServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set content type
        response.setContentType("application/json");
        Gson gson = new Gson();

        // Retrieve request parameters
        String sheetName = request.getParameter("sheetName");
        String userName = request.getParameter("userName");
        String permissionType = request.getParameter("permissionType");  // Either 'reader' or 'writer'

        // Access the ShticellEngine
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());

        // Find the corresponding sheet from the engine
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);
        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        UserPermissionsDTO userPermissionsDTO = sheetManager.getUserPermissionsMap().get(userName);
        if (userPermissionsDTO != null && userPermissionsDTO.getPermissionType() == PermissionType.OWNER) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("error", "You are already the owner of this sheet.");
            response.getWriter().write(gson.toJson(errorObject));
            return;
        }

        if (userPermissionsDTO != null && userPermissionsDTO.getPermissionType() == PermissionType.convertStringToType(permissionType)
                && userPermissionsDTO.getPermissionStatus() == PermissionStatus.APPROVED) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            JsonObject errorObject = new JsonObject();
            errorObject.addProperty("error", "You already have " + permissionType + " permissions to this sheet.");
            response.getWriter().write(gson.toJson(errorObject));
            return;
        }

        // Create a new UserPermissionsDTO for the requested permission
        UserPermissions newPermission = new UserPermissions(userName, PermissionType.convertStringToType(permissionType), PermissionStatus.PENDING);

        // Add the permission to the permissions map
        sheetManager.addUserPermissions(newPermission);

        // Respond with success
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson("Permission request successfully submitted."));
    }
}

