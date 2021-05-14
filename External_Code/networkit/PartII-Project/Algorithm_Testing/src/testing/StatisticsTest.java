package testing;

import utility.ArrayGenerator;
import utility.RandomHelper;
import utility.Statistics;

public class StatisticsTest {

  public static void main(String[] args) {

    double[] values = ArrayGenerator.randDoubles(400000);
    long start = System.nanoTime();
    Statistics s = new Statistics(values);
    long end = System.nanoTime();
    System.out.println((end-start)/Math.pow(10,9));
  }
}
