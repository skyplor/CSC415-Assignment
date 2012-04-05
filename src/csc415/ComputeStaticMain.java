/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csc415;

import CentralProcessingClasses.ComputeStaticScore;
import CentralProcessingClasses.CustomComparator;
import Models.DocumentObj;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
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
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException
    {
        // TODO code application logic here
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
            List<DocumentObj> docs = computeScore.train("Index-tdt3", "htmlterms.txt");
//            CustomComparator scoreOrder = new CustomComparator();
//            Collections.sort(docs, scoreOrder);
            DecimalFormat df = new DecimalFormat("#.############");
//            NumberFormat nf = NumberFormat.getInstance();
            for(int i = 0; i<docs.size(); i++)
            {
                Document doc = new Document();
                String score = df.format(docs.get(i).getStaticQualityScore());
                System.out.println(score);
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
