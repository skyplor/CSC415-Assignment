/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralProcessingClasses;

import Models.DocumentObj;
import java.util.Comparator;

/**
 *
 * @author Sky
 */
public class CustomComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        DocumentObj d1 = (DocumentObj) o1;
        DocumentObj d2 = (DocumentObj) o2;        
        if (d1.getStaticQualityScore() < d2.getStaticQualityScore()) return 1;
        if (d1.getStaticQualityScore() > d2.getStaticQualityScore()) return -1;
        return 0;
    }

//    @Override
//    public int compareTo(Object o)
//    {
//        DocumentObj doc = (DocumentObj) o;
//        int rankCompare = rank.
//    }
}
