package algorithm_evaluation;

import static framework.parsing.Harness.getFile;
import static framework.parsing.Harness.getFileType;
import static framework.parsing.Harness.parseOpts;
import static utility.Printing.printToFile;
import static utility.Printing.print_debug;

import algorithms.brandes.BrandesCalculator;
import algorithms.brandespich2007.BrandesPich2007;
import algorithms.geisberger.GeisbergerCalculator;
import algorithms.kadabra.KADABRACalculator;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import utility.Printing;
import utility.Statistics;

public class MainHarness {

  public static final String[] graphParameters = {
          "--file datasets/final/ca-astroph_undirected.txt --file_type Undirected_ID_ID_List",
      "--file datasets/final/4932.protein.links_undirected_weighted.csv --file_type Undirected_Weighted_CSV",
      "--file datasets/final/com-amazon_undirected.txt --file_type Undirected_ID_ID_List",
      "--file datasets/final/slashdot0811_directed.txt --file_type Directed_ID_ID_List",
      "--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List"};

  public static final String[] shortGraphNames = {
          "ca-astroph",
          "4932-protein",
      "com-amazon",
      "slashdot0811",
      "as-caida20071105"
  };

  public static void runMainTests(String[] args) throws IOException, FileTypeException, InterruptedException {

    Printing.set_debug_level(3);
    final int numberOfIterations = 3;

    final int[] pivots = {
        25, 50, 100, 200, 400, 800, 1600, 3200
    };

    final int[] geisbergerSamples = {1, 2, 4, 8, 16};
    final double[] kadabraLambdas = {0.01, 0.025, 0.02, 0.015, 0.005, 0.001};
    final double kadabraDelta = 0.1;

    warmup("--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List");

    for (int graphNum = 0; graphNum < graphParameters.length; graphNum++) {
      String parameter = graphParameters[graphNum];
      HashMap<String, String> options = parseOpts(parameter);
      long start;
      long end;
      ArrayGraph g;
      Statistics s;

      for (int i = 0; i < numberOfIterations; i++) {
        String name = "Brandes_" + i + "_" + shortGraphNames[graphNum];
        if (!outputExists(name)) {
          System.out.println("Doing Brandes for " + shortGraphNames[graphNum]);
          // Brandes
          g = getGraph(options);
          // Only do Brandes once on very large graphs
          if (i == 0 || g.size() < 100000) {
            ArrayGraph.EdgeTraversals = 0;
            start = System.nanoTime();
            s = BrandesCalculator.Brandes(g);
            end = System.nanoTime();
            printInfo("Brandes", "None", options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
            printCentrality(s, name);
          }
        } else {
          System.out.print("skipping brandes");
        }
        // Brandes and Pich (2008)
        for (int p : pivots) {
          name = "BrandesAndPich2008_" + p + "_" + i + "_" + shortGraphNames[graphNum];
          if (!outputExists(name)) {
            g = getGraph(options);
            ArrayGraph.EdgeTraversals = 0;
            start = System.nanoTime();
            s = BrandesPich2007.BrandesRandom(g, p);
            end = System.nanoTime();
            printInfo("BrandesAndPich2008", "pivots=" + p, options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
            printCentrality(s, name);
          }
        }

        // Geisberger et al. Linear
        for (int p : pivots) {
          name = "GeisbergerLinear_" + p + "_" + i + "_" + shortGraphNames[graphNum];
          if (!outputExists(name)) {
            g = getGraph(options);
            ArrayGraph.EdgeTraversals = 0;
            start = System.nanoTime();
            s = GeisbergerCalculator.GeisbergerLinear(g, p);
            end = System.nanoTime();
            printInfo("GeisbergerLinear", "pivots=" + p, options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
            printCentrality(s, name);
          }
        }

        // Geisberger et al. Bisection
        for (int p : pivots) {
          name = "GeisbergerBisection_" + p + "_" + i + "_" + shortGraphNames[graphNum];
          if (!outputExists(name)) {
            g = getGraph(options);
            ArrayGraph.EdgeTraversals = 0;
            start = System.nanoTime();
            s = GeisbergerCalculator.GeisbergerBisection(g, p);
            end = System.nanoTime();
            printInfo("GeisbergerBisection", "pivots=" + p, options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
            printCentrality(s, name);
          }
        }

        // Geisberger et al. Bisection Sampled
        for (int p : pivots) {
          for (int sampleNum : geisbergerSamples) {
            name = "GeisbergerBisectionSampled_" + p + "_" + sampleNum + "_" + i + "_" + shortGraphNames[graphNum];
            if (!outputExists(name)) {
              g = getGraph(options);
              ArrayGraph.EdgeTraversals = 0;
              start = System.nanoTime();
              s = GeisbergerCalculator.GeisbergerBisectionSampling(g, p, sampleNum);
              end = System.nanoTime();
              printInfo("GeisbergerBisectionSampled", "pivots=" + p + ", samples=" + sampleNum, options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
              printCentrality(s, name);
            }
          }
        }
        // KADABRA
        for (double lambda : kadabraLambdas) {
          name = "KADABRA_" + lambda + "_" + kadabraDelta + "_" + i + "_" + shortGraphNames[graphNum];
          if (!outputExists(name)) {
            g = getGraph(options);
            ArrayGraph.EdgeTraversals = 0;
            start = System.nanoTime();
            s = KADABRACalculator.KADABRA(g, lambda, kadabraDelta);
            end = System.nanoTime();
            printInfo("KADABRA", "lambda=" + lambda + ", delta=" + kadabraDelta + "terminated from cutoff: " + s.endedFromCutoff() + " iterations: " + s.getIterations(), options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
            printCentrality(s, name);
          }
        }
      }
    }
  }

  public static void warmup(String parameter) throws InterruptedException, FileTypeException, IOException {
    print_debug(2, "Doing warmup");
    for (int i = 0; i < 3; i++) {
      HashMap<String, String> options = parseOpts(parameter);
      ArrayGraph g = getGraph(options);
      BrandesCalculator.Brandes(g);
    }
    System.gc();
    Thread.sleep(2000);
  }

  static boolean outputExists(String uniqueName) {
    File f = new File("centralities/" + uniqueName + "centrality.txt");
    File f2 = new File("descriptions/" + uniqueName + "_descriptor.txt");
    return f.exists() && !f.isDirectory() && f2.exists() && !f2.isDirectory();
  }

  static void printInfo(String algorithm, String parameters, String name, int size, int edgeSize, double time, long accesses, String uniqueName) {
    print_debug(2, "Completed " + uniqueName + " in time " + time + "s");
    printToFile("Running " + algorithm + " with parameters " + parameters + " on: " + name +
        "\nSize: " + size + " nodes, " + edgeSize + " edges"
        + "\nTime taken: " + time +
        "\n Graph accesses: " + accesses, "descriptions/" + uniqueName + "_descriptor.txt");
  }

  static void printCentrality(Statistics s, String uniqueName) {
    printToFile(s.getCentrality().toString(), "centralities/" + uniqueName + "centrality.txt");
  }

  public static ArrayGraph getGraph(HashMap<String, String> options) throws IOException, FileTypeException, InterruptedException {
    ArrayGraph g = new ArrayGraph(getFile(options), getFileType(options));
    System.gc();
    Thread.sleep(2000);
    return g;
  }
}
