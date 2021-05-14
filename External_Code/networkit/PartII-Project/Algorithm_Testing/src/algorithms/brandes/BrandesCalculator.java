package algorithms.brandes;

import static algorithms.sssp.BFS.bfsPredecessor;
import static algorithms.sssp.Djikstra.djikstraPredecessor;
import static utility.Printing.printProgress;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import utility.Statistics;

public class BrandesCalculator {

  public static Statistics Brandes(ArrayGraph g) {
    return Brandes(g, false);
  }

  //Using binary heap since I tested it to be much faster than fibonacci or rank pairing heap in practice
  public static Statistics Brandes(ArrayGraph g, boolean computeTimePerNode) {
    int howOftenToPrint = Math.max(1, g.size() / 100);

    double[] centrality = new double[g.size()]; // betweeness result
    long[] time = null; // time per node
    if (computeTimePerNode) {
      time = new long[g.size()];
    }

    //Main loop
    TIntArrayList stack = new TIntArrayList(g.size());

    int node_num = 0;
    for (int s : g.getNodes()) {
      node_num++;
      printProgress(node_num, howOftenToPrint);

      long start = 0;
      if (computeTimePerNode) {
        start = System.nanoTime();
      }
      // Initialize search
      TIntArrayList[] pred = new TIntArrayList[g.size()]; // predecessors on shortest path from source
      long[] sigma = new long[g.size()]; // # shortest paths from s to t
      double[] delta = new double[g.size()]; // dependency of source on v

      if (g.isWeighted()) {
        double[] dist = new double[g.size()];
        djikstraPredecessor(g, s, stack, dist, pred, sigma);
      } else {
        int[] dist = new int[g.size()];
        bfsPredecessor(g, s, stack, dist, pred, sigma);
      }

      accumulateDependencies(s, stack, pred, sigma, delta, centrality);

      if (computeTimePerNode) {
        long end = System.nanoTime();
        time[s] = end - start;
      }
    }
    // Halve for undirected graphs
    if (!g.isDirected()) {
      centrality = Arrays.stream(centrality).map(v -> v / 2).toArray();
    }
    return new Statistics(centrality, time);
  }


  public static void accumulateDependencies(int s, TIntArrayList stack,
      TIntArrayList[] pred, long[] sigma, double[] delta, double[] centrality) {

    // Missing delta = 0
    while (!stack.isEmpty()) {
      int w = stack.removeAt(stack.size() - 1);
      if (pred[w] != null) {
        TIntIterator it = pred[w].iterator();
        while (it.hasNext()) {
          int v = it.next();
          if (sigma[w] != 0) {
            delta[v] +=
                ((double) sigma[v] / sigma[w]) * (1 + delta[w]);
          }
        }
      }
      if (w != s) {
        centrality[w] += delta[w];
      }
    }
  }

}
