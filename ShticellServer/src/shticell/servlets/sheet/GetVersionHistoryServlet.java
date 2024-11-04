package shticell.servlets.sheet;

import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import immutable.objects.SheetDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.util.Map;

@WebServlet("/getversionhistory")
public class GetVersionHistoryServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Retrieve the sheetName parameter
        String sheetName = request.getParameter("sheetName");

        // Access engine and sheet manager
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Get the version history as a Map<Integer, SheetDTO>
        Map<Integer, SheetDTO> versionHistory = sheetManager.getVersionHistory();

        if (versionHistory != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(versionHistory));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Failed to retrieve version history."));
        }
    }
}