package algorithms.brandespp;

import static utility.Printing.print_debug;
import static utility.Printing.print_debug_nobreak;

import algorithms.heaps.MinPriorityHeap;
import algorithms.heaps.binary.MinBinaryHeap;
import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import org.jgrapht.alg.util.Pair;
import utility.MathUtils;

@SuppressWarnings("SpellCheckingInspection")
public class SkeletonCalculator {

  public static ArrayGraph getSkeleton(ArrayGraph g, TIntSet targets, ArrayList<TIntSet> partitions,
      TIntObjectMap<TIntIntMap> globalNumPaths, TIntObjectMap<TIntDoubleMap> globalDistances, ArrayList<TIntSet> frontiers,
      ArrayList<TIntSet> nonFrontiers) {

    TIntIntMap assignments = new TIntIntHashMap(g.size());
    for (int i = 0; i < partitions.size(); i++) {
      TIntIterator it = partitions.get(i).iterator();
      while (it.hasNext()) {
        assignments.put(it.next(), i);
      }
      frontiers.add(new TIntHashSet());
      nonFrontiers.add(new TIntHashSet());
    }

    TIntSet allFrontiers = new TIntHashSet();
    calculateFrontiers(g, assignments, frontiers, nonFrontiers, allFrontiers);

    int f = 0;
    TIntIterator targetIt = targets.iterator();
    while (targetIt.hasNext()) {
      int target = targetIt.next();
      if (!allFrontiers.contains(target)) {
        f++;
        // Remove target from old partition
        partitions.get(assignments.get(target)).remove(target);
        nonFrontiers.get(assignments.get(target)).remove(target);
        // Add it to its own partition
        TIntSet newPart = new TIntHashSet();
        newPart.add(target);
        assignments.put(target, partitions.size());
        allFrontiers.add(target);
        frontiers.add(newPart);
        partitions.add(newPart);
      }
    }
    print_debug(10, "removed " + f + " nodes from interior of clusters, pushed to skeleton");

    print_debug_nobreak(10, "[");
    for (TIntSet p : partitions) {
      print_debug_nobreak(10, p.size() + ", ");
    }
    print_debug(10, "]");

    print_debug(10, "nodes not in frontiers: " + (g.size() - allFrontiers.size()));

    return constructSkeleton(g, assignments, frontiers, partitions, allFrontiers, globalNumPaths,
        globalDistances);

  }

  private static void calculateFrontiers(ArrayGraph g, TIntIntMap assignments,
      ArrayList<TIntSet> frontiers, ArrayList<TIntSet> nonFrontiers, TIntSet allFrontiers) {
    for (int i = 0; i < g.size(); i++) {
      if (!g.empty(i)) {
        int from = assignments.get(i);
        for (int j = g.start(i); j <= g.end(i); j++) {
          int to = assignments.get(g.adjacency[j]);
          if (from != to) {
            frontiers.get(from).add(i);
            frontiers.get(to).add(g.adjacency[j]);

            allFrontiers.add(i);
            allFrontiers.add(g.adjacency[j]);
          }
        }
      }
    }
    for (int i = 0; i < g.size(); i++) {
      if (!allFrontiers.contains(i)) {
        nonFrontiers.get(assignments.get(i)).add(i);
      }
    }
  }

