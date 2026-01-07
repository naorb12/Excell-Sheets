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

@WebServlet("/peekversion")
public class PeekVersionServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Retrieve parameters
        String sheetName = request.getParameter("sheetName");
        int version;
        try {
            version = Integer.parseInt(request.getParameter("version"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Invalid version number."));
            return;
        }

        // Access engine and sheet manager
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Retrieve the specified version of the sheet
        SheetDTO sheetDTO = sheetManager.peekVersion(version);
        if (sheetDTO != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(sheetDTO));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Failed to retrieve sheet version."));
        }
    }
}