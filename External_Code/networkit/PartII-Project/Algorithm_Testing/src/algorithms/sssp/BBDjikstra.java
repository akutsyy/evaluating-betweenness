package algorithms.sssp;

import algorithms.heaps.binary.MinBinaryHeap;
import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Arrays;
import utility.MathUtils;

public class BBDjikstra {

  public static TIntSet findRandomShortestPath(ArrayGraph g, ArrayGraph inverse, int s, int t) {
    if (s == t) {
      return new TIntHashSet();
    }
    // Single-source shortest-paths problem

    TIntSet[] s_pred = new TIntHashSet[g.size()];
    TIntSet[] t_pred = new TIntHashSet[g.size()];
    long[] s_sigma = new long[g.size()];
    long[] t_sigma = new long[g.size()];

    TIntSet midpoints = BBDjikstra.find_midpoints(g, inverse, s, t, s_pred, t_pred, s_sigma, t_sigma);
    if (midpoints.isEmpty()) {
      return midpoints;
    }
    return BBBFS.backtrack_path(s, t, s_pred, t_pred, s_sigma, t_sigma, midpoints);
  }

  // Assume s=/=t
  private static TIntSet find_midpoints(ArrayGraph g, ArrayGraph inverse, int s, int t, TIntSet[] s_pred, TIntSet[] t_pred, long[] s_sigma, long[] t_sigma) {

    double[] s_dist = new double[g.size()];
    double[] t_dist = new double[g.size()];

    Arrays.fill(s_dist, Double.POSITIVE_INFINITY);
    Arrays.fill(t_dist, Double.POSITIVE_INFINITY);

    s_dist[s] = 0;
    t_dist[t] = 0;

    MinBinaryHeap s_frontier = new MinBinaryHeap(g.size(), s_dist);
    MinBinaryHeap t_frontier = new MinBinaryHeap(g.size(), t_dist);

    boolean[] s_visited = new boolean[g.size()];
    boolean[] t_visited = new boolean[g.size()];

    s_visited[s] = true;
    t_visited[t] = true;

    s_frontier.insertOrDecrease(s);
    t_frontier.insertOrDecrease(t);

    s_sigma[s] = 1;
    t_sigma[t] = 1;
    TIntSet candidates = new TIntHashSet();

    int i = 0;
    double mu = Double.POSITIVE_INFINITY; //s->t min dist
    while (!s_frontier.isEmpty() && !t_frontier.isEmpty()) {
      int u = s_frontier.pop();
      int v = t_frontier.pop();

      if (s_dist[u] + t_dist[v] > mu + MathUtils.EPSILON) {
        continue;
      }

      if (!g.empty(u)) {
        for (int j = g.start(u); j <= g.end(u); j++) {
          int w = g.adjacency[j];
          s_visited[w] = true;
          relax(g, u, j, s_dist, s_frontier, s_pred, s_sigma);

          if (s_dist[u] + g.weights[j] + t_dist[w] < mu - MathUtils.EPSILON) {
            mu = s_dist[u] + g.weights[j] + t_dist[w]; //s->u->w->t dist
            candidates.clear();
          }

          if (mu < Double.POSITIVE_INFINITY && MathUtils.fuzzyEquals(mu, s_dist[u] + g.weights[j] + t_dist[w])) {
            candidates.add(w);
          }
        }
      }

      if (!inverse.empty(v)) {
        for (int j = inverse.start(v); j <= inverse.end(v); j++) {
          int w = inverse.adjacency[j];
          t_visited[w] = true;
          relax(inverse, v, j, t_dist, t_frontier, t_pred, t_sigma);

          if (t_dist[v] + inverse.weights[j] + s_dist[w] < mu - MathUtils.EPSILON) {
            mu = t_dist[v] + inverse.weights[j] + s_dist[w]; //t->v->w->s dist
            candidates.clear();
          }
          if (mu < Double.POSITIVE_INFINITY && MathUtils.fuzzyEquals(mu, t_dist[v] + inverse.weights[j] + s_dist[w])) {
            candidates.add(w);
          }
        }
      }
    }

    return candidates; // empty if s and t don't connect

  }

  private static void relax(ArrayGraph g, int v, int edge, double[] dist, MinBinaryHeap frontier, TIntSet[] pred, long[] sigma) {
    int w = g.adjacency[edge];
    double newDist = dist[v] + g.weights[edge];
    if (newDist < dist[w] - MathUtils.EPSILON) {
      dist[w] = newDist;
      frontier.insertOrDecrease(w);
      pred[w] = null;
      sigma[w] = 0;
    }

    // Path counting - shortest path to w via v?
    if (MathUtils.fuzzyEquals(dist[w], newDist)) {
      //set σ(s,w) to σ(s,w)+σ(s,v)
      sigma[w] += sigma[v];
      if (pred[w] == null) {
        pred[w] = new TIntHashSet();
      }
      pred[w].add(v);
    }
  }

  private static void add_neighbors(ArrayGraph g, TIntSet candidates, double[] dist, MinBinaryHeap frontier, boolean[] visited, boolean[] otherVisited, TIntSet[] pred, long[] sigma) {
    int v = frontier.pop();
    if (!g.empty(v)) {
      for (int j = g.start(v); j <= g.end(v); j++) {
        int w = g.adjacency[j];

        double newDist = dist[v] + g.weights[j];
        // New path found
        if (newDist < dist[w]) {
          visited[w] = true;
          dist[w] = newDist;
          frontier.insertOrDecrease(w);
          pred[w] = null;
          sigma[w] = 0;
        }

        // Path counting - shortest path to w via v?
        if (MathUtils.fuzzyEquals(dist[w], newDist)) {
          //set σ(s,w) to σ(s,w)+σ(s,v)
          sigma[w] += sigma[v];
          if (pred[w] == null) {
            pred[w] = new TIntHashSet();
          }
          pred[w].add(v);

          if (otherVisited[w]) {
            candidates.add(w);
          }
        }
      }
    }
  }

}
