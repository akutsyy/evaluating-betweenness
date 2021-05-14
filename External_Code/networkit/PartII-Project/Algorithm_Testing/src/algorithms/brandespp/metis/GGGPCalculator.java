package algorithms.brandespp.metis;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.Random;

public class GGGPCalculator {

  public static TIntSet GGGP(ArrayGraph g) {

    // +2 for a bit of leeway
    TIntHashSet partition = new TIntHashSet(g.size() / 2 + 2);
    double[] dis = new double[g.size()];
    GainBucketHeap heap = new GainBucketHeap(g.getNodes().length, dis);

    Random random = new Random();
    int startNode = random.nextInt(g.size());
    partition.add(startNode);

    // Initialize displacements
    for (int i = 0; i < g.size(); i++) {
      if (!g.empty(i)) {
        for (int j = g.start(i); j <= g.end(i); j++) {
          double weight = g.weights[j];
          dis[i] -= weight;
        }
      }
    }
    updateAdjacents(g, startNode, heap, dis, partition);

    while (partition.size() < g.size() / 2) {
      if (heap.isEmpty()) {
        addNeighbors(g, heap, partition);
        if (heap.isEmpty()) {
          break;
        }
      }
      int toAdd = heap.pop();
      partition.add(toAdd);
      updateAdjacents(g, toAdd, heap, dis, partition);

    }

    return partition;
  }

  private static void addNeighbors(ArrayGraph g, GainBucketHeap heap, TIntHashSet partition) {
    TIntIterator it = partition.iterator();
    while (it.hasNext()) {
      int v = it.next();
      if (!g.empty(v)) {
        for (int u = g.start(v); u <= g.end(v); u++) {
          if (!partition.contains(g.adjacency[u])) {
            heap.insert(g.adjacency[u]);
          }
        }
      }
    }
  }

  public static TIntSet findBestPartition(ArrayGraph g, TIntSet[] possiblePartitions) {
    double minScore = Integer.MAX_VALUE;
    int bestPart = 0;

    for (int i = 0; i < possiblePartitions.length; i++) {
      // Skip duds
      if (possiblePartitions[i].size() == 1) {
        continue;
      }
      double score = calculateEdgeCut(g, possiblePartitions[i]);
      if (score < minScore) {
        minScore = score;
        bestPart = i;
      }
    }
    return possiblePartitions[bestPart];
  }

  public static double calculateEdgeCut(ArrayGraph g, TIntSet partition) {
    double edgeCut = 0;

    for (int u = 0; u < g.size(); u++) {
      if (!g.empty(u)) {
        for (int j = g.start(u); j <= g.end(u); j++) {
          if (partition.contains(g.adjacency[j]) != partition.contains(u)) {
            edgeCut += g.weights[j];
          }
        }
      }
    }
    return edgeCut;
  }

  public static void updateAdjacents(ArrayGraph g, int added, GainBucketHeap heap, double[] dis,
      TIntSet partition) {
    for (int node = 0; node < g.size(); node++) {
      if (!partition.contains(node) && !g.empty(node)) {
        for (int j = g.start(node); j <= g.end(node); j++) {
          if (g.adjacency[j] == added) {
            // we have found a neighbor, edge (node --> added), need to update node
            dis[node] += 2 * g.weights[j];
            if (heap.contains(node)) {
              heap.update(node);
            } else {
              heap.insert(node);
            }
          }
        }
      }
    }
  }
}
