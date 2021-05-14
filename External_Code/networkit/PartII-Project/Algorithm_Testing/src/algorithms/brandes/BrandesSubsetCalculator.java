package algorithms.brandes;

import static algorithms.sssp.BFS.bfsPredecessor;
import static algorithms.sssp.Djikstra.djikstraPredecessor;
import static utility.Printing.printProgress;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Arrays;
import utility.ArrayGenerator;
import utility.Statistics;

public class BrandesSubsetCalculator {

  public static Statistics BrandesSubset(ArrayGraph g, TIntSet targets) {
    int howOftenToPrint = Math.max(1, g.size() / 100);

    double[] centrality = new double[g.size()]; // betweeness result

    //Main loop
    TIntArrayList stack = new TIntArrayList(g.size());

    int node_num = 0;
    TIntIterator sourceIt = targets.iterator();

    while (sourceIt.hasNext()) {
      int s = sourceIt.next();
      node_num++;
      printProgress(node_num, howOftenToPrint);

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
      accumulateDependenciesFromTargets(g, s, targets, stack, pred, sigma, delta, centrality);
    }

    // Halve for undirected graphs
    if (!g.isDirected()) {
      centrality = Arrays.stream(centrality).map(v -> v / 2).toArray();
    }
    return new Statistics(centrality);
  }

  public static TIntSet generateSources(ArrayGraph g, double portion) {
    int howMany = (int) (g.size() * portion);
    int[] allShuffled = ArrayGenerator.shuffledInts(g.size());
    int[] firstN = new int[howMany];
    System.arraycopy(allShuffled, 0, firstN, 0, howMany);
    return new TIntHashSet(firstN);
  }

  public static void accumulateDependenciesFromTargets(ArrayGraph g, int s, TIntSet targets, TIntArrayList stack,
      TIntArrayList[] pred, long[] sigma, double[] delta, double[] centrality) {

    // Missing delta = 0
    while (!stack.isEmpty()) {
      int w = stack.removeAt(stack.size() - 1);
      if (pred[w] != null) {
        TIntIterator it = pred[w].iterator();
        while (it.hasNext()) {
          int v = it.next();
          if (sigma[w] != 0) {
            int ind = targets.contains(w) ? 1 : 0;
            delta[v] +=
                ((double) sigma[v] / sigma[w]) * (ind + delta[w]);
          }
        }
      }
      if (w != s) {
        centrality[w] += delta[w];
      }
    }
  }
}
