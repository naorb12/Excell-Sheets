package shticell.servlets.sheet;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import engine.ShticellEngine;
import immutable.objects.SheetManagerDTO;
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

    private static final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");

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
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeErrorResponse(response, "Failed to add sheet: " + e.getMessage());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeErrorResponse(response, "Failed to add sheet: " + e.getMessage());
            return;
        }

        SheetManagerDTO sheetManagerDTO = engine.getSheetManagerDTO(sheetName);

        // Serialize the SheetManagerDTO to JSON and send it back to the client
        String sheetManagerJson = gson.toJson(sheetManagerDTO);

        // Send back the JSON response
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(sheetManagerJson);
    }

    private void writeErrorResponse(HttpServletResponse response, String errorMessage) throws IOException {
        JsonObject errorObject = new JsonObject();
        errorObject.addProperty("error", errorMessage);
        response.getWriter().write(gson.toJson(errorObject));
    }
}



