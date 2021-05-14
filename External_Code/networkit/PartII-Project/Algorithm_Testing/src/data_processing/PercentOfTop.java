package data_processing;

import static data_processing.NumberOfInversions.getOrder;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import testing.InversionsTest;

public class PercentOfTop {
  public static TIntSet getTop(double portion, int[] ordered){
    TIntSet set = new TIntHashSet();
    for(int i=0;i<ordered.length*portion;i++){
      set.add(ordered[i]);
    }
    return set;
  }
  public static double percentOfTop(TIntSet topPortion, double[] estimate){
    int[] estimateOrder = getOrder(estimate);
    int hits = 0;
    for(int i=0;i<topPortion.size();i++){
      if(topPortion.contains(estimateOrder[i])){
        hits++;
      }
    }
    return hits/(double) topPortion.size();
  }
}
