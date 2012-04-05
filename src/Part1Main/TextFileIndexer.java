package Part1Main;

import Part1CentralProcessingClasses.IndexingCPC;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This terminal application creates an Apache Lucene index in a folder and adds files into this index
 */
public class TextFileIndexer
{

    private IndexWriter writer;
    private ArrayList<File> queue = new ArrayList<>();

    public TextFileIndexer()
    {
    }

    public void runtdt3Indexer() throws IOException
    {

        String fileDirectory = "Index-tdt3";
        String s = "";

//        checkDirectory(fileDirectory);

        TextFileIndexer indexer = null;
        try
        {
            indexer = new TextFileIndexer(fileDirectory);
        }
        catch (Exception ex)
        {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }

        //===================================================
        //read input from user until he enters q for quit
        //===================================================
        try
        {
            s = "tdt3";
            //try to add file into the index
            indexer.indexFileOrDirectory(s);
        }
        catch (Exception e)
        {
            System.out.println("Error indexing " + s + " : " + e.getMessage());
        }

        // ===================================================
        // After adding, we always have to call the
        // closeIndex, otherwise the index is not created    
        // ===================================================
        indexer.closeIndex();
    }

    /**
     * Constructor
     *
     * @param indexDir the name of the folder in which the index should be created
     * @throws java.io.IOException
     */
    TextFileIndexer(String indexDir) throws IOException
    {
        FSDirectory dir = FSDirectory.open(new File(indexDir));

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);

        writer = new IndexWriter(dir, config);
    }

    /**
     * Indexes a file or directory
     *
     * @param fileName the name of a text file or a folder we wish to add to the index
     * @throws java.io.IOException
     */
    public void indexFileOrDirectory(String fileName) throws IOException
    {
        // =========================================================
        // Gets the list of files in a folder (if user has submitted
        // the name of a folder) or gets a single file name (is user
        // has submitted only the file name) 
        // =========================================================
        addFiles(new File(fileName));

        int originalNumDocs = writer.numDocs();
        for (File f : queue)
        {
            try
            {
                // Pass the file as an XML and get the list of elements
                NodeList nodeList = IndexingCPC.parseXML(f);
                Document doc = new Document();

                int totalDocs = nodeList.getLength();
                for (int i = 0; i < totalDocs; i++)
                {
                    // =====================
                    // Add contents of file
                    // =====================
                    Node n = nodeList.item(i);
                    String text = n.getTextContent();
                    if (n.getNodeName().equals("DOCNO"))
                    {
                        doc.add(new Field("docno", text, Field.Store.YES, Field.Index.ANALYZED));
                        doc.add(new Field("date", text.substring(4, 12), Field.Store.YES, Field.Index.ANALYZED));
                        doc.add(new Field("source", text.substring(0, 4), Field.Store.YES, Field.Index.ANALYZED));
                    }
                    if (n.getNodeName().equals("TEXT"))
                    {
                        doc.add(new Field("text", text, Field.Store.YES, Field.Index.ANALYZED));
                    }
                }
                writer.addDocument(doc);
                System.out.println("Added: " + f);
            }
            catch (DOMException | IOException e)
            {
                System.out.println("Could not add: " + f);
            }
        }
        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");

        queue.clear();
    }

    private void addFiles(File file)
    {

        if (!file.exists())
        {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory())
        {
            for (File f : file.listFiles())
            {
                addFiles(f);
            }
        }
        else
        {
            String filename = file.getName().toLowerCase();
            // ======================
            // Only index text files
            // ======================
            if (filename.endsWith(".htm") || filename.endsWith(".html") || filename.endsWith(".xml") || filename.endsWith(".txt"))
            {
                queue.add(file);
            }
            else
            {
                System.out.println("Skipped " + filename);
            }
        }
    }

    /**
     * Close the index.
     *
     * @throws java.io.IOException
     */
    public void closeIndex() throws IOException
    {
        writer.close();
    }

//    /*
//     * Delete tdt3 index folder if exists.
//     */
//    private void checkDirectory(String fileDirectory)
//    {
//        File directory = new File(fileDirectory);
//
//        //make sure directory exists
//        if (directory.exists())
//        {
//            try
//            {
//                directory.delete();
//            }
//            catch (Exception e)
//            {
//                System.out.println("File deletion error: " + e);
//            }
//        }
//    }
}
