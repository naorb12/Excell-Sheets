package xml.handler;

import exception.InvalidXMLFormatException;
import xml.generated.STLSheet;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.FileNotFoundException;

public class XMLSheetLoaderImpl implements XMLSheetLoader {

    @Override
    public STLSheet loadAndValidateXML(String filePath) throws FileNotFoundException, JAXBException, InvalidXMLFormatException {
        File xmlFile = new File(filePath);

        if (!xmlFile.exists()) {
            throw new FileNotFoundException("XML file not found at: " + filePath);
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(STLSheet.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        STLSheet sheet = (STLSheet) unmarshaller.unmarshal(xmlFile);

        // Validate the loaded sheet according to the rules
        validateSheet(sheet);

        return sheet;
    }

    /**
     * Validates the loaded STLSheet object based on specific criteria.
     *
     * @param sheet The STLSheet object to validate.
     * @throws InvalidXMLFormatException If validation fails.
     */
    private void validateSheet(STLSheet sheet) throws InvalidXMLFormatException {
        // Perform the necessary validations
        int rows = sheet.getSTLLayout().getRows(); // Assuming getRows() returns the number of rows
        int columns = sheet.getSTLLayout().getColumns(); // Assuming getColumns() returns the number of columns

        if (rows < 1 || rows > 50 || columns < 1 || columns > 20) {
            throw new InvalidXMLFormatException("Sheet dimensions are out of bounds: " + rows + "x" + columns);
        }

        // Additional validations...
    }
}
