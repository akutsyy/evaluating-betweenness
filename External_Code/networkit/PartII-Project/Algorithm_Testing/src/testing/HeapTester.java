package testing;

import algorithms.heaps.MinPriorityHeap;
import algorithms.heaps.binary.MinBinaryHeap;
import algorithms.heaps.fibonacci.MinFibonacciHeap;
import algorithms.heaps.rankpair.MinRankPairingHeap;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Random;
import org.jgrapht.alg.util.Pair;
import org.jheaps.AddressableHeap.Handle;
import org.jheaps.tree.RankPairingHeap;
import utility.ArrayGenerator;

public class HeapTester {

  public static void main(String[] args) throws InterruptedException {
    int[] elements = ArrayGenerator.range(100000);
    double[] weights = ArrayGenerator.randDoubles(100000);

    double[] w1 = Arrays.copyOf(weights, weights.length);
    double[] w2 = Arrays.copyOf(weights, weights.length);
    double[] w3 = Arrays.copyOf(weights, weights.length);
    double[] w4 = Arrays.copyOf(weights, weights.length);
    double[] w5 = Arrays.copyOf(weights, weights.length);

    double[] less = Arrays.copyOf(weights, weights.length);
    Random r = new Random();
    for (int i = 0; i < less.length; i++) {
      less[i] -= r.nextDouble();
    }

    long start = System.nanoTime();
    TIntArrayList binaryOut = timeHeap(new MinBinaryHeap(elements.length, w1), w1, less, elements);
    long end = System.nanoTime();
    System.out.println("Time taken for binary heap: " + (end - start) / Math.pow(10, 9));

    System.gc();
    Thread.sleep(1000);

    start = System.nanoTime();
    TIntArrayList rankOut = timeHeap(new MinRankPairingHeap(elements.length, w2), w2, less, elements);
    end = System.nanoTime();
    System.out.println("Time taken for rank pair heap: " + (end - start) / Math.pow(10, 9));

    System.gc();
    Thread.sleep(1000);

    start = System.nanoTime();
    TIntArrayList fibOut = timeHeap(new MinFibonacciHeap(elements.length, w3), w3, less, elements);
    end = System.nanoTime();
    System.out.println("Time taken for fibonacci heap: " + (end - start) / Math.pow(10, 9));

    System.gc();
    Thread.sleep(1000);

    start = System.nanoTime();
    TIntArrayList jRankOut = timeJGraphTRankHeap(elements, w4, less);
    end = System.nanoTime();
    System.out.println("Time taken for JRankPairingHeap heap: " + (end - start) / Math.pow(10, 9));

    System.gc();
    Thread.sleep(1000);

    start = System.nanoTime();
    TIntArrayList pOut = timeJavaHeap(elements, w5, less);
    end = System.nanoTime();
    System.out.println("Time taken for Java Priority Queue heap: " + (end - start) / Math.pow(10, 9));

    System.out.println("Binary and rank equal: " + binaryOut.equals(rankOut));
    System.out.println("Binary and fibonacci equal: " + binaryOut.equals(fibOut));
    System.out.println("Binary and JRankPairingHeap equal: " + binaryOut.equals(jRankOut));
    System.out.println("Binary and Java Priority equal: " + binaryOut.equals(pOut));

  }


  public static TIntArrayList timeHeap(MinPriorityHeap a, double[] weights, double[] newWeights, int[] elements) {

    TIntArrayList aOut = new TIntArrayList(weights.length / 3);
    for (int element : elements) {
      a.insert(element);
    }

    for (int i = 0; i < weights.length; i++) {
      weights[i] = newWeights[i];
      a.decreaseKey(elements[i]);
    }
    for (int i = 0; i < weights.length; i += 3) {
      aOut.add(a.pop());
    }
    TIntIterator it = aOut.iterator();
    while (it.hasNext()) {
      int i = it.next();
      a.insert(i);
    }
    while (!a.isEmpty()) {
      aOut.add(a.pop());
    }
    return aOut;
  }

  public static boolean testHeaps(int[] elements, double[] weights, MinPriorityHeap a,
      MinPriorityHeap b) {

    TIntArrayList aOut = new TIntArrayList();
    TIntArrayList bOut = new TIntArrayList();

    for (int element : elements) {
      a.insert(element);
      b.insert(element);
    }

    Random r = new Random();
    for (int i = 0; i < weights.length; i++) {
      weights[i] = weights[i] - r.nextDouble();
      a.decreaseKey(elements[i]);
      b.decreaseKey(elements[i]);
    }
    for (int i = 0; i < weights.length; i += 3) {
      aOut.add(a.pop());
      bOut.add(b.pop());
    }
    TIntIterator it = aOut.iterator();
    while (it.hasNext()) {
      int i = it.next();
      a.insert(i);
      b.insert(i);
    }
    while (!a.isEmpty()) {
      aOut.add(a.pop());
      bOut.add(b.pop());
    }

    return aOut.equals(bOut);
  }

  public static TIntArrayList timeJGraphTRankHeap(int[] elements, double[] weights, double[] newWeights) {

    TIntObjectMap<Handle<Double, Integer>> pointerMap = new TIntObjectHashMap<>();
    RankPairingHeap<Double, Integer> a = new RankPairingHeap<>();
    TIntArrayList aOut = new TIntArrayList(weights.length / 3);
    for (int element : elements) {
      pointerMap.put(element, a.insert(weights[element], element));
    }

    for (int i = 0; i < weights.length; i++) {
      pointerMap.get(elements[i]).decreaseKey(newWeights[i]);
    }
    for (int i = 0; i < weights.length; i += 3) {
      aOut.add(a.deleteMin().getValue());
    }
    TIntIterator it = aOut.iterator();
    while (it.hasNext()) {
      int i = it.next();
      a.insert(weights[i], i);
    }
    while (!a.isEmpty()) {
      aOut.add(a.deleteMin().getValue());
    }
    return aOut;
  }

  public static TIntArrayList timeJavaHeap(int[] elements, double[] weights, double[] newWeights) {

    PriorityQueue<Integer> a = new PriorityQueue<>(
        (o1, o2) -> (int) Math.signum(weights[o1]-weights[o2]));

    TIntObjectMap<Integer> pointerMap = new TIntObjectHashMap<>();

    TIntArrayList aOut = new TIntArrayList(weights.length / 3);
    for (int element : elements) {
      pointerMap.put(element,element);
      a.add(element);
    }

    for (int i = 0; i < weights.length; i++) {
      a.remove(pointerMap.get(elements[i]));
      weights[i] = newWeights[i];
      a.add(pointerMap.get(elements[i]));
    }
    for (int i = 0; i < weights.length; i += 3) {
      aOut.add(a.poll());
    }
    TIntIterator it = aOut.iterator();
    while (it.hasNext()) {
      int i = it.next();
      a.add(pointerMap.get(i));
    }
    while (!a.isEmpty()) {
      aOut.add(a.poll());
    }
    return aOut;
  }
}
