package testing;

import static framework.parsing.Harness.parseOpts;

import algorithms.geisberger.GeisbergerCalculator;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import framework.parsing.Harness;
import java.io.IOException;
import java.util.HashMap;
import utility.Printing;
import utility.Statistics;

public class IntegrationTester {

  public static void main(String[] args) throws IOException, InterruptedException, FileTypeException {
    HashMap<String, String> options = parseOpts(args);

    Printing.set_debug_level(6);
    // Keep g for scope of calculation

    ArrayGraph g = new ArrayGraph(Harness.getFile(options), Harness.getFileType(options));
    System.out.println("initial size:" + g.size());

    System.gc();
    Thread.sleep(1000);
    long start = System.nanoTime();

    Statistics s = GeisbergerCalculator.GeisbergerBisection(g, 800);
    long end = System.nanoTime();
    System.out.println("Time taken for Algorithm: " + (end - start) / Math.pow(10, 9));
    System.out.println("Iterations: " + s.getIterations() + " ended from cutoff: " + s.endedFromCutoff());
  }
}
