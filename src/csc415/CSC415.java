/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csc415;

/**
 *
 * @author Sky
 */
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.text.DecimalFormat;
import java.util.StringTokenizer;

public class CSC415
{

    private static int[][] results;
    private static String[] labels =
    {
        "Computers&Internet.Hardware", "Sports.OutdoorRecreation",
        "Computers&Internet.Programming&Design", "Computers&Internet.Software",
        "Computers&Internet.Internet", "Sports.Basketball", "Sports.AutoRacing",
        "Sports.Baseball", "Sports.Other-Sports", "Sports.Football(American)",
        "Sports.Football(Soccer)", "Sports.Cycling", "Computers&Internet.ComputerNetworking",
        "Sports.FantasySports", "Sports.Volleyball", "Computers&Internet.Security",
        "Computers&Internet.Other-Computers", "Sports.Hockey", "Sports.Golf", "Sports.Rugby",
        "Sports.Cricket", "Sports.Tennis", "Sports.WinterSports", "Sports.WaterSports",
        "Sports.Swimming&Diving", "Sports.Olympics", "Sports.Football(Canadian)",
        "Sports.HorseRacing", "Sports.Snooker&Pool", "Sports.Football(Australian)",
        "Sports.Boxing", "Sports.MotorSports", "Sports.Wrestling", "Sports.MartialArts",
        "Sports.Handball", "Sports.Running", "Sports.CricketWorldCup2007"
    };

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, ParseException
    {
        kNNclassification();
    }

    public static void kNNclassification() throws IOException, ParseException
    {
        // initialize results table
        results = new int[38][38];
        for (int i = 0; i < results.length; i++)
        {
            for (int j = 0; j < results.length; j++)
            {
                if (i == 0)
                {
                    results[i][j] = j;
                }
                else if (j == 0)
                {
                    results[i][j] = i;
                }
                else
                {
                    results[i][j] = 0;
                }
            }
        }

        // 0. Specify the analyzer.
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);

        // 1. create the index
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        IndexWriter w = new IndexWriter(index, config);

        // read in training file input
        File trainFile = new File("questiontrain.arff");

        BufferedReader br = new BufferedReader(new FileReader(trainFile));

        // skip first few lines until @data is reached
        skipLines(br);

        // read in actual training data
        String thisLine = "";
        while ((thisLine = br.readLine()) != null)
        {
            // get tokens from line, and add tokens into index
            String[] tokens = getTokens(thisLine);
            addDoc(w, tokens[0], tokens[1]);
            // System.out.println("DEBUG: document = " + tokens[0] + ", class = " + tokens[1]);
        }
        br.close();

        w.close();

        // 2. query
        File testFile = new File("questiontest2.arff");

        // initialize LUCENE API
        int kVal = 5;
        IndexReader reader = IndexReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        br = new BufferedReader(new FileReader(testFile));

        // skip first few lines until @data is reached
        skipLines(br);

        // read in test question
        thisLine = "";
        while ((thisLine = br.readLine()) != null)
        {
            // get tokens from line, get query
            String[] tokens = getTokens(thisLine);
            String querystr = tokens[0];
            // System.out.println("DEBUG: question = " + tokens[0] + ", class = " + tokens[1]);
            // the "question" argument specifies the default field to use
            Query q = new QueryParser(Version.LUCENE_35, "question", analyzer).parse(querystr);

            // 3. search using LUCENE API
            TopScoreDocCollector collector = TopScoreDocCollector.create(kVal, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            // find majority
            String major = findMajority(hits, searcher);
            // System.out.println("DEBUG: prediction: " + major);
            // System.out.println("DEBUG: index number: " + getIntLabel(major) + "\n\n");

            // increment class in results table
            int actual = getIntLabel(tokens[1]);
            int predict = getIntLabel(major);
            results[predict][actual]++;

            searcher.close();
        }
        br.close();

        printResults();
        // compute precision and recall for category
        for (int i = 1; i <= labels.length; i++)
        {
            evaluate(i);
        }
    }

    // Method to skip lines in train or test data file
    private static void skipLines(BufferedReader br) throws IOException
    {
        String thisLine = "";
        thisLine = br.readLine();
        while (!thisLine.equals("@data"))
        {
            thisLine = br.readLine();
        }
    }

    // Method to get question and corresponding class from input file line
    private static String[] getTokens(String thisLine)
    {
        StringTokenizer st = new StringTokenizer(thisLine, ",");
        String[] tokens = new String[2];
        String token = st.nextToken();
        tokens[0] = token.substring(1, token.length() - 1);
        tokens[1] = st.nextToken();
        return tokens;
    }

