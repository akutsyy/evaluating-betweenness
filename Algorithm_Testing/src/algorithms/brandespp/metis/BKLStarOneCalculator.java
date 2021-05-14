package algorithms.brandespp.metis;

import static utility.Printing.print_debug;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TLongIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.set.hash.TLongHashSet;
import utility.MathUtils;
import utility.MutableDouble;

public class BKLStarOneCalculator {

  // Stops when we haven't decreased the edge cut in 'stopCondition' iterations
  public static TIntSet BKLStarOne(ArrayGraph g, int originalSize, int stopCondition,
      TIntSet partition) {
    TIntSet boundary = calculateBoundary(g, partition);
    if (g.size() > 0.02 * originalSize) {
      TIntSet newPartition = BKLIteration(g, stopCondition, partition,
          boundary); // Only do one iteration if the graph is large

      return newPartition == null ? partition
          : newPartition; // Return old if we can't make any progress

    } else {
      //Iterate until we can't improve
      TIntSet oldPartition;
      TIntSet newPartition = partition;
      do {
        oldPartition = newPartition;
        newPartition = BKLIteration(g, stopCondition, partition,
            boundary); // Only do one iteration if the graph is large
      } while (newPartition != null);

      return oldPartition;
    }
  }

  private static TIntSet BKLIteration(ArrayGraph g, int stopCondition, TIntSet partition,
      TIntSet boundary) {
    double[] dis = new double[g.size()];
    GainBucketHeap inHeap = new GainBucketHeap(g.size(), dis);
    GainBucketHeap outHeap = new GainBucketHeap(g.size(), dis);

    TIntSet originalPartition = new TIntHashSet(partition);

    addInitialDisplacements(g, dis, inHeap, outHeap, boundary, partition);

    // No progress can be made
    if (inHeap.isEmpty() || outHeap.isEmpty() || (dis[inHeap.peek()] < 0
        && dis[outHeap.peek()] < 0)) {
      return null;
    }

    TIntSet used = new TIntHashSet(g.size());

    int iterationsSinceImprovement = 0;

    TIntArrayList swapsFromIn = new TIntArrayList(partition.size());
    TIntArrayList swapsFromOut = new TIntArrayList(partition.size());

    int i = 0;
    int bestIt = -1;
    // We want the minimum edge cut
    double bestScore = 0;
    double currScore = 0;

    // Stop if we can't continue or if we have used all of the nodes or if it has been a while since improvement
    while (!inHeap.isEmpty() && !outHeap.isEmpty() && used.size() / 2 < g.size()
        && iterationsSinceImprovement < stopCondition) {

      MutableDouble improvement = new MutableDouble(0);
      long bestPair = popBestPair(g, inHeap, outHeap, dis, improvement);
      int bestIn = MathUtils.first(bestPair);
      int bestOut = MathUtils.second(bestPair);

      // No pair found
      if (bestIn == -1) {
        break;
      }

      swapsFromIn.add(bestIn);
      swapsFromOut.add(bestOut);
      partition.remove(bestIn);
      partition.add(bestOut);
      used.add(bestIn);
      used.add(bestOut);
      updateAdjacents(g, bestIn, bestOut, inHeap, outHeap, dis, partition, used);

      currScore -= improvement.getValue();
      if (currScore < bestScore) {
        bestScore = currScore;
        bestIt = i;
      }
      iterationsSinceImprovement++;
      if (improvement.getValue() > 0) {
        iterationsSinceImprovement = 0;
      }
      i++;
    }

    print_debug(20, "Best iteration is " + bestIt + "/" + (i - 1) + ", score " + bestScore);
    print_debug(20, "initial edge cut:" + GGGPCalculator.calculateEdgeCut(g, originalPartition));
    print_debug(20, "edge cut at end: " + GGGPCalculator.calculateEdgeCut(g, partition));

    for (int j = 0; j <= bestIt; j++) {
      originalPartition.remove(swapsFromIn.get(j));
      originalPartition.add(swapsFromOut.get(j));
    }
    print_debug(20, "new edge cut: " + GGGPCalculator.calculateEdgeCut(g, originalPartition));

    return originalPartition;
  }

