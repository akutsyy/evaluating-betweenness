package testing.unitTests;

import data_processing.NumberOfInversions;
import java.io.IOException;

public class InversionsTest {

  public static void main(String[] args) throws IOException {
    double[] truth = {1, 2, 3, 4, 5, 6, 7, 8, 9};
    double[] estimate = {9, 3, 4, 2, 5, 6, 7, 8, 1};
    int[] truthMap = NumberOfInversions.getTruthMap(truth);
    System.out.println(NumberOfInversions.getPercentOfPossibleInversions(truthMap, estimate) * (estimate.length * (estimate.length - 1) / 2.0) / 100);
  }
}
