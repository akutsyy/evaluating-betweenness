package algorithm_evaluation;

import static algorithm_evaluation.GraphAccessHarness.printGraphAccesses;
import static algorithm_evaluation.MainHarness.getGraph;
import static algorithm_evaluation.MainHarness.outputExists;
import static algorithm_evaluation.MainHarness.printCentrality;
import static algorithm_evaluation.MainHarness.printInfo;
import static algorithm_evaluation.MainHarness.warmup;
import static framework.parsing.Harness.parseOpts;

import algorithms.brandes.BrandesSubsetCalculator;
import algorithms.brandespp.BrandesPPCalculator;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import gnu.trove.set.TIntSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import utility.Printing;
import utility.Statistics;

public class SubsetEvaluationHarness {

  public static void runSubSetTests(String[] args) throws InterruptedException, FileTypeException, IOException {
    Printing.set_debug_level(3);
    final int numberOfIterations = 3;

    final String[] graphParameters = {
        "--file datasets/final/ca-astroph_undirected.txt --file_type Undirected_ID_ID_List",
        "--file datasets/final/4932.protein.links_undirected_weighted.csv --file_type Undirected_Weighted_CSV",
        "--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List"};

    final String[] shortGraphNames = {
        "ca-astroph",
        "4932-protein",
        "as-caida20071105"
    };

    final double[] setPercentages = {0.001, 0.01, 0.05, 0.1};
    final int[] numPartitions = {2, 8, 32, 128, 512, 2048};

    warmup("--file datasets/final/wiki-vote_directed.txt --file_type Directed_ID_ID_List");

    for (int graphNum = 0; graphNum < graphParameters.length; graphNum++) {
      String parameter = graphParameters[graphNum];
      HashMap<String, String> options = parseOpts(parameter);
      ArrayGraph g;
      Statistics s;
      String name;

      long start;
      long end;

      for (int i = 0; i < numberOfIterations; i++) {
        for (double p : setPercentages) {
          TIntSet set = BrandesSubsetCalculator.generateSources(getGraph(options), p);
          name = "BrandesSubset_" + p + "_" + i + "_" + shortGraphNames[graphNum];
          if (!outputExists(name)) {
            g = getGraph(options);
            ArrayGraph.EdgeTraversals = 0;
            start = System.nanoTime();
            s = BrandesSubsetCalculator.BrandesSubset(g, set);
            end = System.nanoTime();
            printInfo("BrandesSubset", "Set Percentage=" + p + "'\nSet Size=" + set.size(),
                options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
            printGraphAccesses("BrandesSubset", "Set Percentage=" + p + "'\nSet Size=" + set.size(), options.get("file"), ArrayGraph.EdgeTraversals, name);
            printCentrality(s, name);
          }

          for (int partitions : numPartitions) {
            name = "BrandesPP_" + p + "_" + partitions + "_" + i + "_" + shortGraphNames[graphNum];
            if (!outputExists(name)) {
              g = getGraph(options);
              ArrayGraph.EdgeTraversals = 0;
              start = System.nanoTime();
              s = BrandesPPCalculator.brandesPP(g, set, partitions);
              end = System.nanoTime();
              printInfo("BrandesPP", "Set Percentage=" + p + "'\nSet Size=" + set.size() + "\nNumPartitions=" + partitions +
                      "\nTimes=" + Arrays.toString(s.getPartTimes()),
                  options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.EdgeTraversals, name);
              printGraphAccesses("BrandesPP", "Set Percentage=" + p + "'\nSet Size=" + set.size() + "\nNumPartitions=" + partitions,
                  options.get("file"), ArrayGraph.EdgeTraversals, name);
              printCentrality(s, name);
            }
          }
        }
      }
    }
  }
}
