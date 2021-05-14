package algorithms.geisberger;

import static algorithms.sssp.BFS.bfsPredecessor;
import static algorithms.sssp.BFS.bfsSuccessor;
import static algorithms.sssp.Djikstra.djikstraPredecessor;
import static algorithms.sssp.Djikstra.djikstraSuccessor;
import static utility.Printing.printProgress;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import java.util.Arrays;
import java.util.Random;
import utility.RandomHelper;
import utility.Statistics;

public class GeisbergerCalculator {

  public static Statistics GeisbergerLinear(ArrayGraph g, int numIterations) {
    int howOftenToPrint = Math.max(1, numIterations / 100);

    double[] centrality = new double[g.size()]; // betweeness result

    //Main loop
    TIntArrayList stack = new TIntArrayList(g.size());
    Random r = new Random();
    ArrayGraph inverse = ArrayGraph.getInverseGraph(g);

    int node_num = 0;
    for (int i = 0; i < numIterations; i++) {
      int s = r.nextInt(g.size());
      boolean isForwardSearch = r.nextBoolean();
      node_num++;
      printProgress(node_num, howOftenToPrint);

      // Initialize search
      TIntArrayList[] pred = new TIntArrayList[g.size()]; // predecessors on shortest path from source
      long[] sigma = new long[g.size()]; // # shortest paths from s to t
      double[] delta = new double[g.size()]; // dependency of source on v
      double[] weightedDist = new double[g.size()];
      int[] unweightedDist = new int[g.size()];

      if (isForwardSearch) {
        if (g.isWeighted()) {
          djikstraPredecessor(g, s, stack, weightedDist, pred, sigma);
        } else {
          bfsPredecessor(g, s, stack, unweightedDist, pred, sigma);
        }
      } else {
        if (g.isWeighted()) {
          djikstraPredecessor(inverse, s, stack, weightedDist, pred, sigma);
        } else {
          bfsPredecessor(inverse, s, stack, unweightedDist, pred, sigma);
        }
      }

      // Missing delta = 0
      while (!stack.isEmpty()) {
        int w = stack.removeAt(stack.size() - 1);
        if (pred[w] != null) {
          TIntIterator it = pred[w].iterator();
          while (it.hasNext()) {
            int v = it.next();
            if (sigma[w] != 0) {
              double distW;
              if (g.isWeighted()) {
                distW = weightedDist[w];
              } else {
                distW = unweightedDist[w];
              }
              delta[v] +=
                  ((double) sigma[v] / sigma[w]) * (1 / distW + delta[w]);
            }
          }
        }
        double distW;
        if (g.isWeighted()) {
          distW = weightedDist[w];
        } else {
          distW = unweightedDist[w];
        }
        if (w != s) {
          centrality[w] += delta[w] * distW;
        }
      }
    }
    // Extrapolate
    centrality = Arrays.stream(centrality).map(v -> v * 2 * g.size() / numIterations).toArray();
    // Halve for undirected graphs
    if (!g.isDirected()) {
      centrality = Arrays.stream(centrality).map(v -> v / 2).toArray();
    }
    return new Statistics(centrality);
  }

