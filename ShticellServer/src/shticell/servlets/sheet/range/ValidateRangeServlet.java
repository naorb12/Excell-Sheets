package shticell.servlets.sheet.range;

import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheet.coordinate.Coordinate;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/validaterange")
public class ValidateRangeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        String fromCell = request.getParameter("fromCell");
        String toCell = request.getParameter("toCell");

        // Retrieve the engine and validate the range
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Sheet not found.\"}");
            return;
        }

        // Perform the range validation and collect coordinates
        try {
            List<Coordinate> rangeCoordinates = sheetManager.validateRange(fromCell, toCell);
            response.getWriter().write(new Gson().toJson(rangeCoordinates));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Error validating range: " + e.getMessage() + "\"}");
        }
    }
}
