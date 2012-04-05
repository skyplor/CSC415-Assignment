package Part1CentralProcessingClasses;

import java.io.File;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.ParallelReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.search.function.FieldScoreQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Sky
 */
public class Part1LuceneSearchEngineProcessor
{

    private Directory directory, scoreDirectory;
    private StandardAnalyzer analyzer;
    private QueryParser parsertext, parsersource, parserdate;
    private Boolean topicSpecific;
    private Integer maxResult;
    private ParallelReader preader;

    public Part1LuceneSearchEngineProcessor(String indexDirectory, String indexScoreDirectory, Integer maxResult, Boolean topicSpecific) throws IOException
    {
        directory = FSDirectory.open(new File(indexDirectory));
        if (topicSpecific)
        {
            preader = new ParallelReader();
            scoreDirectory = FSDirectory.open(new File(indexScoreDirectory));
            preader.add(IndexReader.open(directory, false));
            preader.add(IndexReader.open(scoreDirectory, false));

        }
        analyzer = new StandardAnalyzer(Version.LUCENE_35);
        parsertext = new QueryParser(Version.LUCENE_35, "text", analyzer);
        parsersource = new QueryParser(Version.LUCENE_35, "source", analyzer);
        parserdate = new QueryParser(Version.LUCENE_35, "date", analyzer);
        this.maxResult = maxResult;
        this.topicSpecific = topicSpecific;
    }

    public void searchIndex(String searchtext, String searchsource, String searchdate) throws ParseException, IOException
    {
        BooleanQuery query = new BooleanQuery();
        if (!searchtext.equals(""))
        {
            query.add(parsertext.parse(searchtext), BooleanClause.Occur.MUST);
        }
        if (!searchsource.equals(""))
        {
            query.add(parsersource.parse(searchsource), BooleanClause.Occur.MUST);
        }
        if (!searchdate.equals(""))
        {
            query.add(parserdate.parse(searchdate), BooleanClause.Occur.MUST);
        }
        IndexReader reader = null;
        IndexSearcher searcher;

        if (topicSpecific)
        {
            searcher = new IndexSearcher(preader);
        }
        else
        {
            reader = IndexReader.open(directory);
            searcher = new IndexSearcher(reader);
        }

        FieldScoreQuery fsQuery = new FieldScoreQuery("score", FieldScoreQuery.Type.FLOAT);
        fsQuery.setBoost(10);

        CustomScoreQuery cq = null;
        if (topicSpecific)
        {
            cq = new CustomScoreQuery(query, fsQuery)
            {

                @Override
                protected CustomScoreProvider getCustomScoreProvider(IndexReader reader)
                {
                    return new CustomScoreProvider(reader)
                    {

                        @Override
                        public float customScore(int doc, float subQueryScore, float valSrcScore)
                        {
                            return subQueryScore + (valSrcScore);
                        }
                    };
                }
            };
        }
        TopScoreDocCollector collector = TopScoreDocCollector.create(maxResult, true);

        ScoreDoc[] hits;
        if (!topicSpecific)
        {
            searcher.search(query, collector);
        }
        else
        {
            searcher.search(cq, collector);
        }

        hits = collector.topDocs().scoreDocs;
        System.out.println("Found " + hits.length + " hits.");
        System.out.println();
        for (int i = 0; i < hits.length; ++i)
        {
            int docId = hits[i].doc;
            Document document = searcher.doc(docId);

            System.out.print((i + 1) + ". " + document.get("docno") + " - ");
            System.out.println(document.get("text"));
        }

        // Close searcher when indexes are no longer needed.
        searcher.close();

        if (reader != null)
        {
            reader.close();
        }
    }
}
