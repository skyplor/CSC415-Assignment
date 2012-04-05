/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralProcessingClasses;

import Models.DocumentObj;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Sky
 */
public class ComputeStaticScore
{

    /**
     * Creates an array of terms and their positive and negative probabilities
     * and the ratio of documents in a certain category. Expects a Lucene
     * index created with the tokenized document bodies, and a category
     * field that is specified in the setters and populated with the specified
     * category value.
     *
     * @throws Exception if one is thrown.
     */
    public List<DocumentObj> train(String textindex, String htmlfile) throws Exception
    {
//        this.trainingSet = new HashMap<String, double[]>();
        IndexReader htmlreader = null, textreader = null;
        Directory htmldirectory, textdirectory;
        List<DocumentObj> matchedDocIds = new ArrayList<>();
        String htmlterms = "";
        StringBuilder contents = new StringBuilder();
        try
        {
//            htmldirectory = FSDirectory.open(new File(htmlindex));
//            htmlreader = IndexReader.open(htmldirectory);

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
//            double matchedDocs = (double) matchedDocIds.size();
//            double nDocs = (double) textreader.numDocs();
//            Double categoryDocRatio = matchedDocs / (nDocs - matchedDocs);
//            TermEnum termEnum = reader.terms();
//            TermEnum htmlTermEnum = htmlreader.terms(new Term("term"));


            int nWords = 0;
            double nUniqueWords = 0.0D;
//            while (htmlTermEnum.next())
//            {
//                double nWordInCategory = 0.0D;
//                double nWordNotInCategory = 0.0D;
//                Term htmlterm = htmlTermEnum.term();
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
//                        nWordInCategory += frequency;
                                matchedDocIds.get(docObj).setFrequency(matchedDocIds.get(docObj).getFrequency() + frequency);
                            }
                            else
                            {
//                        nWordNotInCategory += frequency;
                            }
                        }
                        nWords += frequency;
//                    nUniqueWords++;
                    }
//                double[] pWord = new double[2];
//                if (trainingSet.containsKey(term.text()))
//                {
//                    pWord = trainingSet.get(term.text());
//                }
//                pWord[0] += (double) nWordInCategory;
//                pWord[1] += (double) nWordNotInCategory;
//                trainingSet.put(term.text(), pWord);
                }
            }

            for (int doc = 0; doc < matchedDocIds.size(); doc++)
            {
                int termfrequency = matchedDocIds.get(doc).getFrequency();
//                if (doc == 5240)
//                {
//                    System.out.println("termfrequency: " + termfrequency);
//                    System.out.println("double termfrequency: " + (double) termfrequency);
//                    System.out.println("nWords: " + nWords);
//
//                    double score1 = ((double) termfrequency) / ((double) htmltokens.length);
//                    System.out.println("score: " + score1);
//                    matchedDocIds.get(doc).setStaticQualityScore(score1);
//                    System.out.println("matchedDocIds' static score: " + matchedDocIds.get(doc).getStaticQualityScore());
//
//                }
                double score = ((double) termfrequency) / ((double) htmltokens.length);

                matchedDocIds.get(doc).setStaticQualityScore(score);
            }

//            // once we have gone through all our terms, we normalize our
//            // trainingSet so the values are probabilities, not numbers
//            for (String term : trainingSet.keySet())
//            {
//                double[] pWord = trainingSet.get(term);
//                for (int i = 0; i < pWord.length; i++)
//                {
//                    if (preventOverfitting)
//                    {
//                        // apply smoothening formula
//                        pWord[i] = ((pWord[i] + 1) / (nWords + nUniqueWords));
//                    }
//                    else
//                    {
//                        pWord[i] /= nWords;
//                    }
//                }
//            }
//            if (selectTopFeatures)
//            {
//                InfoGainFeatureSelector featureSelector =
//                        new InfoGainFeatureSelector();
//                featureSelector.setWordProbabilities(trainingSet);
//                featureSelector.setPCategory(matchedDocs / nDocs);
//                Map<String, double[]> topFeatures =
//                        featureSelector.selectFeatures();
//                this.trainingSet = topFeatures;
//            }
        }
        finally
        {
//            if (htmlreader != null)
//            {
//                htmlreader.close();
//            }
            if (textreader != null)
            {
                textreader.close();
            }
        }
        return matchedDocIds;
    }

    private List<DocumentObj> computeMatchedDocIds(IndexReader textreader) throws IOException
    {
//        int num = textreader.numDocs();
        List<DocumentObj> matchedDocIds = new ArrayList<>();

        IndexSearcher searcher = new IndexSearcher(textreader);

        TopScoreDocCollector collector = TopScoreDocCollector.create(40000, true);

        StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_35);

        QueryParser parsertext = new QueryParser(Version.LUCENE_35, "text", analyzer);
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
//        for (int i = 0; i < num; i++)
//        {
//            if (!textreader.isDeleted(i))
//            {
//                Document d = textreader.document(i);
////                System.out.println("d=" + d);
//                matchedDocIds.add(d.get("docno"));
//            }
//        }
//        Filter categoryFilter = new CachingWrapperFilter(
//                new QueryWrapperFilter(new TermQuery(
//                new Term(categoryFieldName, matchCategoryValue))));
//        DocIdSet docIdSet = categoryFilter.getDocIdSet(reader);
//        DocIdSetIterator docIdSetIterator = docIdSet.iterator();
//        Set<Integer> matchedDocIds = new HashSet<Integer>();
//        while (docIdSetIterator.next())
//        {
//            matchedDocIds.add(docIdSetIterator.doc());
//        }
        return matchedDocIds;
    }
}
