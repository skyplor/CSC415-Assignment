/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csc415;

import CentralProcessingClasses.SearchEngineProcessor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Comparator;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;

/**
 *
 * @author Sky
 */
public class SearchEngine
{

    public static void main(String[] args) throws Exception
    {

        String index = "Index-tdt3";
        String index2 = "Index-StaticScore";
        String querytext = "";
        String querysource = "";
        String querydate = "";


        System.out.println("========================Start of Part 1.1===============================");
        for (int queryNo = 0; queryNo < 3; queryNo++)
        {

            if (queryNo == 0)
            {
                querytext = "\"New York\"";
                querysource = "CNN";
                querydate = "";
                System.out.println("Documents that contain \"New York\" and published by \'CNN\'");
            }
            if (queryNo == 1)
            {
                querytext = "helicopters AND NOT planes";
                querysource = "";
                querydate = "";
                System.out.println("Documents that contain \'helicopters\' but not \'planes\'");
            }
            if (queryNo == 2)
            {
                querytext = "\"Dan Ronan\"";
                querysource = "";
                querydate = "[19981101 TO 19981130]";
                System.out.println("Documents which contain the name \"Dan Ronan\" and was published in November 1998");
            }

            SearchEngineProcessor search = new SearchEngineProcessor(index, index2, 20, false);
            search.searchIndex(querytext, querysource, querydate);

        }
        System.out.println("=========================End of Part 1.1================================");
        System.out.println("========================Start of Part 1.2===============================");

//        querytext = "\"New York\"";
//        querysource = "CNN";
//        querydate = "";
//        querytext = "helicopters AND NOT planes";
//        querysource = "";
//        querydate = "";
        querytext = "toilet AND paper";
        querysource = "";
        querydate = "";

        System.out.println("Documents that contain \'toilet\' and \'paper\'");

        SearchEngineProcessor search2 = new SearchEngineProcessor(index, index2, 20, true);
        search2.searchIndex(querytext, querysource, querydate);

        System.out.println("========================End of Part 1.2===============================");

    }
}
