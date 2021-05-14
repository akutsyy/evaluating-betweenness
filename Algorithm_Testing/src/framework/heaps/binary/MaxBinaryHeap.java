package framework.heaps.binary;

import framework.heaps.MaxPriorityHeap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import java.util.Arrays;

public class MaxBinaryHeap implements MaxPriorityHeap {

  private int[] heap;
  private int size;
  // Used to revert to a simple set in the simple case
  private boolean allSame = true;

  double[] p;
  TIntIntMap positions;


  public MaxBinaryHeap(int capacity, double[] priority) {
    this.size = 0;
    heap = new int[capacity + 1];
    this.p = priority;
    positions = new TIntIntHashMap();
  }

  public MaxBinaryHeap(int capacity, double[] priority, int[] existing) {
    capacity = Math.max(capacity, existing.length);
    heap = new int[capacity + 1];
    positions = new TIntIntHashMap();
    this.p = priority;
    for (int value : existing) {
      this.insertOrIncrease(value);
    }
  }


  private int parent(int pos) {
    return pos / 2;
  }

  private int leftChild(int pos) {
    return 2 * pos;
  }

  private int rightChild(int pos) {
    return 2 * pos + 1;
  }

  private boolean isLeaf(int pos) {
    return pos >= (size / 2) && pos <= size;
  }

  public boolean contains(int element) {
    return positions.containsKey(element);
  }

  private void swap(int a, int b) {
    positions.put(heap[a], b);
    positions.put(heap[b], a);

    int tmp = heap[a];
    heap[a] = heap[b];
    heap[b] = tmp;
  }

  private void heapify(int pos) {
    if (!isLeaf(pos)) {
      if (p[heap[pos]] < p[heap[leftChild(pos)]]
          || p[heap[pos]] < p[heap[rightChild(pos)]]) {
        if (p[heap[leftChild(pos)]] > p[heap[rightChild(pos)]]) {
          swap(pos, leftChild(pos));
          heapify(leftChild(pos));
        } else {
          swap(pos, rightChild(pos));
          heapify(rightChild(pos));
        }
        allSame = false;
      }
    }
  }

  public void insertOrIncrease(int element) {
    if (element >= p.length) {
      throw new NullPointerException("element not in priority list: " + element);
    }
    if (positions.containsKey(element)) {
      increaseKey(element);
    } else {
      if (size >= heap.length) {
        heap = Arrays.copyOf(heap, heap.length * 2 + 1);
      }
      heap[size] = element;
      positions.put(element, size);

      int current = size;
      while (p[heap[current]] > p[heap[parent(current)]]) {
        allSame = false;
        swap(current, parent(current));
        current = parent(current);
      }
      size++;
    }
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size <= 0;
  }

  public int pop() {
    if (allSame) {
      return removeByPos(size - 1);
    } else {
      return removeByPos(0);
    }
  }

  public void remove(int element) {
    removeByPos(positions.get(element));
  }

  public int removeByPos(int pos) {

    if (pos >= size) {
      throw new NullPointerException("Element out of bounds: " + pos + " for heap of size " + size);
    }
    int removed = heap[pos];
    swap(pos, size - 1);
    size--;
    heapify(pos);
    positions.remove(removed);
    return removed;
  }

  public int peek() {
    if (isEmpty()) {
      throw new NullPointerException("Heap is empty");
    }
    if (allSame) {
      return heap[size - 1];
    } else {
      return heap[0];
    }
  }

  public void increaseKey(int element) {
    int current = positions.get(element);
    while (p[heap[current]] > p[heap[parent(current)]]) {
      swap(current, parent(current));
      current = parent(current);
      allSame = false;
    }
  }

  public void decreaseKey(int element) {
    heapify(positions.get(element));
  }


  @Override
  public String toString() {
    return Arrays.toString(Arrays.copyOfRange(heap, 0, size));
  }
}
