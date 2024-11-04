package shticell.servlets.sheet;

import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import immutable.objects.CellDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/getcell")
public class GetCellServlet extends HttpServlet {

    private static final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String sheetName = request.getParameter("sheetName");
        int row;
        int column;

        // Parse row and column from parameters, with error handling
        try {
            row = Integer.parseInt(request.getParameter("row"));
            column = Integer.parseInt(request.getParameter("column"));
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson("Invalid row or column number."));
            return;
        }

        // Retrieve the ShticellEngine instance and locate the sheet
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        SheetManager sheetManager = engine.getSheetManagerMap().get(sheetName);

        if (sheetManager == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Sheet not found."));
            return;
        }

        // Fetch the cell from the sheet manager
        CellDTO cellDTO = sheetManager.getCell(row, column);

        if (cellDTO == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson("Cell not found."));
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.write(gson.toJson(cellDTO));
            }
        }
    }
}
