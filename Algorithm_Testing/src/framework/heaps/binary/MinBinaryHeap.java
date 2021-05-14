package framework.heaps.binary;

import framework.heaps.MinPriorityHeap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import java.util.Arrays;

public class MinBinaryHeap implements MinPriorityHeap {

  private int[] heap;
  private int size;
  // Used to revert to a simple set in the simple case
  private boolean allSame = true;

  double[] priority;
  TIntIntMap positions;

  public MinBinaryHeap(double[] priority) {
    this(16, priority);
  }

  public MinBinaryHeap(int capacity, double[] priority) {
    this.size = 0;
    heap = new int[capacity + 1];
    this.priority = priority;
    positions = new TIntIntHashMap();
  }

  public MinBinaryHeap(int capacity, double[] priority, int[] existing) {
    capacity = Math.max(capacity, existing.length);
    heap = new int[capacity + 1];
    positions = new TIntIntHashMap();
    this.priority = priority;
    for (int value : existing) {
      this.insertOrDecrease(value);
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
      if (priority[heap[pos]] > priority[heap[leftChild(pos)]]
          || priority[heap[pos]] > priority[heap[rightChild(pos)]]) {
        if (priority[heap[leftChild(pos)]] < priority[heap[rightChild(pos)]]) {
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

  public void insertOrDecrease(int element) {
    if (element >= priority.length) {
      throw new NullPointerException("element not in priority list: " + element);
    }

    if (positions.containsKey(element)) {
      decreaseKey(element);
    } else {
      insert(element);
    }
  }

  @Override
  public void insert(int element) {
    if (size >= heap.length) {
      heap = Arrays.copyOf(heap, heap.length * 2 + 1);
    }
    heap[size] = element;
    positions.put(element, size);

    int current = size;
    if (priority[element] != priority[heap[parent(current)]]) {
      allSame = false;
    }
    while (priority[heap[current]] < priority[heap[parent(current)]]) {
      swap(current, parent(current));
      current = parent(current);
    }
    size++;

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
    }
    return removeByPos(0);
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

  public void decreaseKey(int element) {
    int current = positions.get(element);
    while (priority[heap[current]] < priority[heap[parent(current)]]) {
      swap(current, parent(current));
      current = parent(current);
      allSame = false;
    }
  }

  public void increaseKey(int element) {
    heapify(positions.get(element));
  }

  @Override
  public String toString() {
    return Arrays.toString(Arrays.copyOfRange(heap, 0, size));
  }
}
