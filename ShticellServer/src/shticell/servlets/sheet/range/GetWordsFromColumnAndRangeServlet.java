package shticell.servlets.sheet.range;

import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import exception.InvalidXMLFormatException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.util.Set;

@WebServlet("/getwordsfromcolumnandrange")
public class GetWordsFromColumnAndRangeServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Retrieve parameters
        String sheetName = request.getParameter("sheetName");
        String column = request.getParameter("column");
        String fromCell = request.getParameter("fromCell");
        String toCell = request.getParameter("toCell");

        // Access engine and sheet manager
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Attempt to retrieve words from the specified column and range
        try {
            Set<String> wordsSet = sheetManager.getWordsFromColumnAndRange(column, fromCell, toCell);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(wordsSet));
        } catch (InvalidXMLFormatException e) {
            // Handle invalid range format gracefully
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Invalid coordinate format: " + e.getMessage()));
        } catch (Exception e) {
            // General error handling for any other exception
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Failed to retrieve words due to an internal error."));
        }
    }
}
