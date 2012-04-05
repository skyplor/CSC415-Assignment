package Part1CentralProcessingClasses;

/**
 *
 * @author Sky
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class IndexingCPC
{
    public static NodeList parseXML(File filename)
    {
        NodeList listOfDocs = null;
        try
        {
            // Open up the text file
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String txtContent = "";

            // Read all the text content to the string variable
            while (br.ready())
            {
                txtContent += br.readLine();
            }

            // Replace all instances of invalid characters for XML
            txtContent = txtContent.replace("&", "&amp;");
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

            Document doc = docBuilder.parse(new InputSource(new StringReader(txtContent)));

            listOfDocs = doc.getChildNodes().item(0).getChildNodes();
            return listOfDocs;
        }
        catch (SAXParseException err)
        {
            System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());
        }
        catch (SAXException e)
        {
            Exception x = e.getException();
        }
        catch (Throwable t)
        {
        }
        return listOfDocs;
    }
}
