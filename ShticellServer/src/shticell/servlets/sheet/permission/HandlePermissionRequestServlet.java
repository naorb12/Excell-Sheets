package shticell.servlets.sheet.permission;

import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import engine.permission.property.PermissionStatus;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;

@WebServlet("/handlepermission")
public class HandlePermissionRequestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();

        // Retrieve request parameters
        String sheetName = request.getParameter("sheetName");
        String userName = request.getParameter("userName");
        String permissionStatus = request.getParameter("permissionStatus");

        // Validate input parameters
        if (sheetName == null || sheetName.isEmpty() || userName == null || userName.isEmpty() || permissionStatus == null || permissionStatus.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Missing required parameters."));
            return;
        }

        // Access the ShticellEngine
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        PermissionStatus status;

        // Ensure permissionStatus is a valid enum value
        try {
            status = PermissionStatus.valueOf(permissionStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Invalid permission status."));
            return;
        }

        // Retrieve the corresponding sheet manager
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);
        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Handle the permissions in the sheet manager
        sheetManager.handlePermissions(userName, status);

        // Respond with success
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson("Permission successfully handled."));
    }
}
