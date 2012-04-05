package Main;

import Part1Main.ComputeStaticMain;
import Part1Main.HtmlParser;
import Part1Main.Part1LuceneSearchEngine;
import Part1Main.TextFileIndexer;
import Part2Main.Part2Weka;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryParser.ParseException;

/**
 *
 * @author Sky
 */
public class CSC415
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        Scanner input = new Scanner(System.in);
        boolean keepRunning = true;
        while (keepRunning)
        {
            listMenu();

            String selection = input.nextLine();

            switch (selection)
            {
                case "1":
                    try
                    {
                        File tdt3Index = new File("Index-tdt3");
                        File staticScoreIndex = new File("Index-StaticScore");
                        File tdt3Data = new File ("tdt3");
                        if (!tdt3Index.exists())
                        {
                            System.out.println("Indexing tdt3 documents...");
                            TextFileIndexer tdt3Indexer = new TextFileIndexer();
                            tdt3Indexer.runtdt3Indexer();
                            System.out.println("Indexing completed!");
                            System.out.println();
                        }
                        if (!staticScoreIndex.exists())
                        {
                            System.out.println("Computing static quality scores for tdt3 documents...");
                            ComputeStaticMain compute = new ComputeStaticMain();
                            compute.runComputeStaticMain();
                            System.out.println("Static quality score index created!");
                            System.out.println();
                        }
                        if (!tdt3Data.exists())
                        {
                            System.out.println("Error: tdt3 data folder not found.");
                            System.out.println("Please place the tdt3 folder in this directory or rename the data folder to tdt3 if it is already in.");
                            System.out.println("System exiting...");
                            System.exit(0);
                        }
                        Part1LuceneSearchEngine lucene = new Part1LuceneSearchEngine();
                        lucene.runSearchEngine();
                    }
                    catch (Exception ex)
                    {
                        Logger.getLogger(CSC415.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;

                case "2":
                    Part2Weka weka = new Part2Weka();
                    try
                    {                        
                        File weka1 = new File ("questiontest.arff");
                        File weka2 = new File ("questiontest2.arff");
                        File weka3 = new File ("questiontrain.arff");
                        
                        if(!weka1.exists() || !weka2.exists() || !weka3.exists())
                        {
                            System.out.println("Error: Required weka files (questiontest.arff, questiontest2.arff and questiontrain.arff) not found.");
                            System.out.println("Please place the relevant weka files in this directory.");
                            System.out.println("System exiting...");
                            System.exit(0);
                        }
                        System.out.println("Classifying...");
                        weka.runWeka();
                    }
                    catch (ParseException | IOException ex)
                    {
                        Logger.getLogger(CSC415.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;

                case "3":
                    System.out.println("Program exiting...");
                    keepRunning = false;
                    break;

                default:
                    System.out.println("Incorrect input. Kindly enter again.");
                    break;
            }
        }

    }

    private static void listMenu()
    {
        System.out.println("\nWelcome! Enter your choice:");
        System.out.println("*********************CSC415***********************");
        System.out.println("* 1. Part 1: Lucene Search Engine Implementation *");
        System.out.println("* 2. Part 2: Weka Implementation                 *");
        System.out.println("* 3. Exit Program                                *");
        System.out.println("**************************************************");
        System.out.print("Choice: ");
    }
}
