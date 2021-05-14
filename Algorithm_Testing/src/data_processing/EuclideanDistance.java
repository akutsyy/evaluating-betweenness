package data_processing;

import java.util.Arrays;

public class EuclideanDistance {

  public static double getNormalizedEuclideanDistance(double[] truth, double[] estimate) {
    return euclideanDistance(normalized(truth), normalized(estimate));
  }

  public static double[] normalized(double[] b) {
    double[] normalized = new double[b.length];
    double norm = Math.sqrt(Arrays.stream(b).map(i -> i * i).sum());
    if (norm == 0) {
      return Arrays.copyOf(b, b.length);
    }

    for (int i = 0; i < b.length; i++) {
      normalized[i] = b[i] / norm;
    }
    return normalized;
  }


  private static double euclideanDistance(double[] b1, double[] b2) {
    double sum = 0;
    for (int i = 0; i < b1.length; i++) {
      sum += Math.pow(b1[i] - b2[i], 2);
    }
    return Math.sqrt(sum);
  }
}
