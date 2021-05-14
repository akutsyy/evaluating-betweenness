package utility;

import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Random;

public class RandomHelper {

  public static int getInPercentile(int[] list, double low, double high) {
    int start = Math.max(0, (int) Math.floor(list.length * low));
    int end = Math.min(list.length, (int) Math.floor((list.length) * high));
    Random r = new Random();
    int index = r.nextInt(end) + start;
    return list[index];
  }

  public static int selectFromWeightedList(Random randGenerator, TIntArrayList list, double[] weights) {
    if (list.size() != weights.length) {
      throw new InputMismatchException(
          "list and weights different sizes: " + list.size() + "," + weights.length);
    }
    double r = randGenerator.nextDouble();
    double sum = Arrays.stream(weights).sum();
    double countWeight = 0.0;
    for (int i = 0; i < weights.length; i++) {
      countWeight += weights[i] / sum;
      if (countWeight >= r) {
        return list.get(i);
      }
    }
    //Can be reached due to floating point issues, where total will be a tiny bit less than 1
    return list.get(list.size() - 1);
  }

  public static int selectFromWeightedList(Random randGenerator, int[] list, double[] weights) {
    if (list.length != weights.length) {
      throw new InputMismatchException(
          "list and weights different sizes: " + list.length + "," + weights.length);
    }
    double r = randGenerator.nextDouble();
    double sum = Arrays.stream(weights).sum();
    double countWeight = 0.0;
    for (int i = 0; i < weights.length; i++) {
      countWeight += weights[i] / sum;
      if (countWeight >= r) {
        return list[i];
      }
    }
    //Can be reached due to floating point issues, where total will be a tiny bit less than 1
    return list[list.length - 1];
  }

  public static int selectWeightedNode(Random randGenerator, int[] nodes, long[] weights) {
    long sum = 0;
    for (int i : nodes) {
      sum += weights[i];
    }
    double r = randGenerator.nextDouble();
    double countWeight = 0.0;

    for (int i = 0; i < weights.length; i++) {
      countWeight += weights[nodes[i]] / (double) sum;
      if (countWeight >= r) {
        return nodes[i];
      }
    }
    //Can be reached due to floating point issues, where total will be a tiny bit less than 1
    return nodes[nodes.length - 1];
  }

}
