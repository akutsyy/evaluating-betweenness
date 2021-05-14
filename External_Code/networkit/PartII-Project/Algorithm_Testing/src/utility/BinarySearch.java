package utility;

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
}
