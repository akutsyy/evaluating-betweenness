package testing;

import algorithms.brandespp.metis.Metis;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import framework.parsing.Harness;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PartitionTester {

  public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {
    HashMap<String, String> options = Harness.parseOpts(args);
    testMetisToPartitions(args);
  }


  public static void testMetisToPartitions(String[] args) throws IOException, FileTypeException {
    HashMap<String, String> options = Harness.parseOpts(args);
    ArrayGraph g = new ArrayGraph(Harness.getFile(options), Harness.getFileType(options));
    g.invertWeights(); // METIS interprets weights as closeness, Brandes interprets them as distance
    ArrayList<TIntSet> graphs = Metis.metisToPartitions(g, 16, 5, 5, 100);
    g.invertWeights();
    validPartition(g, graphs);
  }

  public static boolean graphsAreIsomorphicWithSameGlobal(ArrayGraph g, ArrayGraph graph) {
    for (int i = 0; i < g.size(); i++) {
      if (g.empty(i)) {
        if (!graph.empty(graph.toLocalName(g.toGlobalName(i)))) {
          System.out.println(i + " --> " + g.toGlobalName(i) + " --> " + graph.toLocalName(g.toGlobalName(i)) + " different degree");
          return false;
        }
      } else {
        for (int j = g.start(i); j <= g.end(i); j++) {
          int w = g.adjacency[j];
          if (!graph.containsEdge(graph.toGlobalName(g.toGlobalName(i)), graph.toGlobalName(g.toGlobalName(w)))) {
            System.out.println(i + " --> " + g.toGlobalName(i) + " --> " + graph.toLocalName(g.toGlobalName(i)) + " missing edge");
            return false;
          }
        }
      }
    }
    return true;
  }

  public static void validRenaming(ArrayGraph g, ArrayGraph graph) {
    for (int i = 0; i < graph.size(); i++) {
      if (graph.toLocalName(graph.toGlobalName(i)) != i) {
        System.out.println(
            i + "-->" + graph.toGlobalName(i) + "-->" + graph.toLocalName(graph.toGlobalName(i)));
      }
      if (!graph.empty(i)) {
        for (int j = graph.start(i); j <= graph.end(i); j++) {
          if (!g.containsEdge(graph.toGlobalName(i), graph.toGlobalName(graph.adjacency[j]))) {
            System.out.println(
                "missing: " + i + " (" + graph.toGlobalName(i) + ") --> " + graph.adjacency[j]
                    + " (" + graph.toGlobalName(graph.adjacency[j]) + ")");
          }
          if (graph.toLocalName(graph.toGlobalName(graph.adjacency[j])) != graph.adjacency[j]) {
            System.out.println(
                graph.adjacency[j] + "-->" + graph.toGlobalName(graph.adjacency[j]) + "-->" + graph
                    .toLocalName(graph.toGlobalName(graph.adjacency[j])));
          }
        }
      }
    }
  }

  public static void validRenaming(ArrayGraph g, ArrayList<ArrayGraph> graphs) {
    for (ArrayGraph graph : graphs) {
      for (int i = 0; i < graph.size(); i++) {
        if (graph.toLocalName(graph.toGlobalName(i)) != i) {
          System.out.println(
              i + "-->" + graph.toGlobalName(i) + "-->" + graph.toLocalName(graph.toGlobalName(i)));
        }
        if (!graph.empty(i)) {
          for (int j = graph.start(i); j <= graph.end(i); j++) {
            if (!g.containsEdge(graph.toGlobalName(i), graph.toGlobalName(graph.adjacency[j]))) {
              System.out.println(
                  "missing: " + i + " (" + graph.toGlobalName(i) + ") --> " + graph.adjacency[j]
                      + " (" + graph.toGlobalName(graph.adjacency[j]) + ")");
            }
            if (graph.toLocalName(graph.toGlobalName(graph.adjacency[j])) != graph.adjacency[j]) {
              System.out.println(
                  graph.adjacency[j] + "-->" + graph.toGlobalName(graph.adjacency[j]) + "-->"
                      + graph.toLocalName(graph.toGlobalName(graph.adjacency[j])));
            }
          }
        }
      }
    }
  }

  public static void validGraphs(ArrayGraph g, ArrayList<ArrayGraph> graphs) {
    int totalSize = 0;
    TIntSet seenNodes = new TIntHashSet(g.size());
    for (ArrayGraph g1 : graphs) {
      totalSize += g1.size();
      for (int i : g1.getToGlobalNames()) {
        if (seenNodes.contains(i)) {
          System.out.println("error: repeating " + i);
        }
        seenNodes.add(i);
      }
    }
    for (int i : g.getNodes()) {
      if (!seenNodes.contains(i)) {
        System.out.println("failed to find node " + i);
      }
    }
    System.out.println("total size of " + totalSize + "/" + g.size());
  }

  public static void validPartition(ArrayGraph g, ArrayList<TIntSet> graphs) {
    int totalSize = 0;
    TIntSet seenNodes = new TIntHashSet(g.size());
    for (TIntSet g1 : graphs) {
      totalSize += g1.size();
      TIntIterator it = g1.iterator();
      while (it.hasNext()) {
        int i = it.next();
        if (seenNodes.contains(i)) {
          System.out.println("error: repeating " + i);
        }
        seenNodes.add(i);
      }
    }
    for (int i : g.getNodes()) {
      if (!seenNodes.contains(i)) {
        System.out.println("failed to find node " + i);
      }
    }
    System.out.println("total size of " + totalSize + "/" + g.size());
  }

  public static double calculateEdgeCut(ArrayGraph g, ArrayList<TIntSet> partitions) {
    double cut = 0;
    TIntIntMap assignments = new TIntIntHashMap();
    for (int i = 0; i < partitions.size(); i++) {
      TIntIterator it = partitions.get(i).iterator();
      while (it.hasNext()) {
        int j = it.next();
        assignments.put(j, i);
      }
    }
    for (int i : g.getNodes()) {
      if (!g.empty(i)) {
        for (int j = g.start(i); j <= g.end(i); j++) {
          if (assignments.get(i) != assignments.get(g.adjacency[j])) {
            cut += g.weights[j];
          }
        }
      }
    }
    return cut;
  }


}
