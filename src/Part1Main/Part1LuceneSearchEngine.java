package Part1Main;

import Part1CentralProcessingClasses.Part1LuceneSearchEngineProcessor;
import java.io.IOException;
import org.apache.lucene.queryParser.ParseException;

/**
 *
 * @author Sky
 */
public class Part1LuceneSearchEngine
{

    String index;
    String index2;
    String querytext;
    String querysource;
    String querydate;

    public Part1LuceneSearchEngine() throws Exception
    {

        index = "Index-tdt3";
        index2 = "Index-StaticScore";
        querytext = "";
        querysource = "";
        querydate = "";

    }

    public void runSearchEngine() throws IOException, ParseException
    {
        System.out.println("========================Start of Part 1.1===============================");
        for (int queryNo = 0; queryNo < 3; queryNo++)
        {
            if (queryNo == 0)
            {
                querytext = "\"New York\"";
                querysource = "CNN";
                querydate = "";
                System.out.println("\nDocuments that contain \"New York\" and published by \'CNN\'");
            }
            if (queryNo == 1)
            {
                querytext = "helicopters AND NOT planes";
                querysource = "";
                querydate = "";
                System.out.println("\nDocuments that contain \'helicopters\' but not \'planes\'");
            }
            if (queryNo == 2)
            {
                querytext = "\"Dan Ronan\"";
                querysource = "";
                querydate = "199811*";
                System.out.println("\nDocuments which contain the name \"Dan Ronan\" and was published in November 1998");
            }

            Part1LuceneSearchEngineProcessor search = new Part1LuceneSearchEngineProcessor(index, index2, 20, false);
            search.searchIndex(querytext, querysource, querydate);
        }
        System.out.println("=========================End of Part 1.1================================");
        System.out.println("========================Start of Part 1.2===============================");

        querytext = "spring AND monitor";
        querysource = "";
        querydate = "";

        System.out.println("\nDocuments that contain the terms: " + querytext + "(without Boost)");

        Part1LuceneSearchEngineProcessor search2 = new Part1LuceneSearchEngineProcessor(index, index2, 20, false);
        search2.searchIndex(querytext, querysource, querydate);

        System.out.println("\nDocuments that contain the terms: " + querytext + "(with Boost)");
        Part1LuceneSearchEngineProcessor search3 = new Part1LuceneSearchEngineProcessor(index, index2, 20, true);
        search3.searchIndex(querytext, querysource, querydate);

        System.out.println("========================End of Part 1.2===============================");
    }
}
