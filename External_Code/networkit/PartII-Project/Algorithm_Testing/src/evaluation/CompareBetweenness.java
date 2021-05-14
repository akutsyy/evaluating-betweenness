package evaluation;

import java.util.HashMap;
import java.util.Map;

public class CompareBetweenness {

  public static double normalizedEuclideanDistance(Map<Integer, Double> b1,
      Map<Integer, Double> b2) {
    Map<Integer, Double> normalizedb1 = normalize(b1);
    Map<Integer, Double> normalizedb2 = normalize(b2);
    return euclideanDistance(normalizedb1, normalizedb2);

  }

  public static Map<Integer, Double> normalize(Map<Integer, Double> b) {
    Map<Integer, Double> b2 = new HashMap<>();
    for (Integer key : b.keySet()) {
      b2.put(key, b.get(key) / ((b.size()-1)*(b.size()-2)));
    }
    return b2;
  }


  public static double euclideanDistance(Map<Integer, Double> b1, Map<Integer, Double> b2) {
    double average = 0;
    for (Integer i : b1.keySet()) {
      average += Math.pow(b1.get(i) - b2.get(i), 2);
    }
    return Math.sqrt(average);
  }
}
