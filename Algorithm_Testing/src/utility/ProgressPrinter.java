package utility;

public class ProgressPrinter {

  private final double totalIterations;
  private double nextPrintTime = 0;
  private boolean printedEnd = false;

  public ProgressPrinter(int totalIterations) {
    this.totalIterations = totalIterations;
  }

  public void print(int iteration) {
    if (Printing.debug_level <= 0) {
      return;
    }

    if (iteration > nextPrintTime) {
      StringBuilder s = new StringBuilder("Progress: [");
      s.append("=".repeat((int) Math.max(0, 100 * iteration / totalIterations)));
      s.append(" ".repeat((int) Math.max(0, 100 - 100 * iteration / totalIterations)));
      s.append("] ").append((int) (100 * iteration / totalIterations));
      if (iteration / totalIterations >= 100 && !printedEnd) {
        s.append("%\n");
        printedEnd = true;
      } else {
        s.append("%\r");
      }
      System.out.print(s);
      nextPrintTime += totalIterations / 100.0;
    }
  }

}
