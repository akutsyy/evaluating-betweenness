package algorithms.bader;

import static algorithms.sssp.BFS.bfsPredecessor;
import static algorithms.sssp.Djikstra.djikstraPredecessor;
import static utility.Printing.printProgress;

import algorithms.brandes.BrandesCalculator;
import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import java.util.Random;
import utility.Statistics;

public class BaderCalculator {


  //Using binary heap since I tested it to be much faster than fibonacci or rank pairing heap in practice
  public static Statistics Bader(ArrayGraph g, int chosenNode, double alpha) {
    int howOftenToPrint = Math.max(1, g.size() / 100);

    double[] centrality = new double[g.size()]; // betweeness result

    if (!g.isDirected()) {
      alpha = alpha * 2; // Account for dividing centralities by two at end
    }

    //Main loop
    TIntArrayList stack = new TIntArrayList(g.size());

    int numIterations = 0;
    Random r = new Random();
    while (centrality[chosenNode] < alpha * g.size() && numIterations < g.size() / 20) {
      int s = r.nextInt(g.size());

      numIterations++;
      printProgress(numIterations, howOftenToPrint);

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

      BrandesCalculator.accumulateDependencies(s, stack, pred, sigma, delta, centrality);

    }

    // Extrapolate
    int finalNumIterations = numIterations;
    centrality = Arrays.stream(centrality).map(v -> v * 2 * g.size() / finalNumIterations).toArray();
    // Halve for undirected graphs
    if (!g.isDirected()) {
      centrality = Arrays.stream(centrality).map(v -> v / 2).toArray();
    }
    return new Statistics(centrality, numIterations, numIterations >= g.size() / 20);
  }

}
