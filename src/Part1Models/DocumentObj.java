package Part1Models;

import org.apache.lucene.document.Document;

/**
 *
 * @author Sky
 */
public class DocumentObj
{
    private int docID;
    private Document doc;
    private int frequency = 0;
    private double staticQualityScore = 0.0;

    /**
     * @return the doc
     */
    public Document getDoc()
    {
        return doc;
    }

    /**
     * @param doc the doc to set
     */
    public void setDoc(Document doc)
    {
        this.doc = doc;
    }

    /**
     * @return the docID
     */
    public int getDocID()
    {
        return docID;
    }

    /**
     * @param docID the docID to set
     */
    public void setDocID(int docID)
    {
        this.docID = docID;
    }

    /**
     * @return the frequency
     */
    public int getFrequency()
    {
        return frequency;
    }

    /**
     * @param frequency the frequency to set
     */
    public void setFrequency(int frequency)
    {
        this.frequency = frequency;
    }

    /**
     * @return the staticQualityScore
     */
    public double getStaticQualityScore()
    {
        return staticQualityScore;
    }

    /**
     * @param staticQualityScore the staticQualityScore to set
     */
    public void setStaticQualityScore(double staticQualityScore)
    {
        this.staticQualityScore = staticQualityScore;
    }
}