  //Using binary heap since it is much faster than fibonacci heap in practice
  public static Statistics GeisbergerBisection(ArrayGraph g, int numIterations) {

    int howOftenToPrint = Math.max(1, numIterations / 100);
    ArrayGraph inverse = ArrayGraph.getInverseGraph(g);

    double[] centrality = new double[g.size()]; // betweeness result

    //Main loop
    Random r = new Random();

    int node_num = 0;
    for (int i = 0; i < numIterations; i++) {
      int s = r.nextInt(g.size());
      boolean isForwardSearch = r.nextBoolean();

      node_num++;
      printProgress(node_num, howOftenToPrint);

      // Initialize search
      TIntArrayList[] succ = new TIntArrayList[g.size()]; // successors on shortest path from source
      long[] sigma = new long[g.size()]; // # shortest paths from s to t
      double[] delta = new double[g.size()]; // dependency of source on v, default 0
      double[] weightedDist = null;

      if (g.isWeighted()) {
        weightedDist = new double[g.size()];
        if (isForwardSearch) {
          djikstraSuccessor(g, s, weightedDist, succ, sigma);
        } else {
          djikstraSuccessor(inverse, s, weightedDist, succ, sigma);
        }

      } else {
        int[] dist = new int[g.size()];
        if (isForwardSearch) {
          bfsSuccessor(g, s, dist, succ, sigma);
        } else {
          bfsSuccessor(inverse, s, dist, succ, sigma);
        }
      }

      //Accumulation
      TIntArrayList dfsStack = new TIntArrayList();// Also acts as path from root
      int[] numExploredChildren = new int[g.size()];
      boolean[] completed = new boolean[g.size()];
      dfsStack.add(s);
      while (!dfsStack.isEmpty()) {
        int v = peek(dfsStack);

        if (succ[v] == null) { // No children, so delta of 0, FRESH
          decrementHalf(dfsStack, delta, weightedDist, isForwardSearch, sigma);
          pop(dfsStack);
        }

        // Still have unexplored children
        else if (numExploredChildren[v] < succ[v].size()) {
          if (numExploredChildren[v] == 0) { //FRESH
            decrementHalf(dfsStack, delta, weightedDist, isForwardSearch, sigma);
          }
          dfsStack.add(succ[v].get(numExploredChildren[v]));
          numExploredChildren[v]++;
        }
        // All children are complete, OLD (on way down dfs)
        else {
          numExploredChildren[v] = 0;
          pop(dfsStack);
          if (!completed[v]) {
            for (int x = 0; x < succ[v].size(); x++) {
              int w = succ[v].get(x);
              // delta defaults to 0
              delta[v] += (delta[w] + 1.0) * sigma[v] / sigma[w];
            }
            completed[v] = true;
          }
        }
      }

      for (int v = 0; v < g.size(); v++) {
        if (v != s && completed[v]) {
          centrality[v] += delta[v];
        }
      }
    }

    // Extrapolate
    centrality = Arrays.stream(centrality).map(v -> v * 2 * g.size() / numIterations).toArray();
    // Halve for undirected graphs
    if (!g.isDirected()) {
      centrality = Arrays.stream(centrality).map(v -> v / 2).toArray();
    }

    return new Statistics(centrality);
  }

  //Using binary heap since it is much faster than fibonacci heap in practice
  public static Statistics GeisbergerBisectionSampling(ArrayGraph g, int numIterations, int numSamples) {

    int howOftenToPrint = Math.max(1, numIterations / 100);
    ArrayGraph inverse = ArrayGraph.getInverseGraph(g);

    double[] centrality = new double[g.size()]; // betweeness result

    //Main loop
    Random r = new Random();

    int node_num = 0;
    for (int i = 0; i < numIterations; i++) {
      int s = r.nextInt(g.size());
      boolean isForwardSearch = r.nextBoolean();

      node_num++;
      printProgress(node_num, howOftenToPrint);

      // Initialize search
      TIntArrayList[] pred = new TIntArrayList[g.size()]; // predecessors on shortest path from source
      long[] sigma = new long[g.size()]; // # shortest paths from s to t
      double[] weightedDist = null;

      if (g.isWeighted()) {
        weightedDist = new double[g.size()];
        if (isForwardSearch) {
          djikstraPredecessor(g, s, new TIntArrayList(), weightedDist, pred, sigma);
        } else {
          djikstraPredecessor(inverse, s, new TIntArrayList(), weightedDist, pred, sigma);
        }

      } else {
        int[] dist = new int[g.size()];
        if (isForwardSearch) {
          bfsPredecessor(g, s, new TIntArrayList(), dist, pred, sigma);
        } else {
          bfsPredecessor(inverse, s, new TIntArrayList(), dist, pred, sigma);
        }
      }

      // Run canonical betweenness algorithm for each sample
      for (int j = 0; j < numSamples; j++) {
        double[] delta = new double[g.size()]; // dependency of source on v, default 0
        TIntArrayList[] newSucc = sampleSSSPDAG(r, pred, sigma);
        //Accumulation
        TIntArrayList dfsStack = new TIntArrayList();// Also acts as path from root
        int[] numExploredChildren = new int[g.size()];
        boolean[] completed = new boolean[g.size()];
        dfsStack.add(s);
        while (!dfsStack.isEmpty()) {
          int v = peek(dfsStack);

          if (newSucc[v] == null) { // No children, so delta of 0, FRESH
            decrementHalfCanonical(dfsStack, delta, weightedDist, isForwardSearch);
            pop(dfsStack);
          }

          // Still have unexplored children
          else if (numExploredChildren[v] < newSucc[v].size()) {
            if (numExploredChildren[v] == 0) { //FRESH
              decrementHalfCanonical(dfsStack, delta, weightedDist, isForwardSearch);
            }
            dfsStack.add(newSucc[v].get(numExploredChildren[v]));
            numExploredChildren[v]++;
          }
          // All children are complete, OLD (on way down dfs)
          else {
            numExploredChildren[v] = 0;
            pop(dfsStack);
            if (!completed[v]) {
              for (int x = 0; x < newSucc[v].size(); x++) {
                int w = newSucc[v].get(x);
                // delta defaults to 0
                delta[v] += delta[w] + 1.0;
              }
              completed[v] = true;
            }
          }
        }

        for (int v = 0; v < g.size(); v++) {
          if (v != s && completed[v]) {
            centrality[v] += delta[v] / numSamples;
          }
        }
      }
    }

    // Extrapolate
    centrality = Arrays.stream(centrality).map(v -> v * 2 * g.size() / numIterations).toArray();
    // Halve for undirected graphs
    if (!g.isDirected()) {
      centrality = Arrays.stream(centrality).map(v -> v / 2).toArray();
    }
    return new Statistics(centrality);
  }

