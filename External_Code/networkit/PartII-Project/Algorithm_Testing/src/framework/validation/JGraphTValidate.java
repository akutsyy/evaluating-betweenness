package framework.validation;

import static utility.MathUtils.fuzzyEquals;
import static utility.MathUtils.percentEquals;

import com.sun.jdi.InvalidTypeException;
import framework.graphs.arraygraph.ArrayGraph;
import java.util.HashSet;
import java.util.Map;

public class JGraphTValidate {


  public static Map<Integer, Double> getJGraphTBetweeness(ArrayGraph g) throws InvalidTypeException {
    if (g.isWeighted()) {
      JGraphTValidateWeighted jg = new JGraphTValidateWeighted(g);
      return jg.getBetweeness();
    } else {
      JGraphTValidateUnweighted jg = new JGraphTValidateUnweighted(g);
      return jg.getBetweeness();
    }
  }

  public static void findConnected(ArrayGraph g) throws InvalidTypeException {
    if (g.isWeighted()) {
      JGraphTValidateWeighted jg = new JGraphTValidateWeighted(g);
      jg.findConnected();
    } else {
      JGraphTValidateUnweighted jg = new JGraphTValidateUnweighted(g);
      jg.findConnected();
    }
  }

  public static int compareBetweeness(Map<Integer, Double> b1, Map<Integer, Double> b2,
      double epsilon) {
    return compareBetweeness(b1, "b1", b2, "b2", epsilon);
  }

  public static int nonZeroOverlap(Map<Integer, Double> b1, String name1, Map<Integer, Double> b2, String name2) {
    int overlap = 0;
    int b1nonzero = 0;
    int b2nonzero = 0;
    HashSet<Integer> merged = new HashSet<>(b1.keySet());
    merged.addAll(b2.keySet());

    for (Integer i : merged) {
      // If one is nonzero and the other isn't
      if (((b1.containsKey(i) && b1.get(i) != 0.0)) == (b2.containsKey(i) && b2.get(i) != 0.0)) {
        overlap++;
      }
      if (b1.containsKey(i) && b1.get(i) != 0.0) {
        b1nonzero++;
      }
      if ((b2.containsKey(i) && b2.get(i) != 0.0)) {
        b2nonzero++;
      }
    }
    System.out.println("Overlap of " + overlap + " out of " + b1nonzero + " nonzero nodes in " + name1 + " and " + b2nonzero + " in " + name2);
    return overlap;
  }

  public static int compareBetweeness(Map<Integer, Double> b1, String name1, Map<Integer, Double> b2, String name2,
      double epsilon) {
    int errors = 0;

    for (Integer i : b1.keySet()) {
      if (!b2.containsKey(i)) {
        System.out.println("Problem: "+name2+" is missing node " + i);
        errors++;
      } else if (!fuzzyEquals(b1.get(i), b2.get(i), epsilon)) {
        double difference = 100 * (b1.get(i) - b2.get(i)) / (Math.max(b1.get(i), b2.get(i)));
        System.out.println(
            "Problem: " + i + " is " + b1.get(i) + " in " + name1 + ", but is " + b2.get(i) + " in " + name2 + ", " + difference + "% difference");
        errors++;
      }
    }
    for (Integer i : b2.keySet()) {
      if (!b1.containsKey(i)) {
        System.out.println("Problem: "+name1+" is missing node " + i);
        errors++;
      }
    }
    if (errors == 0) {
      System.out.println("b1 and b2 are identical!");
    }
    return errors;
  }

  public static int compareRelativeBetweeness(Map<Integer, Double> b1, Map<Integer, Double> b2,
      double percentage) {
    int errors = 0;

    for (Integer i : b1.keySet()) {
      if (!b2.containsKey(i)) {
        System.out.println("Problem: b2 missing node " + i);
        errors++;
      } else if (!percentEquals(b1.get(i), b2.get(i), percentage)) {
        System.out.println(
            "Problem: " + i + " is " + b1.get(i) + " in b1, but is " + b2.get(i) + " in b2");
        errors++;
      }
    }
    for (Integer i : b2.keySet()) {
      if (!b1.containsKey(i)) {
        System.out.println("Problem: b1 missing node " + i);
        errors++;
      }
    }
    if (errors == 0) {
      System.out.println("b1 and b2 are identical!");
    }
    return errors;
  }
}
