package algorithms.fastQueue;

import java.util.Arrays;

public class IntFastQueue {

  private int[] items;
  private int start;
  private int end; // points to last+1


  public IntFastQueue() {
    this(100);
  }

  public IntFastQueue(int size) {
    items = new int[size];
    start = 0;
    end = 0;
  }

  public boolean isEmpty() {
    return end == start;
  }

  public int size() {
    if (end >= start) {
      return end - start ;
    } else {
      return end + items.length - start;
    }
  }

  private boolean isFull() {
    return size() == items.length-1;
  }

  public int peek() {
    if (isEmpty()) {
      throw new IndexOutOfBoundsException("Dequeue is empty");
    }
    return items[start];
  }

  public int pop() {
    int ret = peek();
    start = (start + 1) % items.length;
    return ret;
  }

  public void add(int i) {
    if (isFull()) {
      resize();
    }
    items[end] = i;
    end = (end + 1) % items.length;
  }

  private void resize() {
    resize(items.length * 2);
  }

  private void resize(int newSize) {
    int[] newItems = new int[newSize];
    if (start <= end) {
      System.arraycopy(items, start, newItems, 0, size());
      end = size();
    } else {
      System.arraycopy(items, start, newItems, 0, items.length - start);
      System.arraycopy(items, 0, newItems, items.length - start, end );
      end = size() - 1;
    }
    start = 0;
    this.items = newItems;
  }

  @Override
  public String toString() {
    return "IntFastQueue{" +
        "items=" + Arrays.toString(items) +
        ", start=" + start +
        ", end=" + end +
        '}';
  }
}