  private static ArrayGraph constructSkeleton(ArrayGraph g, TIntIntMap assignments,
      ArrayList<TIntSet> frontierSets, ArrayList<TIntSet> partitions, TIntSet allFrontierNodes,
      TIntObjectMap<TIntIntMap> globalNumPaths,
      TIntObjectMap<TIntDoubleMap> globalDistance) {

    int[] oldToNew = ArrayGraph.oldToNew(allFrontierNodes, g.size()); // normalizes

    // In sk's namespace
    TIntObjectHashMap<TIntSet> edges = new TIntObjectHashMap<>(allFrontierNodes.size());
    TIntObjectMap<TIntDoubleMap> weights = new TIntObjectHashMap<>(allFrontierNodes.size());
    TIntObjectMap<TIntIntMap> paths = new TIntObjectHashMap<>(allFrontierNodes.size());

    // Construct X, clique of each subgraph
    int cliqueSize = 0;
    int nonCliqueSize = 0;
    // a and b in sk's namespace
    // v and w in global namespace
    // Run djikstra's from every source, constrtuct an edge to every reachable frontier
    for (int i = 0; i < frontierSets.size(); i++) {
      TIntSet front = frontierSets.get(i);
      TIntIterator frontIt = front.iterator();
      while (frontIt.hasNext()) {
        int v = frontIt.next();
        int a = oldToNew[v];
        if (!weights.containsKey(a)) {
          weights.put(a, new TIntDoubleHashMap());
          paths.put(a, new TIntIntHashMap());
        }
        Pair<TIntDoubleMap, TIntIntMap> weightsAndPaths = SkeletonCalculator.djikstra(g, front, partitions.get(i), v, globalNumPaths, globalDistance);
        TIntDoubleMap localWeights = weightsAndPaths.getFirst();
        TIntIntMap localPaths = weightsAndPaths.getSecond();
        TIntIterator targetIt = localWeights.keySet().iterator(); // Every reached frontier node
        while (targetIt.hasNext()) {
          int w = targetIt.next();
          int b = oldToNew[w];
          if (!edges.containsKey(a)) {
            edges.put(a, new TIntHashSet());
          }
          edges.get(a).add(b);
          cliqueSize++;
          weights.get(a).put(b, localWeights.get(w));
          paths.get(a).put(b, localPaths.get(w));
        }
      }
    }

    // Construct R
    TIntIterator it = allFrontierNodes.iterator();
    while (it.hasNext()) {
      int v = it.next();
      int a = oldToNew[v];
      if (!weights.containsKey(a)) {
        weights.put(a, new TIntDoubleHashMap());
      }
      if (!paths.containsKey(a)) {
        paths.put(a, new TIntIntHashMap());
      }

      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int u = g.adjacency[j];
          int b = oldToNew[u];
          // Children in different different frontiers
          if (allFrontierNodes.contains(u) && assignments.get(v) != assignments.get(u)) {
            if (!edges.containsKey(a)) {
              edges.put(a, new TIntHashSet());
            }
            nonCliqueSize++;
            edges.get(a).add(b);
            paths.get(a).put(b, 1);
            weights.get(a).put(b, g.weights[j]);
          }
        }
      }
    }
    print_debug(10, "Edges not in cliques: " + nonCliqueSize);
    print_debug(10, "Edges in cliques: " + cliqueSize);
    print_debug(10, "frontiers: " + allFrontierNodes.size());
    print_debug(10, "original edge size: " + g.edgeSize());
    return new ArrayGraph(allFrontierNodes.size(), edges, oldToNew, weights, paths, true, g.isDirected());
  }


  public static Pair<TIntDoubleMap, TIntIntMap> djikstra(ArrayGraph g, TIntSet frontier, TIntSet partition, int s,
      TIntObjectMap<TIntIntMap> globalNumPaths, TIntObjectMap<TIntDoubleMap> globalDistance) {
    // Local: f->f
    // Global: f->c (nonfrontier), s to itself

    // Initialization
    double[] dist = new double[g.size()];
    Arrays.fill(dist, Double.POSITIVE_INFINITY);
    dist[s] = 0.0;
    TIntDoubleMap finalDists = new TIntDoubleHashMap(frontier.size());
    TIntIntMap finalPaths = new TIntIntHashMap(frontier.size());

    // Initialize with all original names
    MinPriorityHeap queue = new MinBinaryHeap(partition.size(), dist);
    queue.insertOrDecrease(s);

    if (!globalDistance.containsKey(s)) {
      globalDistance.put(s, new TIntDoubleHashMap());
      globalNumPaths.put(s, new TIntIntHashMap());
    }

    globalNumPaths.get(s).put(s, 1);

    // Single-source all-shortest-paths problem
    while (!queue.isEmpty()) {
      int v = queue.pop();

      // foreach vertex w such that (v,w) in E
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (partition.contains(w)) {
            double newDist = dist[v] + g.weights[j];

            // New node
            if (newDist < dist[w]) {
              dist[w] = newDist;

              if (frontier.contains(w)) {
                finalDists.put(w, newDist);
                finalPaths.put(w, globalNumPaths.get(s).get(v));

                if (!frontier.contains(s)) { // Going from an interor, we want to record frontiers in global
                  globalNumPaths.get(s).put(w, globalNumPaths.get(s).get(v));
                  globalDistance.get(s).put(w, newDist);
                }
              }

              // Only traverse from non-frontiers onward
              else {
                globalNumPaths.get(s).put(w, globalNumPaths.get(s).get(v));
                globalDistance.get(s).put(w, newDist);

                queue.insertOrDecrease(w);
              }
            }

            // Seen again
            else if (Math.abs(dist[w] - newDist) < MathUtils.EPSILON) {
              if (frontier.contains(w)) {
                finalPaths.put(w, finalPaths.get(w) + globalNumPaths.get(s).get(v));

                if (!frontier.contains(s)) { // Going from an interior, we want to record frontiers in global
                  globalNumPaths.get(s).put(w, globalNumPaths.get(s).get(w) + globalNumPaths.get(s).get(v));
                }
              } else {
                globalNumPaths.get(s).put(w, globalNumPaths.get(s).get(w) + globalNumPaths.get(s).get(v));
              }
            }
          }
        }
      }
    }
    return new Pair<>(finalDists, finalPaths);
  }

}
