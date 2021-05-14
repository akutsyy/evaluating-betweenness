package data_processing;

import java.util.Arrays;
import org.jgrapht.alg.util.Pair;

public class AverageError {

  public static double[] normalizeBetweenness(double[] b) {
    return Arrays.stream(b).map(i -> i / ((b.length - 1) * (b.length - 2))).toArray();
  }

  public static Pair<Double, Double> averageAndMaxErrorNormalized(double[] truth, double[] estimate) {
    double[] nTruth = normalizeBetweenness(truth);
    double[] nEstimate = normalizeBetweenness(estimate);
    double max = 0;
    double average = 0;
    for (int i = 0; i < nTruth.length; i++) {
      average += (Math.abs(nTruth[i] - nEstimate[i]));
      max = Math.max(max, (Math.abs(nTruth[i] - nEstimate[i])));
    }
    return new Pair<>(average / nTruth.length, max);
  }
}
