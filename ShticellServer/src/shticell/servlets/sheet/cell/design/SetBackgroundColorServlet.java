package shticell.servlets.sheet.cell.design;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import javafx.scene.paint.Color;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;

@WebServlet("/setbackgroundcolor")
public class SetBackgroundColorServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sheetName = request.getParameter("sheetName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));
        JsonObject jsonRequest = new JsonParser().parse(request.getReader()).getAsJsonObject();

        // Parse color from the request payload
        String colorHex = jsonRequest.get("color").getAsString();

        // Use the sheet engine to update the color in the sheet cell
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        sheetManager.setBackgroundColor(row, col, Color.valueOf(colorHex));

        // Set response status based on success
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
