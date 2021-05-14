package framework.heaps;


// adapted from https://www.geeksforgeeks.org/min-heap-in-java/ as it is a basic data structure
public interface MinPriorityHeap {

  void insertOrDecrease(int element);

  void insert(int element);

  boolean isEmpty();

  int pop();

  void remove(int element);

  int peek();

  void decreaseKey(int element);

}
