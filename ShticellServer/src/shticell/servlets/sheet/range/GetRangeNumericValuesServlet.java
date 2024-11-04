package shticell.servlets.sheet.range;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheet.coordinate.Coordinate;
import shticell.utils.ServletUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet("/getrangenumericvalues")
public class GetRangeNumericValuesServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");

        // Parse the JSON body to get the range of coordinates
        StringBuilder jsonBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBody.append(line);
            }
        }

        List<Coordinate> range = gson.fromJson(jsonBody.toString(), new TypeToken<List<Coordinate>>() {}.getType());

        // Retrieve the engine and the sheet to get values
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Fetch numeric values from the specified range in the sheet
        List<Double> numericValues = sheetManager.getRangeNumericValues(range);

        // Send the response as JSON
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(numericValues));
    }
}