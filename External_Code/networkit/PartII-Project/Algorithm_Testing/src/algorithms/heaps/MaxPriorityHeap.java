package algorithms.heaps;


// adapted from https://www.geeksforgeeks.org/min-heap-in-java/ as it is a basic data structure
public interface MaxPriorityHeap {

  void insertOrIncrease(int element);

  boolean isEmpty();

  int pop();

  void remove(int element);

  int peek();

  void increaseKey(int element);


}
