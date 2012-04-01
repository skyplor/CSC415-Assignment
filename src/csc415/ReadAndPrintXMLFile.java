/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csc415;

/**
 *
 * @author Sky
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import org.w3c.dom.Document;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ReadAndPrintXMLFile
{

    public static NodeList parseXML(File filename)
    {
        NodeList listOfDocs = null;
        try
        {
            // Open up the text file
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String txtContent = "<?xml version=\"1.1\"?>"; // Start the string with the XML header

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

            // normalize text representation
//            doc.getDocumentElement().normalize();
            System.out.println("Root element of the doc is " + doc.getDocumentElement().getNodeName());


//            listOfDocs = doc.getElementsByTagName(doc.getDocumentElement().getNodeName());
            listOfDocs = doc.getChildNodes().item(0).getChildNodes();
            return listOfDocs;
//            int totalDocs = listOfDocs.getLength();
//            System.out.println("Total no of documents : " + totalDocs);

//            for (int s = 0; s < listOfDocs.getLength(); s++)
//            {
//
//
//                Node firstDocNode = listOfDocs.item(s);
//                if (firstDocNode.getNodeType() == Node.ELEMENT_NODE)
//                {
//
//
//                    Element firstDocElement = (Element) firstDocNode;
//
//                    //-------
//                    NodeList firstNameList = firstDocElement.getElementsByTagName("TEXT");
//                    Element firstNameElement = (Element) firstNameList.item(0);
//
//                    NodeList textFNList = firstNameElement.getChildNodes();
//                    System.out.println("Text : " + ((Node) textFNList.item(0)).getNodeValue().trim());
//
//                    //-------
//                    NodeList docNo = firstDocElement.getElementsByTagName("DOCNO");
//                    Element docNoElement = (Element) docNo.item(0);
//
//                    NodeList docno = docNoElement.getChildNodes();
//                    System.out.println("Doc No. : " + ((Node) docno.item(0)).getNodeValue().trim());
//
////                    NodeList lastNameList = firstDocElement.getElementsByTagName("last");
////                    Element lastNameElement = (Element)lastNameList.item(0);
////
////                    NodeList textLNList = lastNameElement.getChildNodes();
////                    System.out.println("Last Name : " + 
////                           ((Node)textLNList.item(0)).getNodeValue().trim());
//
//                    //----
////                    NodeList ageList = firstDocElement.getElementsByTagName("age");
////                    Element ageElement = (Element)ageList.item(0);
////
////                    NodeList textAgeList = ageElement.getChildNodes();
////                    System.out.println("Age : " + 
////                           ((Node)textAgeList.item(0)).getNodeValue().trim());
//
//                    //------
//
//
//                }//end of if clause
//
//
//            }//end of for loop with s var


        }
        catch (SAXParseException err)
        {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        }
        catch (SAXException e)
        {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        }
        catch (Throwable t)
        {
            t.printStackTrace();
        }

        return listOfDocs;
        //System.exit (0);

    }//end of main
}
