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
import sheet.coordinate.Coordinate;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/createnewrange")
public class CreateNewRangeServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        // Extract parameters from the request
        String sheetName = request.getParameter("sheetName");
        String rangeName = request.getParameter("rangeName");
        String fromCell = request.getParameter("fromCell");
        String toCell = request.getParameter("toCell");

        if (sheetName == null || rangeName == null || fromCell == null || toCell == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Missing required parameters."));
            return;
        }

        // Decode URL-encoded parameters
        sheetName = URLDecoder.decode(sheetName, StandardCharsets.UTF_8);
        rangeName = URLDecoder.decode(rangeName, StandardCharsets.UTF_8);
        fromCell = URLDecoder.decode(fromCell, StandardCharsets.UTF_8);
        toCell = URLDecoder.decode(toCell, StandardCharsets.UTF_8);

        // Retrieve the ShticellEngine and the corresponding sheet manager
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Try to create the new range in the specified sheet
        try {
            List<Coordinate> newRange = sheetManager.createNewRange(rangeName, fromCell, toCell);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(newRange));
        } catch (InvalidXMLFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Invalid range format: " + e.getMessage()));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson("Failed to create range: " + e.getMessage()));
        }
    }
}
