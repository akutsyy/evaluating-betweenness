package utility;

import gnu.trove.list.array.TIntArrayList;
import java.util.function.DoubleFunction;

public class BinarySearch {

  public static double binarySearch(DoubleFunction<Double> f, double min, double max, double target, double min_range) {

    double original_min = min;
    double original_max = max;
    double out;
    double mid;
    int i = 0;
    do {
      i++;

      mid = (max + min) / 2;
      out = f.apply(mid);
      if (i > 1000) {
        String parameters = "min: " + original_min + "\n max: " + original_max + "\n target: " + target + "\n min range: " + min_range + "\n";
        String current = "current search: " + mid + "\n current value: " + out;
        throw new RuntimeException("Excessively long binary search with \n" + parameters + current);
      }

      if (target > out) {
        min = mid;
      } else {
        max = mid;
      }
    } while (Math.abs(max - min) > min_range);

    return mid;
  }

  public static int binarySearchJustLessThan(TIntArrayList array, double[] weights, double toFind) {
    return binarySearchJustLessThan(array, weights, toFind, array.size() - 1);
  }

  public static int binarySearchJustLessThan(TIntArrayList array, double[] weights, double toFind, int max) {
    // Looking for least value which is greater than or equal to toFind
    int l = 0;
    int r = max;
    while (l < r) {
      int mid = (l + r + 1) / 2;

      if (weights[array.get(mid)] <= toFind) {
        l = mid;
      } else {
        r = mid - 1;
      }
    }
    return array.get(l);
  }

  public static int binarySearchJustMoreThan(TIntArrayList array, double[] weights, double toFind) {
    return binarySearchJustLessThan(array, weights, toFind, array.size() - 1);
  }

  public static int binarySearchJustMoreThan(TIntArrayList array, double[] weights, double toFind, int max) {
    // Looking for greatest value which is less than or equal to toFind
    int l = 0;
    int r = max + 1;
    while (l < r) {
      int mid = (l + r) / 2;
      if (toFind <= weights[array.get(mid)]) {
        r = mid;
      } else {
        l = mid + 1;
      }
    }
    return array.get(l);
  }
}
