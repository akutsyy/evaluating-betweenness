package testing;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import utility.MathUtils;
import utility.Printing;

public class PairRepresentationTester {

  public static void main(String[] args) {
    intCombiningTest();
  }

  public static void intCombiningTest() {
    long start = System.nanoTime();

    TLongIntMap m = new TLongIntHashMap(1000 * 1000);
    for (int i = 0; i < 1000; i++) {
      for (int j = 0; j < 1000; j++) {
        Printing.printProgress(i * 1000 + j, 1000 * 1000 / 100);
        long l = MathUtils.combine(i, j);
        m.put(l, i);
        m.get(l);
      }
    }
    long end = System.nanoTime();
    System.out.println("Time taken for combo: " + (end - start) / Math.pow(10, 9));

    start = System.nanoTime();

    TIntObjectHashMap<TIntIntMap> m2 = new TIntObjectHashMap<>(1000);
    for (int i = 0; i < 1000; i++) {
      m2.put(i, new TIntIntHashMap(1000));
      for (int j = 0; j < 1000; j++) {
        Printing.printProgress(i * 1000 + j, 1000 * 1000 / 100);
        m2.get(i).put(j, i);
        m2.get(i).get(j);
      }
    }
    end = System.nanoTime();
    System.out.println("Time taken for normal: " + (end - start) / Math.pow(10, 9));
  }
}
