package xml.handler;

import exception.InvalidXMLFormatException;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import xml.generated.STLSheet;

import java.io.File;
import java.io.FileNotFoundException;

public class XMLSheetLoaderImpl implements XMLSheetLoader {

    @Override
    public STLSheet loadXML(String filePath) throws FileNotFoundException, JAXBException, InvalidXMLFormatException, InvalidXMLFormatException {
        File xmlFile = new File(filePath);

        // Check if the file exists
        if (!xmlFile.exists()) {
            throw new FileNotFoundException("XML file not found at: " + filePath);
        }

        // Load the XML file into an STLSheet object
        JAXBContext jaxbContext = JAXBContext.newInstance(STLSheet.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (STLSheet) unmarshaller.unmarshal(xmlFile);
    }

}
