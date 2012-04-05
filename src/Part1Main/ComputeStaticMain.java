package Part1Main;

import Part1CentralProcessingClasses.ComputeStaticScore;
import Part1Models.DocumentObj;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Sky
 */
public class ComputeStaticMain
{

    private IndexWriter writer;

    public ComputeStaticMain()
    {
    }

    public void runComputeStaticMain() throws IOException
    {
        String fileDirectory = "Index-StaticScore";

        ComputeStaticMain indexer = null;

        try
        {
            indexer = new ComputeStaticMain(fileDirectory);
        }
        catch (Exception ex)
        {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }
        indexer.indexStaticScore();

        indexer.closeIndex();
    }

    ComputeStaticMain(String indexDir) throws IOException
    {
        FSDirectory dir = FSDirectory.open(new File(indexDir));

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);

        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);

        writer = new IndexWriter(dir, config);
    }

    private void indexStaticScore()
    {
        ComputeStaticScore computeScore = new ComputeStaticScore();
        try
        {
            List<DocumentObj> docs = computeScore.compute("Index-tdt3", "htmlterms.txt");
            DecimalFormat df = new DecimalFormat("#.############");
            for (int i = 0; i < docs.size(); i++)
            {
                Document doc = new Document();
                String score = df.format(docs.get(i).getStaticQualityScore());
                doc.add(new Field("score", score, Field.Store.YES, Field.Index.NOT_ANALYZED));
                writer.addDocument(doc);
            }
        }
        catch (Exception ex)
        {
            Logger.getLogger(ComputeStaticMain.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error Indexing");
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
}