    // Method to add a new document into index
    private static void addDoc(IndexWriter w, String questionVal, String classVal)
            throws IOException
    {
        Document doc = new Document();
        doc.add(new Field("question", questionVal, Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("class", classVal, Field.Store.YES, Field.Index.NO));
        w.addDocument(doc);
    }

    // Method to determine which class label has the most occurrences
    public static String findMajority(ScoreDoc[] hits, IndexSearcher searcher)
            throws CorruptIndexException, IOException
    {
        // create predict array which will store class label counts
        String predict[][] = new String[hits.length][2];

        // fill in predict array
        for (int i = 0; i < hits.length; i++)
        {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            // System.out.println("DEBUG: " + (i + 1) + ". " + d.get("class"));

            for (int j = 0; j < predict.length; j++)
            {

                // fill row if class i does not exist
                if (predict[j][0] == null)
                {
                    predict[j][0] = d.get("class");
                    predict[j][1] = "1";
                    break;
                }
                // increment counter if class i exists in row j
                else if (predict[j][0].equals(d.get("class")))
                {
                    predict[j][1] = Integer.toString(Integer.parseInt(predict[j][1]) + 1);
                    break;
                }
            }
        }

        // bubble sort predict array on class counts
        boolean swap = true;
        while (swap)
        {
            swap = false;
            for (int i = 0; i < predict.length - 1; i++)
            {
                if (predict[i + 1][0] != null)
                {
                    if (Integer.parseInt(predict[i][1]) < Integer.parseInt(predict[i + 1][1]))
                    {
                        String temp[] = new String[2];
                        temp = predict[i];
                        predict[i] = predict[i + 1];
                        predict[i + 1] = temp;

                        swap = true;
                    }
                }
            }
        }

        // return class label with highest occurrence
        return predict[0][0];
    }

    // Method will return a corresponding integer representing a class label
    private static int getIntLabel(String label)
    {
        int i = 0;
        while (!label.equals(labels[i]))
        {
            i++;
        }
        return i + 1;
    }

    // Method will return a corresponding String representing a class label
    private static String getStringLabel(int label)
    {
        return labels[label - 1];
    }

    // Method to print results table
    private static void printResults()
    {
        drawLine(21);
        System.out.println("Overall Results Table");
        drawLine(21);
        System.out.println();

        for (int i = 0; i < results.length; i++)
        {
            for (int j = 0; j < results.length; j++)
            {
                System.out.print(results[i][j] + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }

    // Method to print contingency table for class i
    private static void evaluate(int i)
    {
        String title = "Contingency Table for '" + getStringLabel(i) + "'";
        drawLine(title.length());
        System.out.println(title);
        drawLine(title.length());
        System.out.println("");

        String[][] metric =
        {
            {
                "", "YES", "NO"
            },
            {
                "YES", "", ""
            },
            {
                "NO", "", ""
            }
        };

        // true positive
        metric[1][1] = Integer.toString(results[i][i]);

        // false positive
        int temp = 0;
        for (int j = 1; j < results.length; j++)
        {
            if (j != i)
            {
                temp += results[i][j];
            }
        }
        metric[1][2] = Integer.toString(temp);

        // false negative
        temp = 0;
        for (int j = 1; j < results.length; j++)
        {
            if (j != i)
            {
                temp += results[j][i];
            }
        }
        metric[2][1] = Integer.toString(temp);

        // true negative
        temp = 0;
        for (int j = 1; j < results.length; j++)
        {
            for (int k = 1; k < results.length; k++)
            {
                if (j != i && k != i)
                {
                    temp += results[j][k];
                }
            }
        }
        metric[2][2] = Integer.toString(temp);

        // print class contingency table
        for (int j = 0; j < metric.length; j++)
        {
            for (int k = 0; k < metric.length; k++)
            {
                System.out.print(metric[j][k] + "\t");
            }
            System.out.println();
        }
        System.out.println();

        // compute precision and recall
        String p = "";
        String r = "";
        if (Double.parseDouble(metric[1][1]) == 0)
        {
            p = "0";
            r = "0";
        }
        else
        {
            p = Double.toString(Double.parseDouble(metric[1][1])
                    / (Double.parseDouble(metric[1][1]) + Double.parseDouble(metric[1][2])));
            r = Double.toString(Double.parseDouble(metric[1][1])
                    / (Double.parseDouble(metric[1][1]) + Double.parseDouble(metric[2][1])));
        }

        DecimalFormat df = new DecimalFormat("0.00");
        System.out.println("Precision P = " + df.format(Double.parseDouble(p)));
        System.out.println("Recall R = " + df.format(Double.parseDouble(r)) + "\n");
    }

    // Method prints a separator line with the specified length
    private static void drawLine(int length)
    {
        for (int i = 0; i < length; i++)
        {
            System.out.print("-");
        }
        System.out.println();
    }
}
