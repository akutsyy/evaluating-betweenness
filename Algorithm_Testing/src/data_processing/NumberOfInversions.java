package data_processing;

import java.util.Arrays;
import utility.ArrayGenerator;

public class NumberOfInversions {

  public static int[] getOrder(double[] truth) {
    return Arrays.stream(ArrayGenerator.range(truth.length)).boxed().sorted((o1, o2) -> (int) Math.signum(truth[o2] - truth[o1])).mapToInt(value -> value).toArray();
  }

  private static int[] invertPermutation(int[] p) {
    int[] inverse = new int[p.length];
    for (int i = 0; i < p.length; i++) {
      inverse[p[i]] = i;
    }
    return inverse;
  }

  public static int[] getTruthMap(double[] truth) {
    return invertPermutation(getOrder(truth));
  }

  public static double getPercentOfPossibleInversions(int[] truthMap, double[] estimate) {
    int[] estimateOrder = Arrays.stream(getOrder(estimate)).map(i -> truthMap[i]).toArray();
    double maxInversions = estimate.length * ((double) (estimate.length - 1) / 2);
    return 100.0 * invCount(estimateOrder) / maxInversions;
  }

  // From https://stackoverflow.com/questions/337664/counting-inversions-in-an-array
  private static long invCount(int[] array) {
    if (array.length < 2) {
      return 0;
    }

    int m = array.length / 2;
    int[] left = Arrays.copyOfRange(array, 0, m);
    int[] right = Arrays.copyOfRange(array, m, array.length);

    return invCount(left) + invCount(right) + merge(array, left, right);
  }

  private static long merge(int[] arr, int[] left, int[] right) {
    int i = 0, j = 0;
    long count = 0;
    while (i < left.length || j < right.length) {
      if (i == left.length) {
        arr[i + j] = right[j];
        j++;
      } else if (j == right.length) {
        arr[i + j] = left[i];
        i++;
      } else if (left[i] <= right[j]) {
        arr[i + j] = left[i];
        i++;
      } else {
        arr[i + j] = right[j];
        count += left.length - i;
        j++;
      }
    }
    return count;
  }
}
