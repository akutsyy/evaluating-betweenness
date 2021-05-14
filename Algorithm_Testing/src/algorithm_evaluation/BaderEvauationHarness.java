package algorithm_evaluation;

import static algorithm_evaluation.GraphAccessHarness.printGraphAccesses;
import static algorithm_evaluation.MainHarness.getGraph;
import static algorithm_evaluation.MainHarness.outputExists;
import static algorithm_evaluation.MainHarness.printCentrality;
import static algorithm_evaluation.MainHarness.printInfo;
import static algorithm_evaluation.MainHarness.warmup;
import static data_processing.NumberOfInversions.getOrder;
import static data_processing.ValuesFromFile.getValuesFromFile;
import static framework.parsing.Harness.parseOpts;
import static utility.RandomHelper.getInPercentile;

import algorithms.bader.BaderCalculator;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import java.io.IOException;
import java.util.HashMap;
import utility.Printing;
import utility.Statistics;

public class BaderEvauationHarness {

  public static void runBaderTests(String[] args) throws InterruptedException, FileTypeException, IOException {
    Printing.set_debug_level(3);
    final int numberOfIterations = 10;

    final String[] groundTruthFiles = {
            "Brandes_0_ca-astrophcentrality.txt",
            "Brandes_0_4932-proteincentrality.txt",
        "Brandes_0_as-caida20071105centrality.txt",
        "Brandes_0_slashdot0811centrality.txt",
    };

    final String[] graphParameters = {
            "--file datasets/final/ca-astroph_undirected.txt --file_type Undirected_ID_ID_List",
            "--file datasets/final/4932.protein.links_undirected_weighted.csv --file_type Undirected_Weighted_CSV",
            "--file datasets/final/slashdot0811_directed.txt --file_type Directed_ID_ID_List",
            "--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List"};

    final String[] shortGraphNames = {
            "ca-astroph",
            "4932-protein",
            "slashdot0811",
            "as-caida20071105"
    };

    final String centralityDirectory = "centralities/";

    final double[] lowPercentile = {0, 0, 0, 0};
    final double[] highPercentile = {0.001, 0.01, 0.1, 0.2};

    final int[] alphas = {2, 5};

    warmup("--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List");

    for (int i = 0; i < numberOfIterations; i++) {
      for (int alpha : alphas) {
        for (int graphNum = 0; graphNum < graphParameters.length; graphNum++) {
          doIteration(graphParameters[graphNum], centralityDirectory, groundTruthFiles[graphNum],
              lowPercentile, highPercentile, i, shortGraphNames[graphNum], alpha);
        }
      }
    }
  }

  private static void doIteration(String parameter, String centralityDirectory, String groundTruthFile,
      double[] lowPercentile, double[] highPercentile, int i, String shortGraphName, int alpha)
      throws IOException, FileTypeException, InterruptedException {
    HashMap<String, String> options = parseOpts(parameter);
    int[] ordered = getOrder(getValuesFromFile(centralityDirectory + groundTruthFile));
    ArrayGraph g;
    Statistics s;
    String name;

    long start;
    long end;

    // Bader et al.
    for (int j = 0; j < lowPercentile.length; j++) {
      double low = lowPercentile[j];
      double high = highPercentile[j];
      name = "Bader_" + low + "_" + high + "_" + alpha + "_" + i + "_" + shortGraphName;
      if (!outputExists(name)) {
        int vertex = getInPercentile(ordered, low, high);
        g = getGraph(options);
        ArrayGraph.EdgeTraversals = 0;
        start = System.nanoTime();
        s = BaderCalculator.Bader(g, vertex, alpha);
        end = System.nanoTime();
        printInfo("Bader", "Low=" + low + "'\nHigh=" + high + "\nTerminated from max iterations cutoff:" + s.endedFromCutoff() +
                "\nAfter iterations: " + s.getIterations(),
            options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
        printGraphAccesses("Bader", "Low=" + low + "'\nHigh=" + high,
            options.get("file"), ArrayGraph.EdgeTraversals, name);
        printCentrality(s, name);
      }
    }
  }
}
