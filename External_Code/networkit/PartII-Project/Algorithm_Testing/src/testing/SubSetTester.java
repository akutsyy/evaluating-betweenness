package testing;

import static framework.main.Harness.getFile;
import static framework.main.Harness.parseOpts;
import static utility.Printing.printToFile;
import static utility.Printing.print_debug;

import algorithms.brandes.BrandesSubsetCalculator;
import algorithms.brandespp.BrandesPPCalculator;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileTypeException;
import framework.main.Harness;
import framework.validation.JGraphTValidate;
import gnu.trove.set.TIntSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import gnu.trove.set.hash.TIntHashSet;
import utility.ArrayGenerator;
import utility.Printing;
import utility.Statistics;

public class SubSetTester {

  public static void main(String[] args)
      throws IOException, FileTypeException, InterruptedException {
    HashMap<String, String> options = parseOpts(args);
    Printing.set_debug_level(9);
    Statistics statistics;
    TIntSet targets;
    // Keep g for scope of calculation
    {
      System.out.println("Building graph");
      ArrayGraph g = new ArrayGraph(getFile(options), Harness.getFileType(options));

      System.out.println("Initial size:" + g.size());
      targets = BrandesSubsetCalculator.generateSources(g, 0.01);
      System.out.println("Using target subset of size " + targets.size());
      ArrayGraph.GraphAccesses = 0;
      System.gc();
      long start = System.nanoTime();

      // DO WORK
      statistics = BrandesPPCalculator.brandesPP(g, targets, 8, 5, 5, 100);

      // END WORK
      long end = System.nanoTime();
      System.out.println("Time taken for brandesPP: " + (end - start) / Math.pow(10, 9));

      //printToFile(statistics.getCentrality().toString(), "centrality.txt");

      print_debug(10, "Centralities:" + statistics.getCentrality());
      System.out.println("Size: " + g.size() + " nodes, " + g.edgeSize() + " edges");
      System.out.println("Graph Accesses: " + ArrayGraph.GraphAccesses);
      System.out.println("Time taken: " + (end - start) / Math.pow(10, 9));
      //printToFile("Running on: " + getFile(options).getName() + "\nSize: " + g.size() + " nodes, " + g.edgeSize() + " edges\nTime taken: " + (end - start) / Math.pow(10, 9), "descriptor.txt");
    }

    System.gc(); // Hint that we want a garbage collection
    Thread.sleep(1000);

    {
      System.out.println("Building Verification Graph");
      ArrayGraph g = new ArrayGraph(getFile(options), Harness.getFileType(options));

      long start = System.nanoTime();

      Statistics brandesCentrality = BrandesSubsetCalculator.BrandesSubset(g, targets);

      long end = System.nanoTime();
      System.out.println("Time taken for brandes: " + (end - start) / Math.pow(10, 9));

      System.out.println("Verifying...");
      System.out.println(JGraphTValidate.compareBetweeness(
          brandesCentrality.getCentrality(), "brandes",
          statistics.getCentrality(), "brandes++",
          Harness.defaultEpsilon) + " errors");
    }
  }
}
