package algorithms.brandespp.metis;

import static utility.Printing.print_debug;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import utility.MathUtils;

public class Metis {

  public static ArrayList<ArrayGraph> metis(ArrayGraph g, int numPartitions, int coarseningLevels, int GGGPIterations, int partitionStopCondition) {
    if (!MathUtils.isPowerOfTwo(numPartitions) && numPartitions < 2) {
      throw new RuntimeException("Can only handle powers of two >1 at the moment");
    }
    if (g.size() == 0) {
      ArrayList<ArrayGraph> subGraphs = new ArrayList<>();
      subGraphs.add(g);
      return subGraphs;
    }

    print_debug(10, "Partitioning a graph of size " + g.size() + " for " + numPartitions + " partitions");
    TIntSet partition = bisection(g, coarseningLevels, partitionStopCondition, GGGPIterations);
    TIntSet inverse = new TIntHashSet(g.getNodes());
    inverse.removeAll(partition);

    ArrayGraph g1 = new ArrayGraph(g, partition);
    ArrayGraph g2 = new ArrayGraph(g, inverse);

    ArrayList<ArrayGraph> subGraphs = new ArrayList<>();

    if (numPartitions > 2) {
      subGraphs.addAll(metis(g1, numPartitions / 2, coarseningLevels, GGGPIterations, partitionStopCondition));
      subGraphs.addAll(metis(g2, numPartitions / 2, coarseningLevels, GGGPIterations, partitionStopCondition));
    } else {
      subGraphs.add(g1);
      subGraphs.add(g2);
    }

    return subGraphs;
  }

  public static ArrayList<TIntSet> metisToPartitions(ArrayGraph g, int numPartitions, int coarseningLevels, int GGGPIterations, int partitionStopCondition) {

    if (!MathUtils.isPowerOfTwo(numPartitions) && numPartitions < 2) {
      throw new RuntimeException("Can only handle powers of two >1 at the moment");
    }

    if (g.size() == 0) {
      ArrayList<TIntSet> sets = new ArrayList<>();
      sets.add(new TIntHashSet());
      return sets;
    }

    print_debug(10, "Partitioning a graph of size " + g.size() + " for " + numPartitions + " partitions");
    TIntSet partition = bisection(g, coarseningLevels, partitionStopCondition, GGGPIterations);
    TIntSet inverse = new TIntHashSet(g.getNodes());
    inverse.removeAll(partition);

    ArrayGraph g1 = new ArrayGraph(g, partition);
    ArrayGraph g2 = new ArrayGraph(g, inverse);

    ArrayList<TIntSet> subGraphs = new ArrayList<>();

    if (numPartitions > 2) {
      subGraphs.addAll(metisToPartitions(g1, numPartitions / 2, coarseningLevels, GGGPIterations, partitionStopCondition));
      subGraphs.addAll(metisToPartitions(g2, numPartitions / 2, coarseningLevels, GGGPIterations, partitionStopCondition));
    } else {
      subGraphs.add(new TIntHashSet(g1.getToGlobalNames()));
      subGraphs.add(new TIntHashSet(g2.getToGlobalNames()));
    }

    return subGraphs;
  }

  private static TIntSet bisection(ArrayGraph g, int coarseningLevels, int partitionStopCondition, int GGGPIterations) {
    // coarsen the prescribed number of times
    ArrayGraph[] coarseGraphs = new ArrayGraph[coarseningLevels + 1];
    coarseGraphs[0] = g;
    // Projection i is the projection from graph i to i+1
    int[][] projections = new int[coarseningLevels][g.size() * 2];
    for (int i = 0; i < coarseningLevels; i++) {
      // Don't over-compress graphs
      if (coarseGraphs[i].size() < 10) {
        coarseningLevels = i;
        break;
      }
      Arrays.fill(projections[i], -1);
      coarseGraphs[i + 1] = coarseGraphs[i].coarsening(projections[i]);
    }

    // Partition
    TIntSet partition = partitionGraph(coarseGraphs[coarseningLevels], GGGPIterations);

    //Uncoarsen and refine
    for (int i = coarseningLevels; i > 0; i--) {
      partition = projectPartition(partition, projections[i - 1]);
      partition = refinePartition(coarseGraphs[i], partitionStopCondition, g.size(), partition);
    }

    return partition;
  }

  // BKL(*,1) algorithm
  private static TIntSet refinePartition(ArrayGraph g, int originalSize, int partitionStopCondition, TIntSet partition) {
    return BKLStarOneCalculator.BKLStarOne(g, originalSize, partitionStopCondition, partition);
  }

  private static TIntSet projectPartition(TIntSet partition, int[] projection) {
    TIntSet nextPartition = new TIntHashSet(projection.length);
    TIntIterator it = partition.iterator();
    while (it.hasNext()) {
      int node = it.next();
      if (projection[node * 2 + 1] != -1) {
        nextPartition.add(projection[node * 2 + 1]);
      }
      nextPartition.add(projection[node * 2]);
    }
    return nextPartition;
  }


  public static TIntSet partitionGraph(ArrayGraph g, int GGGPIterations) {
    if (GGGPIterations < 1) {
      throw new InputMismatchException("expected GGGPIterations > 0, got " + GGGPIterations);
    }

    TIntSet[] possiblePartitions = new TIntSet[GGGPIterations];
    for (int i = 0; i < GGGPIterations; i++) {
      possiblePartitions[i] = GGGPCalculator.GGGP(g);
    }

    return GGGPCalculator.findBestPartition(g, possiblePartitions);
  }

}
