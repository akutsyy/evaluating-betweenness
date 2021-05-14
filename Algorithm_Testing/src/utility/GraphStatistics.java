package utility;

import algorithms.sssp.BFS;
import algorithms.sssp.DFS;
import algorithms.sssp.Djikstra;
import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Random;
import org.jgrapht.alg.util.Pair;

public class GraphStatistics {

  public static double approximateVertexDiameter(ArrayGraph g, ArrayGraph inverse) {
    return approximateVertexDiameter(g, inverse, 5);
  }

  // As described in "Matteo Riondato and Evgenios M Kornaropoulos.  Fast approximation of betweennesscentrality through sampling.Data Mining and Knowledge Discovery, 30(2):438–475,2015."
  public static double approximateVertexDiameter(ArrayGraph g, ArrayGraph inverse, int iterations) {
    Random r = new Random();
    ArrayList<TIntSet> connectedComponents = findConnectedComponents(g, inverse);
    double maxLength = 0;
    for (int i = 0; i < iterations; i++) {
      for (TIntSet component : connectedComponents) {
        int[] values = component.toArray();
        int v = values[r.nextInt(values.length)];
        if (!g.isDirected()) {
          double l = findMaxLengthsSumInComponent(g, v, component);
          if (l > maxLength) {
            maxLength = l;
          }
        } else {
          Pair<Integer, Double> p = findMaxLengthInComponent(g, v, component, -1);
          double forward = p.getSecond();
          int used = p.getFirst();
          Pair<Integer, Double> p2 = findMaxLengthInComponent(inverse, v, component, used);
          double backward = p2.getSecond();
          if (forward + backward > maxLength) {
            maxLength = forward + backward;
          }
        }
      }
    }
    return maxLength;
  }

  private static Pair<Integer, Double> findMaxLengthInComponent(ArrayGraph g, int v, TIntSet component, int toAvoid) {
    if (g.isWeighted()) {
      return Djikstra.djikstraFurthestNode(g, v, component, toAvoid);
    } else {
      Pair<Integer, Integer> p = BFS.bfsFurthestNode(g, v, component, toAvoid);
      return new Pair<>(p.getFirst(), (double) p.getSecond());
    }
  }

  private static double findMaxLengthsSumInComponent(ArrayGraph g, int v, TIntSet component) {
    if (g.isWeighted()) {
      return Djikstra.djikstraMaxTwoDistsSum(g, v, component);
    } else {
      return BFS.bfsMaxTwoDistsSum(g, v, component);
    }
  }

  public static ArrayList<TIntSet> findConnectedComponents(ArrayGraph g, ArrayGraph inverse) {
    ArrayList<TIntSet> connectedComponents = new ArrayList<>();
    if (!g.isDirected()) {
      TIntSet allNodes = new TIntHashSet();
      allNodes.addAll(g.getNodes());
      while (!allNodes.isEmpty()) {
        int v = allNodes.iterator().next();
        TIntSet component = DFS.DFS(g, v);
        connectedComponents.add(component);
        allNodes.removeAll(component);
      }
    } else {
      boolean[] visited = new boolean[g.size()];
      TIntArrayList list = new TIntArrayList(g.size());
      for (int i = 0; i < g.size(); i++) {
        if (!visited[i]) {
          visited[i] = true;
          list.addAll(DFS.DFSPostOrder(g, i, visited));
        }
      }
      TIntIterator listIt = list.iterator();
      boolean[] assigned = new boolean[g.size()];
      while (listIt.hasNext()) {
        int v = listIt.next();
        if (!assigned[v]) {
          assigned[v] = true;
          connectedComponents.add(DFS.DFSAssign(inverse, v, assigned));
        }
      }
    }
    return connectedComponents;
  }
}
