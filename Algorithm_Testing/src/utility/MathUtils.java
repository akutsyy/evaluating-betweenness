package utility;

public class MathUtils {

  public static final double EPSILON = 0.001;

  public static boolean isPowerOfTwo(int x) {
    return (x != 0) && ((x & (x - 1)) == 0);
  }

  public static boolean fuzzyEquals(double a, double b, double epsilon) {
    return Math.abs(a - b) < epsilon;
  }

  public static boolean fuzzyEquals(double a, double b) {
    return Math.abs(a - b) < EPSILON;
  }

  public static boolean percentEquals(double a, double b, double percent) {
    double diff = Math.abs(a - b);
    if (diff == 0) {
      return true;
    }
    return 100 * diff / Math.max(a, b) < percent;
  }

  public static int first(long l) {
    return (int) (l >>> 32);
  }

  public static int second(long l) {
    return (int) (l & ~0);
  }

  public static long combine(int first, int second) {
    return ((long) first << 32) | (Integer.toUnsignedLong(second));
  }

}
