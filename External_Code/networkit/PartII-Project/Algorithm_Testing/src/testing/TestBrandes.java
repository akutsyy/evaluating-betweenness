package testing;

import static framework.main.Harness.defaultPercent;
import static framework.main.Harness.getFileType;
import static utility.Printing.printToFile;

import algorithms.brandes.BrandesCalculator;
import com.sun.jdi.InvalidTypeException;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileTypeException;
import framework.main.Harness;
import framework.validation.JGraphTValidate;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import utility.Printing;
import utility.Statistics;

public class TestBrandes {
  public static void main(String[] args)
      throws IOException, FileTypeException, InvalidTypeException, InterruptedException {
    HashMap<String, String> options = Harness.parseOpts(args);
    Statistics statistics;

    Printing.set_debug_level(5);

    // Keep g for scope of calculation
    {
      System.out.println("Building graph");
      ArrayGraph g = new ArrayGraph(Harness.getFile(options), getFileType(options));

      ArrayGraph.GraphAccesses = 0;
      System.gc(); // Hint that we want a garbage collection

      long start = System.nanoTime();
      // DO WORK HERE

      statistics = BrandesCalculator.Brandes(g);

      // END WORK
      long end = System.nanoTime();

      //statistics.addDegree(g);

      printToFile(statistics.getCentrality().toString(), "centrality.txt");
      printToFile(statistics.getTime().toString(), "time.txt");
      //printPointsToFile(statistics.getDegree(), "degree.txt");
      Printing.print_debug(10, "Centralities:" + statistics.getCentrality());
      System.out.println("Size: " + g.size() + " nodes, " + g.edgeSize() + " edges");
      System.out.println("Graph Accesses: " + ArrayGraph.GraphAccesses);
      System.out.println("Time taken: " + (end - start) / Math.pow(10, 9));
      printToFile("Running on: " + Harness.getFile(options).getName() + "\nSize: " + g.size() + " nodes, " + g.edgeSize() + " edges\nTime taken: " + (end - start) / Math.pow(10, 9), "descriptor.txt");
    }
    System.gc(); // Hint that we want a garbage collection
    Thread.sleep(1000);
    {
      System.out.println("Building fresh verification graph");
      ArrayGraph g = new ArrayGraph(Harness.getFile(options), getFileType(options));

      long start = System.nanoTime();
      Map<Integer, Double> groundTruthCentrality = JGraphTValidate.getJGraphTBetweeness(g);
      long end = System.nanoTime();

      System.out.println("Time taken for ground truth: " + (end - start) / Math.pow(10, 9));
      System.out.println("Differing centralities: " + JGraphTValidate
          .compareRelativeBetweeness(statistics.getCentrality(), groundTruthCentrality,
              defaultPercent));
    }

  }
}