  private static long popBestPair(ArrayGraph g, GainBucketHeap inHeap, GainBucketHeap outHeap,
      double[] dis, MutableDouble improvement) {

    double bestGainPair = Double.NEGATIVE_INFINITY;
    TIntSet inContenders = new TIntHashSet();
    TIntSet outContenders = new TIntHashSet();

    // Get top three
    int j = 0;
    while (!inHeap.isEmpty() && !outHeap.isEmpty() && j < 3) {
      int topIn = inHeap.pop();
      int topOut = outHeap.pop();
      if (dis[topIn] + dis[topOut] < bestGainPair) {
        break;
      }
      bestGainPair = Math.max(bestGainPair,
          dis[topIn] + dis[topOut] - g.getEdgeWeight(topIn, topOut) - g
              .getEdgeWeight(topOut, topIn));
      inContenders.add(topIn);
      outContenders.add(topOut);
      j++;
    }

    // Do all combinations
    TLongSet allPairs = new TLongHashSet(inContenders.size() * outContenders.size());
    TIntIterator inIt = inContenders.iterator();
    while (inIt.hasNext()) {
      int in = inIt.next();
      TIntIterator outIt = outContenders.iterator();
      while (outIt.hasNext()) {
        int out = outIt.next();
        allPairs.add(MathUtils.combine(in, out));
      }
    }

    //find best pair
    TLongIterator pairIt = allPairs.iterator();
    long bestPair = MathUtils.combine(-1, -1);
    double bestScore = Double.NEGATIVE_INFINITY;
    while (pairIt.hasNext()) {
      long i = pairIt.next();
      int in = MathUtils.first(i);
      int out = MathUtils.second(i);
      double score = dis[in] + dis[out] - g.getEdgeWeight(in, out) - g.getEdgeWeight(out, in);
      if (score > bestScore) {
        bestPair = i;
        bestScore = score;
      }
    }
    improvement.setValue(bestScore);

    // Put all but best pair back
    pairIt = allPairs.iterator();
    while (pairIt.hasNext()) {
      long i = pairIt.next();
      int first = MathUtils.first(bestPair);
      int second = MathUtils.second(bestPair);
      if (MathUtils.first(bestPair) != first) {
        inHeap.insert(first);
      }
      if (MathUtils.second(bestPair) != second) {
        outHeap.insert(second);
      }
    }

    return bestPair;
  }


  private static void updateAdjacents(ArrayGraph g, int inNode, int outNode, GainBucketHeap inHeap,
      GainBucketHeap outHeap, double[] dis,
      TIntSet partition, TIntSet used) {
    // Update everything pointing to node
    for (int v = 0; v < g.size(); v++) {
      if (!used.contains(v) && !g.empty(v)) { // Don't bother if we can't add it or if it is empty
        for (int j = g.start(v); j <= g.end(v); j++) {
          int u = g.adjacency[j];
          if (u == inNode) {
            // If v->u in same partition, now are not going to be
            if (partition.contains(v)) {
              dis[v] += 2 * g.weights[j];
              inHeap.insert(v); // updates if it was already there
            }
            // If v->u was across the boundary
            else {
              dis[v] -= 2 * g.weights[j];
              outHeap.insert(v);
            }
          } else if (u == outNode) {
            // If v->u in same partition, now are not going to be
            if (!partition.contains(v)) {
              dis[v] += 2 * g.weights[j];
              outHeap.insert(v); // updates if it was already there
            }
            // If v->u was across the boundary
            else {
              dis[v] -= 2 * g.weights[j];
              inHeap.insert(v);
            }
          }
        }
      }
    }

  }

  private static TIntSet calculateBoundary(ArrayGraph g, TIntSet partition) {
    TIntSet boundary = new TIntHashSet();
    // Iterate over nodes
    for (int i = 0; i < g.size(); i++) {
      if (!g.empty(i)) {
        for (int j = g.start(i); j <= g.end(i); j++) {
          if (partition.contains(i) != partition.contains(g.adjacency[j])) {
            boundary.add(i);
            boundary.add(g.adjacency[j]);
          }
        }
      }
    }
    return boundary;
  }

  private static void addInitialDisplacements(ArrayGraph g, double[] dis, GainBucketHeap inHeap,
      GainBucketHeap outHeap,
      TIntSet boundary, TIntSet partition) {
    TIntSet foundIn = new TIntHashSet();
    TIntSet foundOut = new TIntHashSet();

    TIntIterator boundIt = boundary.iterator();
    while (boundIt.hasNext()) {
      int v = boundIt.next();
      //Go over edges of each node
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int u = g.adjacency[j];
          // node v-->u
          if (partition.contains(v)) {
            foundIn.add(v);
            if (partition.contains(u)) {
              dis[v] -= g.weights[j];
            } else {
              dis[v] += g.weights[j];
            }
          } else {
            foundOut.add(v);
            if (!partition.contains(u)) {
              dis[v] -= g.weights[j];
            } else {
              dis[v] += g.weights[j];
            }
          }
        }
      }
    }

    TIntIterator inIt = foundIn.iterator();
    while (inIt.hasNext()) {
      inHeap.insert(inIt.next());
    }

    TIntIterator outIt = foundOut.iterator();
    while (outIt.hasNext()) {
      outHeap.insert(outIt.next());
    }
  }
}
