package algorithms.sssp;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

public class DFS {

  public static TIntSet DFS(ArrayGraph g, int s) {
    TIntArrayList stack = new TIntArrayList();
    TIntSet found = new TIntHashSet();
    stack.add(s);
    found.add(s);

    while (!stack.isEmpty()) {
      int v = stack.removeAt(stack.size() - 1);
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (!found.contains(w)) {
            stack.add(w);
            found.add(w);
          }
        }
      }
    }
    return found;
  }

  // True if path from s to t, primarily for testing purposes
  public static boolean dfsPathExists(ArrayGraph g, int s, int t) {
    // Single-source shortest-paths problem
    TIntArrayList stack = new TIntArrayList();
    TIntSet found = new TIntHashSet();
    stack.add(s);
    while (!stack.isEmpty()) {
      int v = stack.removeAt(stack.size() - 1);
      if (v == t) {
        return true;
      }
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (!found.contains(w)) {
            stack.add(w);
            found.add(w);
          }
        }
      }
    }
    return false;
  }

  public static TIntArrayList DFSPostOrder(ArrayGraph g, int s, boolean[] visited) {
    TIntArrayList stack = new TIntArrayList();
    stack.add(s);
    TIntArrayList postOrderStack = new TIntArrayList();
    visited[s] = true;
    while (!stack.isEmpty()) {
      int v = stack.removeAt(stack.size() - 1);
      postOrderStack.add(v);

      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (!visited[w]) {
            visited[w] = true;
            stack.add(w);
          }
        }

      }
    }
    postOrderStack.reverse();
    return postOrderStack;
  }

  public static TIntSet DFSAssign(ArrayGraph g, int s, boolean[] assigned) {
    TIntSet nodes = new TIntHashSet();
    TIntArrayList stack = new TIntArrayList();
    stack.add(s);
    assigned[s] = true;

    while (!stack.isEmpty()) {
      int v = stack.removeAt(stack.size() - 1);
      nodes.add(v);

      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (!assigned[w]) {
            assigned[w] = true;
            stack.add(w);
          }
        }
      }
    }
    return nodes;
  }
}
