package algorithms.sssp;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Random;
import utility.RandomHelper;

//Balanced Bidirectional BFS
public class BBBFS {

  public static TIntSet findRandomShortestPath(ArrayGraph g, ArrayGraph inverse, int s, int t) {

    if (s == t) {
      return new TIntHashSet();
    }
    // Single-source shortest-paths problem

    TIntSet[] s_pred = new TIntHashSet[g.size()];
    TIntSet[] t_pred = new TIntHashSet[g.size()];
    long[] s_sigma = new long[g.size()];
    long[] t_sigma = new long[g.size()];

    TIntSet midpoints = find_midpoints(g, inverse, s, t, s_pred, t_pred, s_sigma, t_sigma);
    if (midpoints.isEmpty()) {
      return midpoints;
    }
    return backtrack_path(s, t, s_pred, t_pred, s_sigma, t_sigma, midpoints);
  }


  private static TIntSet simple_search(ArrayGraph g, ArrayGraph inverse, int s, int t) {
    // Single-source shortest-paths problem
    TIntLinkedList queue = new TIntLinkedList();
    boolean[] visited = new boolean[g.size()];
    TIntHashSet[] pred = new TIntHashSet[g.size()];
    queue.add(s);
    while (!queue.isEmpty()) {
      int v = queue.removeAt(0);
      if (v == t) {
        return simple_backtrack(pred, s, t);
      }
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (!visited[w]) {
            visited[w] = true;
            if (pred[w] == null) {
              pred[w] = new TIntHashSet();
            }
            pred[w].add(v);
          }
          queue.add(w);
        }
      }
    }
    return new TIntHashSet();
  }

  private static TIntSet simple_backtrack(TIntHashSet[] pred, int s, int t) {
    int currNode = pred[t].iterator().next();
    TIntHashSet path = new TIntHashSet();

    while (currNode != s) {
      path.add(currNode);
      currNode = pred[currNode].iterator().next();
    }
    return path;
  }

  // Assume s=/=t
  private static TIntSet find_midpoints(ArrayGraph g, ArrayGraph inverse, int s, int t, TIntSet[] s_pred, TIntSet[] t_pred, long[] s_sigma, long[] t_sigma) {
    final short UNVISITED = 0;
    final short S_VISITED = 1;
    final short T_VISITED = 2;

    TIntSet midpoints = new TIntHashSet();

    TIntLinkedList currQueue;
    ArrayGraph currGraph;
    TIntSet[] currPred;
    long[] currSigma;

    TIntLinkedList s_queue = new TIntLinkedList(100);
    TIntLinkedList t_queue = new TIntLinkedList(100);

    s_queue.add(s);
    t_queue.add(t);

    int s_degree = 0;
    int t_degree = 0;

    short[] visited = new short[g.size()];
    visited[s] = S_VISITED;
    visited[t] = T_VISITED;

    s_sigma[s] = 1;
    t_sigma[t] = 1;

    int[] dist = new int[g.size()];

    boolean haveToStop = false;
    while (!haveToStop) {
      boolean doing_s = false;
      if (s_degree <= t_degree) {
        currQueue = s_queue;
        s_degree = 0;
        currGraph = g;
        currPred = s_pred;
        currSigma = s_sigma;
        doing_s = true;
      } else {
        currQueue = t_queue;
        t_degree = 0;
        currGraph = inverse;
        currPred = t_pred;
        currSigma = t_sigma;

      }
      int popped = 0;
      int max = currQueue.size();
      while (popped < max) {
        int x = currQueue.removeAt(0);
        popped++;
        if (!currGraph.empty(x)) {
          for (int j = currGraph.start(x); j <= currGraph.end(x); j++) {
            int y = currGraph.adjacency[j];
            if (visited[y] == UNVISITED) {
              if (doing_s) {
                s_degree += currGraph.degree(y);
              } else {
                t_degree += currGraph.degree(y);
              }

              currQueue.add(y);

              if (currPred[y] == null) {
                currPred[y] = new TIntHashSet();
              }
              currPred[y].add(x);
              currSigma[y] += currSigma[x];

              dist[y] = dist[x] + 1;
              visited[y] = visited[x];

            } else if (visited[y] != visited[x]) {
              haveToStop = true;
              midpoints.add(y);
              if (currPred[y] == null) {
                currPred[y] = new TIntHashSet();
              }
              currPred[y].add(x);
              currSigma[y] += currSigma[x];
            } else if (dist[y] == dist[x] + 1) {
              currPred[y].add(x);
              currSigma[y] += currSigma[x];
            }
          }
        }
      }
      if ((doing_s && s_degree == 0) || (!doing_s && t_degree == 0)) {
        haveToStop = true;
      }
    }

    return midpoints;
  }

  private static int add_neighbors(ArrayGraph g, TIntSet candidates, TIntSet frontier, TIntSet other_frontier, TIntSet visited, TIntSet[] pred, long[] sigma) {
    int[] front_nodes = frontier.toArray();
    visited.addAll(frontier);
    frontier.clear();
    int nextSize = 0;

    for (int v : front_nodes) {
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (other_frontier.contains(w)) {
            candidates.add(w);
          }
          if (!visited.contains(w)) {
            frontier.add(w);
            nextSize += g.degree(w);

            if (pred[w] == null) {
              pred[w] = new TIntHashSet();
            }
            pred[w].add(v);
            sigma[w] += sigma[v];
          }
        }
      }
    }
    return nextSize;
  }

  static TIntSet backtrack_path(int s, int t, TIntSet[] s_pred, TIntSet[] t_pred, long[] s_sigma, long[] t_sigma, TIntSet midpoints) {
    Random r = new Random();
    TIntSet path = new TIntHashSet();
    double[] midpointweights = new double[midpoints.size()];
    int[] midpointList = midpoints.toArray();
    for (int j = 0; j < midpoints.size(); j++) {
      midpointweights[j] = t_sigma[midpointList[j]] * s_sigma[midpointList[j]];
    }
    int midpoint = RandomHelper.selectFromWeightedList(r, midpointList, midpointweights);

    // Backtrack

    // To s (but not including)
    int currNode = midpoint;
    while (currNode != s) {
      path.add(currNode);
      currNode = RandomHelper.selectWeightedNode(r, s_pred[currNode].toArray(), s_sigma);
    }
    // To t (but not including)
    currNode = midpoint;
    while (currNode != t) {
      path.add(currNode);
      currNode = RandomHelper.selectWeightedNode(r, t_pred[currNode].toArray(), t_sigma);
    }
    return path;
  }

}
