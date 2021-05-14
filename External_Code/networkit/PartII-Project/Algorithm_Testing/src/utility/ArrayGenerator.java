package utility;

import java.util.Random;

public class ArrayGenerator {

  public static double[] randDoubles(int num) {
    return randDoubles(0, 1, num);
  }

  public static double[] randDoubles(double min, double max, int num) {
    Random r = new Random();
    double[] nums = new double[num];
    for (int i = 0; i < nums.length; i++) {
      nums[i] = (r.nextDouble() * (max - min) + min);
    }
    return nums;
  }

  public static int[] shuffledInts(int end) {
    return shuffledInts(0, end);
  }

  public static int[] shuffledInts(int start, int end) {
    //array to store N random integers (0 - N-1)
    int[] nums = new int[end - start];

    for (int i = 0; i < nums.length; ++i) {
      nums[i] = i;
    }
    Random randomGenerator = new Random();
    int randomIndex; // the randomly selected index each time through the loop
    int randomValue; // the value at nums[randomIndex] each time through the loop

    // randomize order of values
    for (int i = 0; i < nums.length; ++i) {
      // select a random index
      randomIndex = randomGenerator.nextInt(nums.length);

      // swap values
      randomValue = nums[randomIndex];
      nums[randomIndex] = nums[i];
      nums[i] = randomValue;
    }
    return nums;
  }

  public static int[] range(int end) {
    return range(0, end);
  }

  public static int[] range(int start, int end) {
    int[] nums = new int[end - start];

    for (int i = 0; i < nums.length; ++i) {
      nums[i] = i;
    }
    return nums;
  }
}
