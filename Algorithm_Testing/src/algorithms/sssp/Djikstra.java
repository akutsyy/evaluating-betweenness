package algorithms.sssp;

import framework.heaps.MinPriorityHeap;
import framework.heaps.binary.MinBinaryHeap;
import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import java.util.Arrays;
import org.jgrapht.alg.util.Pair;
import utility.MathUtils;

public class Djikstra {

  public static void djikstraPredecessor(ArrayGraph g, int s, TIntArrayList stack, double[] dist,
      TIntArrayList[] pred, long[] sigma) {
    // Single-source shortest-paths problem
    MinPriorityHeap queue = new MinBinaryHeap(g.size(), dist);

    //Initialization
    Arrays.fill(dist, Double.POSITIVE_INFINITY);
    dist[s] = 0.0;
    sigma[s] = 1;
    queue.insert(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.pop();
      stack.add(v);

      // foreach vertex w such that (v,w) in E
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];

          double newDist = dist[v] + g.weights[j];

          // New path found
          if (newDist < dist[w] - MathUtils.EPSILON) {
            dist[w] = newDist;
            queue.insertOrDecrease(w);
            pred[w] = null;
            sigma[w] = 0;
          }

          // Path counting - shortest path to w via v?
          if (Math.abs(dist[w] - newDist) < MathUtils.EPSILON) {
            //set σ(s,w) to σ(s,w)+σ(s,v)
            sigma[w] += sigma[v];
            if (pred[w] == null) {
              pred[w] = new TIntArrayList();
            }
            pred[w].add(v);
          }
        }
      }
    }
  }

  public static void djikstraSuccessor(ArrayGraph g, int s, double[] dist, TIntArrayList[] succ, long[] sigma) {
    // Single-source shortest-paths problem
    MinPriorityHeap queue = new MinBinaryHeap(g.size(), dist);

    TIntArrayList[] pred = new TIntArrayList[g.size()];

    //Initialization
    Arrays.fill(dist, Double.POSITIVE_INFINITY);
    dist[s] = 0.0;
    sigma[s] = 1;
    queue.insert(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.pop();

      // foreach vertex w such that (v,w) in E
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];

          double newDist = dist[v] + g.weights[j];

          // New path found
          if (newDist < dist[w] - MathUtils.EPSILON) {
            dist[w] = newDist;
            queue.insertOrDecrease(w);
            succ[w] = null;
            // Remove w from all predecessors
            if (pred[w] != null) {
              for (int i = 0; i < pred[w].size(); i++) {
                succ[pred[w].get(i)].remove(w);
              }
            }
            pred[w] = null;
            sigma[w] = 0;
          }

          // Path counting - shortest path to w via v?
          if (Math.abs(dist[w] - newDist) < MathUtils.EPSILON) {
            //set σ(s,w) to σ(s,w)+σ(s,v)
            sigma[w] += sigma[v];
            if (succ[v] == null) {
              succ[v] = new TIntArrayList();
            }
            if (pred[w] == null) {
              pred[w] = new TIntArrayList();
            }
            succ[v].add(w);
            pred[w].add(v);
          }
        }
      }
    }

  }

  public static double djikstraMaxTwoDistsSum(ArrayGraph g, int s, TIntSet set) {
    // Single-source shortest-paths problem
    double[] dist = new double[g.size()];
    MinPriorityHeap queue = new MinBinaryHeap(g.size(), dist);

    double[] maxDists = new double[2];
    double[] maxDistNodes = new double[2];

    //Initialization
    Arrays.fill(dist, Double.POSITIVE_INFINITY);
    Arrays.fill(maxDistNodes, -1);
    dist[s] = 0.0;
    queue.insert(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.pop();
      if (v != maxDistNodes[0] && dist[v] > maxDists[0]) {
        maxDists[0] = dist[v];
        maxDistNodes[0] = v;
      } else if (v != maxDistNodes[1] && dist[v] > maxDists[1]) {
        maxDists[1] = dist[v];
        maxDistNodes[1] = v;
      }
      // foreach vertex w such that (v,w) in E
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (set.contains(w)) {
            double newDist = dist[v] + g.weights[j];

            // New path found
            if (newDist < dist[w]) {
              dist[w] = newDist;
              queue.insertOrDecrease(w);
            }
          }
        }
      }
    }
    if (maxDistNodes[0] != -1 && maxDistNodes[1] != -1) {
      return maxDists[0] + maxDists[1];
    }
    return -1;
  }

  public static Pair<Integer, Double> djikstraFurthestNode(ArrayGraph g, int s, TIntSet set, int toAvoid) {
    // Single-source shortest-paths problem
    double[] dist = new double[g.size()];
    MinPriorityHeap queue = new MinBinaryHeap(g.size(), dist);

    double maxDist = 0;
    int maxNode = -1;

    //Initialization
    Arrays.fill(dist, Double.POSITIVE_INFINITY);
    dist[s] = 0.0;
    queue.insert(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.pop();
      if (v != toAvoid && dist[v] > maxDist) {
        maxDist = dist[v];
        maxNode = v;
      }
      // foreach vertex w such that (v,w) in E
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (set.contains(w)) {
            double newDist = dist[v] + g.weights[j];

            // New path found
            if (newDist < dist[w]) {
              dist[w] = newDist;
              queue.insertOrDecrease(w);
            }
          }
        }
      }
    }
    return new Pair<>(maxNode, maxDist);
  }
}
