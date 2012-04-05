/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Models;

import org.apache.lucene.analysis.payloads.PayloadHelper;
import org.apache.lucene.search.DefaultSimilarity;

/**
 *
 * @author Sky
 */
public class CustomSimilarity extends DefaultSimilarity
{

    @Override
    public float scorePayload(int docId, String fieldName, int start, int end, byte[] payload, int offset, int length)
    {
        if (payload != null)
        {
            float score = PayloadHelper.decodeFloat(payload, offset);
            return score;
        }
        else
        {
            return 1.0F;
        }
    }
}
