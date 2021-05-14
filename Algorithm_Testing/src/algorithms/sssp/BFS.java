package algorithms.sssp;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import java.util.Arrays;
import org.jgrapht.alg.util.Pair;

public class BFS {

  public static void bfsPredecessor(ArrayGraph g, int s, TIntArrayList stack, int[] dist,
      TIntArrayList[] pred, long[] sigma) {
    // Single-source shortest-paths problem
    TIntLinkedList queue = new TIntLinkedList();

    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[s] = 0;
    sigma[s] = 1;
    queue.add(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.removeAt(0);
      stack.add(v);

      // foreach vertex w such that (v,w) in E
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];

          // Path discovery - w found for the first time?
          if (dist[w] > dist[v] + 1) {
            dist[w] = dist[v] + 1;
            queue.add(w);
          }

          // Path counting - shortest path to w via v?
          if (dist[w] == dist[v] + 1) {
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


  public static void bfsSuccessor(ArrayGraph g, int s, int[] dist, TIntArrayList[] succ, long[] sigma) {
    // Single-source shortest-paths problem
    TIntLinkedList queue = new TIntLinkedList();

    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[s] = 0;
    sigma[s] = 1;
    queue.add(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.removeAt(0);

      // foreach vertex w such that (v,w) in E
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];

          // Path discovery - w found for the first time?
          if (dist[w] > dist[v] + 1) {
            dist[w] = dist[v] + 1;
            queue.add(w);
          }

          // Path counting - shortest path to w via v?
          if (dist[w] == dist[v] + 1) {
            //set σ(s,w) to σ(s,w)+σ(s,v)
            sigma[w] += sigma[v];
            if (succ[v] == null) {
              succ[v] = new TIntArrayList();
            }
            succ[v].add(w);
          }
        }
      }
    }
  }

  public static int bfsMaxTwoDistsSum(ArrayGraph g, int s, TIntSet set) {
    // Single-source shortest-paths problem
    int[] dist = new int[g.size()];
    int[] maxDists = new int[2];
    int[] maxDistNodes = new int[2];

    TIntLinkedList queue = new TIntLinkedList();

    Arrays.fill(dist, Integer.MAX_VALUE);
    Arrays.fill(maxDistNodes, -1);
    dist[s] = 0;
    queue.add(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.removeAt(0);
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

            // Path discovery - w found for the first time?
            if (dist[w] > dist[v] + 1) {
              dist[w] = dist[v] + 1;
              queue.add(w);
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

  public static Pair<Integer, Integer> bfsFurthestNode(ArrayGraph g, int s, TIntSet set, int toAvoid) {
    // Single-source shortest-paths problem
    int[] dist = new int[g.size()];
    int maxDist = 0;
    int maxNode = -1;

    TIntLinkedList queue = new TIntLinkedList();

    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[s] = 0;
    queue.add(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.removeAt(0);
      if (v != toAvoid && dist[v] > maxDist) {
        maxDist = dist[v];
        maxNode = v;
      }
      // foreach vertex w such that (v,w) in E
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (set.contains(w)) {

            // Path discovery - w found for the first time?
            if (dist[w] > dist[v] + 1) {
              dist[w] = dist[v] + 1;
              queue.add(w);
            }
          }
        }
      }
    }
    return new Pair<>(maxNode, maxDist);
  }
}
