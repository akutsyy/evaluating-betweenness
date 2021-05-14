package testing;

import gnu.trove.map.hash.TIntDoubleHashMap;
import utility.ArrayGenerator;

public class SetSpeedTest {

  public static void main(String[] args) {
    //ArrayGraph g = new ArrayGraph(Harness.getFile(options), Harness.getFileType(options));
    int size = 10000000;
    TIntDoubleHashMap map = new TIntDoubleHashMap(size);
    double[] array = new double[size];
    int[] order = ArrayGenerator.shuffledInts(0, size);
    long start = System.nanoTime();
    for (int i = 0; i < size; i++) {
      map.put(order[i], order[i]);
    }
    for (int i = 0; i < size; i++) {
      map.put(i, map.get(i) / 2);
    }
    long end = System.nanoTime();

    System.out.println("Time taken for map: " + (end - start) / Math.pow(10, 9));

    start = System.nanoTime();

    for (int i = 0; i < size; i++) {
      array[order[i]] = order[i];
    }
    for (int i = 0; i < size; i++) {
      array[i] = array[i] / 2;
    }

    end = System.nanoTime();

    System.out.println("Time taken for array: " + (end - start) / Math.pow(10, 9));
  }
}
