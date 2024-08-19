package xml.handler;

import exception.InvalidXMLFormatException;
import xml.generated.STLSheet; // Import your generated root element class
import jakarta.xml.bind.JAXBException;

import java.io.FileNotFoundException;

public interface XMLSheetLoader {

    /**
     * Loads and validates an XML sheet file.
     *
     * @param filePath Path to the XML file.
     * @return The loaded STLSheet object.
     * @throws FileNotFoundException If the file is not found.
     * @throws JAXBException If there is an error during XML unmarshalling.
     * @throws InvalidXMLFormatException If the XML file does not conform to the expected format or contains invalid data.
     */
    STLSheet loadXML(String filePath) throws FileNotFoundException, JAXBException, InvalidXMLFormatException, InvalidXMLFormatException;
}
