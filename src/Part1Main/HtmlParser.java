package Part1Main;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Sky
 *
 * A program to collect sports terms from webpages automatically.
 *
 */
public class HtmlParser
{

    private IndexWriter writer;

    public HtmlParser()
    {
    }

    public void runHtmlParser()
    {
        try
        {
            // ======================================================================================================
            // The sports terms collected from enchantedlearning will be used as a base/dictionary.
            // These terms will then be verified against another sports website and the remaining 
            // terms will be used to compute static quality scores of each document.
            // ======================================================================================================
            URL my_url = new URL("http://www.enchantedlearning.com/wordlist/sports.shtml");
            URL my_url2 = new URL("http://en.wikipedia.org/wiki/Sports");
            BufferedReader br = new BufferedReader(new InputStreamReader(my_url.openStream()));
            BufferedReader br2 = new BufferedReader(new InputStreamReader(my_url2.openStream()));
            String strTemp = "", strTemp2 = "", html = "", html2 = "";
            while (null != (strTemp = br.readLine()))
            {
                html += strTemp + "\n";
            }

            while (null != (strTemp2 = br2.readLine()))
            {
                html2 += strTemp2 + "\n";
            }

            Document doc = Jsoup.parse(html);

            int startIndex = doc.body().text().lastIndexOf("aerobics");
            int endIndex = doc.body().text().lastIndexOf("wrestling") + 9;
            String pretext = doc.body().text().substring(startIndex, endIndex);
            String[] pretokens = pretext.split("\\s([a-zA-Z]\\s){0,1}(Cont.){0,1}");
            String text = "";
            Boolean firsttoken = true;

            // =========================================================
            // Convert it to list as we need the list object to create a
            // set object. A set is a collection object that cannot have
            // a duplicate values, so by converting the array to a set
            // the duplicate value will be removed.
            // =========================================================
            List<String> list = Arrays.asList(pretokens);
            Set<String> set = new HashSet<>(list);

            // =========================================================
            // Create an array to convert the Set back to array.
            // The Set.toArray() method copy the value in the set to the
            // defined array.
            // =========================================================
            String[] tokens = new String[set.size()];
            set.toArray(tokens);

            String[] tokens2 = Jsoup.parse(html2).text().split("\\s");
            for (int i = 0; i < tokens.length; i++)
            {
                for (int j = 0; j < tokens2.length; j++)
                {
                    if (tokens2[j].equalsIgnoreCase(tokens[i]))
                    {
                        if (firsttoken)
                        {
                            text += tokens[i];
                            firsttoken = false;
                            break;
                        }
                        else
                        {
                            text += "\n" + tokens[i];
                            break;
                        }
                    }
                }
            }

            Writer output;
            try
            {
                File file = new File("htmlterms.txt");
                output = new BufferedWriter(new FileWriter(file));
                output.write(text);
                output.close();
                System.out.println("Your file has been written");
            }
            catch (Exception ex)
            {
                System.out.println("Cannot write file..." + ex.getMessage());
                System.exit(-1);
            }

            br.close();
            br2.close();
        }
        catch (Exception ex)
        {
            System.out.println("Exception Caught: " + ex);
        }
    }

    HtmlParser(String indexDir) throws IOException
    {
        // =================================================================
        // The boolean true parameter means to create a new index everytime, 
        // potentially overwriting any existing files there.
        // =================================================================
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
    public void indexString(String[] html) throws IOException
    {
        try
        {
            String[] text = html;
            org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
            Field field = new Field("term", text[0], Field.Store.YES, Field.Index.ANALYZED);
            doc.add(field);
            for (int i = 1; i < text.length; i++)
            {
                doc.add(new Field("term", text[i], Field.Store.YES, Field.Index.ANALYZED));
            }

            writer.addDocument(doc);
            System.out.println("Added: " + text);
        }
        catch (Exception e)
        {
            System.out.println("Could not add: " + html);
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
