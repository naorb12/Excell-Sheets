package shticell.servlets.sheet.cell.design;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import immutable.objects.CellDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javafx.scene.paint.Color;
import sheet.cell.api.Cell;
import shticell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/settextcolor")
public class SetTextColorServlet extends HttpServlet {


    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));

        // Parse JSON body to get color information
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }

        JsonObject jsonObject = gson.fromJson(jsonBody.toString(), JsonObject.class);
        String colorHex = jsonObject.get("color").getAsString();

        // Retrieve sheet manager
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Set the text color for the specified cell
        CellDTO cell = sheetManager.getCell(row, col);
        if (cell != null) {
            sheetManager.setTextColor(row, col, Color.valueOf(colorHex));
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson("Text color updated successfully."));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Cell not found."));
        }
    }
}
