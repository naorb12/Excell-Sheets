package shticell.servlets.sheet.cell.design;

import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import immutable.objects.CellDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javafx.scene.paint.Color;
import shticell.utils.ServletUtils;

import java.io.IOException;

@WebServlet("/undocolor")
public class UndoColorServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");

        String sheetName = request.getParameter("sheetName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));

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
            sheetManager.undoColor(row, col);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson("Color removed successfully."));
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Cell not found."));
        }

            }
}
