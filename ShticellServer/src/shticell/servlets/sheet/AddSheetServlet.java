package shticell.servlets.sheet;


import com.google.gson.Gson;
import engine.ShticellEngine;
import engine.manager.dto.SheetManagerDTO;
import exception.InvalidXMLFormatException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import shticell.utils.ServletUtils;
import xml.generated.STLSheet;

import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/addsheet")
public class AddSheetServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        BufferedReader reader = request.getReader();
        Gson gson = new Gson();

        // Deserialize the JSON request body into an STLSheet (since the client sends this)
        STLSheet sheet = gson.fromJson(reader, STLSheet.class);

        // Get the ShticellEngine and SheetManager from the servlet context
        ShticellEngine engine = ServletUtils.getEngine(getServletContext());
        String sheetName = request.getParameter("sheetName");
        String owner = request.getParameter("owner");

        try {
            engine.mapSTLSheet(sheetName, sheet, owner);  // Add the sheet to the engine
        } catch (InvalidXMLFormatException e) {
            throw new RuntimeException(e);
        }

        SheetManagerDTO sheetManagerDTO = engine.getSheetManagerDTO(sheetName);

        // Serialize the SheetManagerDTO to JSON and send it back to the client
        String sheetManagerJson = gson.toJson(sheetManagerDTO);

        // Send back the JSON response
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(sheetManagerJson);
    }
}



