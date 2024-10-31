package shticell.servlets.sheet;

import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.SheetManager;
import immutable.objects.SheetManagerDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/getsheets")
public class GetSheetsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        Gson gson = new Gson();

        // Get all SheetManager instances from the engine and convert them to DTOs
        Map<String, SheetManager> sheetManagerMap = engine.getSheetManagerMap();  // Map<String, SheetManager>
        Map<String, SheetManagerDTO> sheetManagerDTOMap = new HashMap<>();

        for (Map.Entry<String, SheetManager> entry : sheetManagerMap.entrySet()) {
            sheetManagerDTOMap.put(entry.getKey(), new SheetManagerDTO(entry.getValue()));  // Convert each to DTO
        }

        // Serialize the map to JSON and send it back to the client
        String sheetsJson = gson.toJson(sheetManagerDTOMap);
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(sheetsJson);
    }
}
