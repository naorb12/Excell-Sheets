package shticell.servlets;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import engine.ShticellEngine;
import exception.InvalidXMLFormatException;
import immutable.objects.CellDTO;
import immutable.objects.SheetDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sheet.coordinate.Coordinate;
import sheet.impl.SheetImpl;
import shticell.utils.ServletUtils;
import xml.generated.STLSheet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

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

        try {
            engine.mapSTLSheet(sheetName, sheet);  // Add the sheet to the engine
        } catch (InvalidXMLFormatException e) {
            throw new RuntimeException(e);
        }


        // Serialize the SheetDTOImpl to JSON and send it back to the client
        String sheetJson = gson.toJson(engine.getSheetManagerMap().get(sheetName).getSheet());

        // Send back the JSON response
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(sheetJson);
    }
}



