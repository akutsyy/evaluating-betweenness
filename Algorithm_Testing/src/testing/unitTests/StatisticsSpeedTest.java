package testing.unitTests;

import utility.ArrayGenerator;
import utility.Statistics;

public class StatisticsSpeedTest {

  public static void main(String[] args) {

    double[] values = ArrayGenerator.randDoubles(400000);
    long start = System.nanoTime();
    Statistics s = new Statistics(values);
    long end = System.nanoTime();
    System.out.println((end - start) / Math.pow(10, 9));
  }
}
