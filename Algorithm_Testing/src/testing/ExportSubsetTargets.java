package testing;

import static utility.Printing.printToFile;

import algorithm_evaluation.MainHarness;
import algorithms.brandes.BrandesSubsetCalculator;
import algorithms.brandespp.BrandesPPCalculator;
import algorithms.brandespp.metis.Metis;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import framework.parsing.Harness;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import testing.dataValidation.DoesGraphHaveAllNodes;
import utility.Statistics;

public class ExportSubsetTargets {

  public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {

    for (double p : new double[]{0.001, 0.01, 0.1}) {
      HashMap<String, String> options = Harness.parseOpts(args);
      String separator = "\\s";
      if (!DoesGraphHaveAllNodes.doesFileHaveAllNodes(args, separator)) {
        System.out.println("Cannot continue, graph needs to have all nodes 0...n");
        return;
      }
      if (!DoesGraphHaveAllNodes.doesFileStartAtZero(args, separator)) {
        System.out.println("Cannot continue, graph needs to start at node 0");
        return;
      }

      ArrayGraph g = MainHarness.getGraph(options);
      String graphName = options.get("file").split("/")[options.get("file").split("/").length - 1].split("\\.")[0];
      System.out.println("Graph name is" + graphName);

      TIntSet targets = BrandesSubsetCalculator.generateSources(g, p);
      printTargets(targets, "evaluation/brandesPPData/sources_" + p + ".txt");
      long start = System.nanoTime();
      ArrayList<TIntSet> partitions = Metis.metisToPartitions(g, 100, 3, 5, 100);
      long end = System.nanoTime();
      double partitionTime = (end - start) / Math.pow(10, 9);
      printPartitions(partitions, "evaluation/brandesPPData/clusterfile_" + p + ".txt");

      System.gc();
      Thread.sleep(5000);
      start = System.nanoTime();
      Statistics s = BrandesSubsetCalculator.BrandesSubset(g, targets);
      end = System.nanoTime();
      printToFile("File: " + options.get("file") + "\n Time: " + (end - start) / Math.pow(10, 9), "evaluation/brandesPPData/descriptions/BrandesSubset_" + p + ".txt");
      printToFile(s.getCentrality().toString(), "evaluation/brandesPPData/centralities/BrandesSubset_" + p + ".txt");

      System.gc();
      Thread.sleep(5000);
      start = System.nanoTime();
      s = BrandesPPCalculator.brandesPP(g, targets, partitions);
      end = System.nanoTime();
      double[] portions = s.getPartTimes();
      portions[0] = partitionTime;

      printToFile("File: " + options.get("file") + "\n Time: " + (end - start) / Math.pow(10, 9) + "\n Time portions: " + Arrays.toString(portions),
          "evaluation/brandesPPData/descriptions/BrandesPP" + p + ".txt");
      printToFile(s.getCentrality().toString(), "evaluation/brandesPPData/centralities/BrandesPP" + p + ".txt");

    }
  }

  // Prints partitions in format acceptable by Erdos Brandes++ implementation
  private static void printPartitions(ArrayList<TIntSet> partitions, String filename) {
    TIntIntMap map = new TIntIntHashMap();

    for (int i = 0; i < partitions.size(); i++) {
      TIntIterator it = partitions.get(i).iterator();
      while (it.hasNext()) {
        map.put(it.next(), i);
      }
    }
    StringBuilder s = new StringBuilder();
    for (int i = 0; i < map.size(); i++) {
      if (!s.toString().equals("")) {
        s.append("\n");
      }
      s.append(map.get(i));
    }
    printToFile(s.toString(), filename);
  }

  // Prints targets in the fashion accepted by the Erdos et al. implementation of BrandesSubset and Brandes++
  private static void printTargets(TIntSet targets, String filename) {
    StringBuilder s = new StringBuilder();
    TIntIterator it = targets.iterator();
    while (it.hasNext()) {
      int i = it.next();
      s.append(i);
      if (it.hasNext()) {
        s.append("\t");
      }
    }
    printToFile(s.toString(), filename);
  }
}
