package shticell.servlets.sheet.readonly;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import immutable.objects.SheetDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheet.coordinate.Coordinate;
import shticell.utils.ServletUtils;

import java.io.IOException;

@WebServlet("/applyDynamicAnalysis")
public class DynamicAnalysisServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));

        // Parse the JSON body to retrieve the new value
        JsonObject jsonBody = gson.fromJson(request.getReader(), JsonObject.class);
        Number newValue = jsonBody.get("newValue").getAsNumber();

        // Retrieve the engine and apply the dynamic analysis on the sheet
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Perform dynamic analysis (your implementation in SheetManager)
        SheetDTO updatedSheetDTO = sheetManager.applyDynamicAnalysis(new Coordinate(row, col), newValue);

        if (updatedSheetDTO != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(updatedSheetDTO));
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Failed to apply dynamic analysis."));
        }
    }
}
