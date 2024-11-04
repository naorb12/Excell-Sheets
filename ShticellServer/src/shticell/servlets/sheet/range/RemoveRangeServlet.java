package shticell.servlets.sheet.range;

import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/removerange")
public class RemoveRangeServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Get and decode the parameters
        String sheetName = request.getParameter("sheetName");
        String rangeName = request.getParameter("rangeName");

        if (sheetName == null || rangeName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Missing required parameters."));
            return;
        }

        sheetName = URLDecoder.decode(sheetName, StandardCharsets.UTF_8);
        rangeName = URLDecoder.decode(rangeName, StandardCharsets.UTF_8);

        // Retrieve the ShticellEngine and the sheet manager
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        try {
            // Attempt to remove the range
            sheetManager.removeRange(rangeName);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson("Range removed successfully."));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Failed to remove range: " + e.getMessage()));
        }
    }
}
