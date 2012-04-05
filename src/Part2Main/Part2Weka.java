package Part2Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
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

public class Part2Weka
{
    private static int[][] results = new int[38][38];
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

    public void runWeka() throws IOException, ParseException
    {
        // 0. Specify the analyzer.
        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);

        // 1. create the index
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_35, analyzer);
        IndexWriter w = new IndexWriter(index, config);

        // read in training file input
        File trainFile = new File("questiontrain.arff");

        BufferedReader br = new BufferedReader(new FileReader(trainFile));

        String strLine;

        strLine = br.readLine();
        while (!strLine.equals("@data"))
        {
            strLine = br.readLine();
        }

        // read in actual training data
        while ((strLine = br.readLine()) != null)
        {
            StringTokenizer st = new StringTokenizer(strLine, ",");
            String token = st.nextToken();
            String ques = token.substring(1, token.length() - 1);
            String cat = st.nextToken();

            addDoc(w, ques, cat);

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

        // read in test question
        String testLine;
        testLine = br.readLine();
        while (!testLine.equals("@data"))
        {
            testLine = br.readLine();
        }
        int j = 0;
        while ((testLine = br.readLine()) != null && j < 2)
        {
            StringTokenizer st = new StringTokenizer(testLine, ",");
            String token = st.nextToken();
            String ques = token.substring(1, token.length() - 1);
            String cat = st.nextToken();
            String querystr = ques;

            // the "question" argument specifies the default field to use
            Query q = new QueryParser(Version.LUCENE_35, "question", analyzer).parse(querystr);

            // 3. search using LUCENE API
            TopScoreDocCollector collector = TopScoreDocCollector.create(kVal, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;

            // find majority
            String majority = ClassifyTestQ(hits, searcher);

            // increment class in results table
            int actual = getIntLabel(cat);
            int predict = getIntLabel(majority);
            results[predict][actual]++;

            searcher.close();
        }
        br.close();

        printResults();
        System.out.println("Class: Precision,Recall");
        System.out.println("---------------------------");
        // compute precision and recall for category
        for (int i = 1; i <= labels.length; i++)
        {
            evaluate(i);
        }
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
    public static String ClassifyTestQ(ScoreDoc[] hits, IndexSearcher searcher)
            throws CorruptIndexException, IOException
    {
        // create predict array which will store class label counts
        String majority[][] = new String[hits.length][2];

        // fill in majority array
        for (int i = 0; i < hits.length; i++)
        {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            for (int j = 0; j < majority.length; j++)
            {

                // fill row if class i does not exist
                if (majority[j][0] == null)
                {
                    majority[j][0] = d.get("class");
                    majority[j][1] = "1";
                    break;
                }
                // increment counter if class i exists in row j
                else if (majority[j][0].equals(d.get("class")))
                {
                    majority[j][1] = Integer.toString(Integer.parseInt(majority[j][1]) + 1);
                    break;
                }
            }
        }

        // bubble sort majority array on class counts
        boolean swap = true;
        while (swap)
        {
            swap = false;
            for (int i = 0; i < majority.length - 1; i++)
            {
                if (majority[i + 1][0] != null)
                {
                    if (Integer.parseInt(majority[i][1]) < Integer.parseInt(majority[i + 1][1]))
                    {
                        String temp[];
                        temp = majority[i];
                        majority[i] = majority[i + 1];
                        majority[i + 1] = temp;

                        swap = true;
                    }
                }
            }
        }

        // return class label with highest occurrence
        return majority[0][0];
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
        System.out.println("--------------------");
        System.out.println("Overall Results");
        System.out.println("--------------------");
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

    // Method to print precision and recall for each class
    private static void evaluate(int i)
    {

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

        String[] precision = new String[38];
        String[] recall = new String[38];

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

        // compute precision and recall
        String p;
        String r;
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

        precision[i] = df.format(Double.parseDouble(p));
        recall[i] = df.format(Double.parseDouble(r));

        System.out.print(getStringLabel(i) + ":");
        System.out.print(precision[i] + ",");
        System.out.println(recall[i]);
    }
}