  ////////////////////////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////// HELPER FUNCTIONS ////////////////////////////////////////////
  ///////////////////////////////////////////////////////////////////////////////////////////////

  private static TIntArrayList[] sampleSSSPDAG(Random r, TIntArrayList[] pred, long[] sigma) {
    TIntArrayList[] newSucc = new TIntArrayList[pred.length];
    for (int i = 0; i < pred.length; i++) {
      if (pred[i] != null) {
        // Select a parent with probability sigma(p)/sigma(child)
        double[] probabilities = new double[pred[i].size()];
        for (int j = 0; j < pred[i].size(); j++) {
          probabilities[j] = sigma[pred[i].get(j)] / (double) sigma[i];
        }
        int p = RandomHelper.selectFromWeightedList(r, pred[i], probabilities);
        if (newSucc[p] == null) {
          newSucc[p] = new TIntArrayList();
        }
        newSucc[p].add(i);
      }
    }
    return newSucc;
  }

  private static int pop(TIntArrayList stack) {
    return stack.removeAt(stack.size() - 1);
  }

  private static int peek(TIntArrayList stack) {
    return stack.get(stack.size() - 1);
  }

  private static void decrementHalf(TIntArrayList currPath, double[] delta, double[] weightedDist,
      boolean isForwardSearch, long[] sigma) {
    int w = currPath.get(currPath.size() - 1);
    if (weightedDist == null) {
      int v;
      if (isForwardSearch) {
        if (currPath.size() == 1) {
          v = currPath.get(0);
        } else {
          v = currPath.get(((currPath.size() - 2) / 2));
        }
      } else {
        v = currPath.get(((currPath.size() - 1) / 2));
      }
      delta[v] -= 1.0 / sigma[w];
    } else {
      double toFind = weightedDist[currPath.get(currPath.size() - 1)] / 2;
      if (isForwardSearch) {
        delta[binarySearchJustLessThan(currPath, weightedDist, toFind)] -= 1.0 / sigma[w];
      } else {
        delta[binarySearchJustMoreThan(currPath, weightedDist, toFind)] -= 1.0 / sigma[w];
      }
    }
  }

  private static void decrementHalfCanonical(TIntArrayList currPath, double[] delta, double[] weightedDist, boolean isForwardSearch) {
    if (weightedDist == null) {
      int v;
      if (isForwardSearch) {
        if (currPath.size() == 1) {
          v = currPath.get(0);
        } else {
          v = currPath.get(((currPath.size() - 2) / 2));
        }
      } else {
        v = currPath.get(((currPath.size() - 1) / 2));
      }
      delta[v] -= 1.0;
    } else {
      double toFind = weightedDist[currPath.get(currPath.size() - 1)] / 2;
      if (isForwardSearch) {
        delta[binarySearchJustLessThan(currPath, weightedDist, toFind)] -= 1.0;
      } else {
        delta[binarySearchJustMoreThan(currPath, weightedDist, toFind)] -= 1.0;
      }
    }
  }

  private static int binarySearchJustMoreThan(TIntArrayList array, double[] weights,
      double toFind) {
    // Looking for least value which is greater than or equal to toFind
    int l = 0;
    int r = array.size() - 1;
    while (l < r) {
      int mid = l + (r - 1) / 2;
      if (weights[array.get(mid)] < toFind) {
        l = mid - 1;
      } else {
        r = mid + 1;
      }
    }
    return array.get(l);
  }

  private static int binarySearchJustLessThan(TIntArrayList array, double[] weights,
      double toFind) {
    // Looking for greatest value which is less than or equal to toFind
    int l = 0;
    int r = array.size() - 1;
    while (l < r) {
      int mid = l + (r - 1) / 2;
      if (weights[array.get(mid)] > toFind) {
        r = mid - 1;
      } else {
        l = mid + 1;
      }
    }
    return array.get(l);
  }
}
