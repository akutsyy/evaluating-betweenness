package utility;

import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;

public class TMapUtility {

  public static double getOrDefault(TIntDoubleMap mergeList, int i, double def) {
    if (!mergeList.containsKey(i)) {
      return def;
    }
    return mergeList.get(i);
  }

  public static int getOrDefault(TIntIntMap mergeList, int i, int def) {
    if (!mergeList.containsKey(i)) {
      return def;
    }
    return mergeList.get(i);
  }

}
