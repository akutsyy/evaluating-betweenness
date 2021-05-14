package framework.heaps.fibonacci;

import framework.heaps.fibonacci.SchwarzFibonacciHeap.Entry;
import framework.graphs.HasID;
import java.util.AbstractQueue;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class MyGenericFibonacciHeap<E extends HasID> extends AbstractQueue<E> {

  //Mangle the fibonacci heap to use our value of type E as the key, with a custom comparator
  private final SchwarzFibonacciHeap<E> SFibHeap;
  double[] valueMap;
  Map<E, Entry<E>> pointerMap;

  public MyGenericFibonacciHeap(double[] valueMap) {
    SFibHeap = new SchwarzFibonacciHeap<>();
    this.valueMap = valueMap;
    pointerMap = new HashMap<>();
  }


  @Override
  public Iterator<E> iterator() {
    return new Iterator<>() {
      @Override
      public boolean hasNext() {
        return !SFibHeap.isEmpty();
      }

      @Override
      public E next() {
        if (!this.hasNext()) {
          throw new NoSuchElementException();
        }
        return SFibHeap.dequeueMin().getValue();
      }
    };
  }

  @Override
  public int size() {
    return SFibHeap.size();
  }

  @Override
  public boolean offer(E e) {
    pointerMap.put(e, SFibHeap.enqueue(e, valueMap[e.getID()]));
    return true;
  }

  @Override
  public E poll() {
    if (SFibHeap.isEmpty()) {
      return null;
    }
    E dequeued = SFibHeap.dequeueMin().getValue();
    pointerMap.remove(dequeued);
    return dequeued;
  }

  @Override
  public E peek() {
    if (SFibHeap.isEmpty()) {
      return null;
    }
    return SFibHeap.min().getValue();
  }

  @Override
  public void clear() {
    SFibHeap.clear();
  }

  public void insertOrDecreaseKey(E e) {
    if (!pointerMap.containsKey(e)) {
      this.add(e);
    } else {
      SFibHeap.decreaseKey(pointerMap.get(e), valueMap[e.getID()]);
    }
  }
}
