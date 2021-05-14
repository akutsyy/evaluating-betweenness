package algorithms.brandespich2008;

import static algorithms.sssp.BFS.bfsPredecessor;
import static utility.Printing.printProgress;

import algorithms.brandes.BrandesCalculator;
import algorithms.sssp.Djikstra;
import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import java.util.Random;
import utility.Statistics;

public class BrandesPich2008 {

  //Using binary heap since I tested it to be much faster than fibonacci or rank pairing heap in practice
  public static Statistics BrandesRandom(ArrayGraph g, int numIterations) {
    int howOftenToPrint = Math.max(1, numIterations / 100);

    double[] centrality = new double[g.size()]; // betweeness result

    //Main loop
    TIntArrayList stack = new TIntArrayList(g.size());
    Random r = new Random();

    int node_num = 0;
    for (int i = 0; i < numIterations; i++) {
      int s = r.nextInt(g.size());
      node_num++;
      printProgress(node_num, howOftenToPrint);

      // Initialize search
      TIntArrayList[] pred = new TIntArrayList[g.size()]; // predecessors on shortest path from source
      long[] sigma = new long[g.size()]; // # shortest paths from s to t
      double[] delta = new double[g.size()]; // dependency of source on v
      double[] weightedDist = new double[g.size()];
      int[] unweightedDist = new int[g.size()];

      if (g.isWeighted()) {
        Djikstra.djikstraPredecessor(g, s, stack, weightedDist, pred, sigma);
      } else {
        bfsPredecessor(g, s, stack, unweightedDist, pred, sigma);
      }

      BrandesCalculator.accumulateDependencies(s, stack, pred, sigma, delta, centrality);

    }

    // Extrapolate
    centrality = Arrays.stream(centrality).map(v -> v * g.size() / numIterations).toArray();

    // Halve for undirected graphs
    if (!g.isDirected()) {
      centrality = Arrays.stream(centrality).map(v -> v / 2).toArray();
    }
    return new Statistics(centrality);
  }
}
