package algorithms.heaps.fibonacci;

import algorithms.heaps.MinPriorityHeap;
import algorithms.heaps.fibonacci.SchwarzFibonacciHeap.Entry;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MinFibonacciHeap implements MinPriorityHeap {

  private final SchwarzFibonacciHeap<Integer> SFibHeap;
  double[] valueMap;
  TIntObjectMap<Entry<Integer>> pointerMap;

  public MinFibonacciHeap(int size, double[] valueMap) {
    SFibHeap = new SchwarzFibonacciHeap<>();
    this.valueMap = valueMap;
    pointerMap = new TIntObjectHashMap<>(size);
  }

  public MinFibonacciHeap(double[] valueMap) {
    this(10, valueMap);
  }


  public Iterator<Integer> iterator() {
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return !SFibHeap.isEmpty();
      }

      @Override
      public Integer next() {
        if (!this.hasNext()) {
          throw new NoSuchElementException();
        }
        return SFibHeap.dequeueMin().getValue();
      }
    };
  }

  public int size() {
    return SFibHeap.size();
  }

  @Override
  public void insertOrDecrease(int e) {
    if (pointerMap.containsKey(e)) {
      this.decreaseKey(e);
    } else {
      this.insert(e);
    }
  }

  @Override
  public void insert(int e) {
    pointerMap.put(e, SFibHeap.enqueue(e, valueMap[e]));
  }

  @Override
  public boolean isEmpty() {
    return SFibHeap.isEmpty();
  }

  @Override
  public void remove(int element) {
    SFibHeap.delete(pointerMap.get(element));
  }


  @Override
  public int pop() {
    Integer dequeued = SFibHeap.dequeueMin().getValue();
    pointerMap.remove(dequeued);
    return dequeued;
  }

  @Override
  public int peek() {
    return SFibHeap.min().getValue();
  }

  @Override
  public void decreaseKey(int element) {
    SFibHeap.decreaseKey(pointerMap.get(element), valueMap[element]);
  }


  public void clear() {
    SFibHeap.clear();
  }

}
