package shticell.servlets.sheet.readonly;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import exception.InvalidXMLFormatException;
import immutable.objects.SheetDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/sortsheet")
public class SortSheetServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Retrieve query parameters
        String sheetName = request.getParameter("sheetName");
        String fromCell = request.getParameter("fromCell");
        String toCell = request.getParameter("toCell");

        // Read and parse the JSON body to get columnsToSortBy
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }
        List<Integer> columnsToSortBy = gson.fromJson(jsonBody.toString(), new TypeToken<List<Integer>>() {}.getType());

        // Retrieve the engine and target sheet
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Perform sorting on the specified range and columns
        try {
            SheetDTO sortedSheetDTO = sheetManager.sortSheet(fromCell, toCell, columnsToSortBy);
            if (sortedSheetDTO != null) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(sortedSheetDTO));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(gson.toJson("Failed to sort sheet."));
            }
        } catch (InvalidXMLFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Error validating range: " + e.getMessage() + "\"}");
        } catch (RuntimeException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error validating range: " + e.getMessage() + "\"}");
        }
    }
}
