package shticell.servlets.sheet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/setcell")
public class SetCellServlet extends HttpServlet {
    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Get parameters
        String sheetName = request.getParameter("sheetName");
        int row = Integer.parseInt(request.getParameter("row"));
        int column = Integer.parseInt(request.getParameter("column"));
        String userNameUpdated = request.getParameter("userName");

        // Read input JSON from the request body
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }

        // Parse JSON to extract "input" field
        String input;
        try {
            JsonObject jsonObject = gson.fromJson(jsonBody.toString(), JsonObject.class);
            input = jsonObject.get("input").getAsString();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Invalid JSON format for input field."));
            return;
        }

        // Retrieve the engine and the sheet manager
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        try {
            // Update the cell value
            synchronized (sheetManager) {
                sheetManager.setCell(row, column, input, userNameUpdated);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson("Cell updated successfully."));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeErrorResponse(response, "Failed to update cell: " + e.getMessage());
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        JsonObject errorObject = new JsonObject();
        errorObject.addProperty("error", errorMessage);
        response.getWriter().write(gson.toJson(errorObject));
    }
}

