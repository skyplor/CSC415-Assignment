package Part1CentralProcessingClasses;

import Part1Models.DocumentObj;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author Sky
 */
public class ComputeStaticScore
{
    /**
     * Compute the static quality score of each document in the index and returns
     * the list of DocumentObj consisting of the document object and static quality score
     *
     * @param textindex the filename of the index of documents
     * @param htmlfile the name of the text file containing the sports terms collected from HtmlParser.java
     * @return a list of DocumentObj objects.
     * @throws Exception if one is thrown.
     */
    public List<DocumentObj> compute(String textindex, String htmlfile) throws Exception
    {
        IndexReader textreader = null;
        Directory textdirectory;
        List<DocumentObj> matchedDocIds = new ArrayList<>();
        String htmlterms;
        StringBuilder contents = new StringBuilder();
        try
        {
            BufferedReader htmlinput = new BufferedReader(new FileReader(htmlfile));
            try
            {
                String line;
                while ((line = htmlinput.readLine()) != null)
                {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            }
            finally
            {
                htmlinput.close();
            }

            htmlterms = contents.toString();
            String[] htmltokens = htmlterms.split("\r\n");
            textdirectory = FSDirectory.open(new File(textindex));
            textreader = IndexReader.open(textdirectory);
            matchedDocIds = computeMatchedDocIds(textreader);

            for (int i = 0; i < htmltokens.length; i++)
            {
                if (htmltokens[i].equals(""))
                {
                }
                else
                {
                    Term htmlterm = new Term("text", htmltokens[i]);
                    TermDocs termDocs = textreader.termDocs(htmlterm);
                    while (termDocs.next())
                    {
                        int docId = termDocs.doc();
                        int frequency = termDocs.freq();
                        for (int docObj = 0; docObj < matchedDocIds.size(); docObj++)
                        {
                            if (matchedDocIds.get(docObj).getDocID() == (docId))
                            {
                                matchedDocIds.get(docObj).setFrequency(matchedDocIds.get(docObj).getFrequency() + frequency);
                            }
                        }
                    }
                }
            }

            for (int doc = 0; doc < matchedDocIds.size(); doc++)
            {
                int termfrequency = matchedDocIds.get(doc).getFrequency();
                double score = ((double) termfrequency) / ((double) htmltokens.length);
                matchedDocIds.get(doc).setStaticQualityScore(score);
            }
        }
        finally
        {
            if (textreader != null)
            {
                textreader.close();
            }
        }
        return matchedDocIds;
    }

    private List<DocumentObj> computeMatchedDocIds(IndexReader textreader) throws IOException
    {
        List<DocumentObj> matchedDocIds = new ArrayList<>();

        IndexSearcher searcher = new IndexSearcher(textreader);

        TopScoreDocCollector collector = TopScoreDocCollector.create(40000, true);

        MatchAllDocsQuery query = new MatchAllDocsQuery();

        searcher.search(query, collector);

        ScoreDoc[] hits = collector.topDocs().scoreDocs;

        for (int i = 0; i < hits.length; ++i)
        {
            DocumentObj dObj = new DocumentObj();
            dObj.setDocID(hits[i].doc);

            Document docu = searcher.doc(hits[i].doc);
            dObj.setDoc(docu);

            matchedDocIds.add(dObj);
        }

        // Close searcher when indexes are no longer needed.
        searcher.close();
        return matchedDocIds;
    }
}
