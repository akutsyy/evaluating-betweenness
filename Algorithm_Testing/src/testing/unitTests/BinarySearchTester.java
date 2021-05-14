package testing.unitTests;

import gnu.trove.list.array.TIntArrayList;
import utility.BinarySearch;

public class BinarySearchTester {

  public static void main(String[] args) {
    TIntArrayList array = new TIntArrayList(new int[]{0, 1, 2, 3, 4, 5});
    double[] weights = new double[]{0, 1, 2, 3, 4, 5};
    System.out.println(BinarySearch.binarySearchJustLessThan(array, weights, 5.5));
  }
}
