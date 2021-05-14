package testing.unitTests;

import algorithms.brandespp.metis.GainBucketHeap;
import java.util.Arrays;

public class GainBucketTester {

  public static void main(String[] args) {
    double[] dis = {-1.0, -1.0, -10, -9.9};
    GainBucketHeap gain = new GainBucketHeap(3, dis);
    System.out.println(Arrays.toString(dis));
    System.out.println(gain);
    for (int i = 0; i < 4; i++) {
      System.out.println("Adding " + i);
      gain.insert(i);
      System.out.println(gain);
    }
    while (!gain.isEmpty()) {
      System.out.println("Popped " + gain.pop());
      System.out.println(gain);
    }
  }
}
