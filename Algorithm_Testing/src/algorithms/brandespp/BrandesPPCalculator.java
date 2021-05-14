package algorithms.brandespp;

import static utility.Printing.print_debug;

import algorithms.brandespp.metis.Metis;
import framework.heaps.MinPriorityHeap;
import framework.heaps.binary.MinBinaryHeap;
import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import java.util.ArrayList;
import java.util.Arrays;
import utility.MathUtils;
import utility.ProgressPrinter;
import utility.Statistics;

public class BrandesPPCalculator {

  public static Statistics brandesPP(ArrayGraph g, TIntSet targets, int numPartitions) {
    return brandesPP(g, targets, numPartitions, (int) Math.ceil(Math.log10(g.size())), 5, 100);
  }

  public static Statistics brandesPP(ArrayGraph g, TIntSet targets) {
    return brandesPP(g, targets, 16);
  }


  public static Statistics brandesPP(ArrayGraph g, TIntSet targets, int numPartitions, int coarseningLevels, int GGGPIterations, int partitionStopCondition) {
    long start = System.nanoTime();
    g.invertWeights(); // METIS interprets weights as closeness, Brandes interprets them as distance
    ArrayList<TIntSet> partitions = Metis.metisToPartitions(g, numPartitions, coarseningLevels, GGGPIterations, partitionStopCondition);
    g.invertWeights();
    long end = System.nanoTime();
    double partitionTime = (end - start) / Math.pow(10, 9);
    print_debug(1, "Time taken to partition: " + (end - start) / Math.pow(10, 9));

    return brandesPP(g, targets, partitions, partitionTime);
  }

  public static Statistics brandesPP(ArrayGraph g, TIntSet targets, ArrayList<TIntSet> partitions) {
    return BrandesPPCalculator.brandesPP(g, targets, partitions, 0);
  }

  public static Statistics brandesPP(ArrayGraph g, TIntSet targets, ArrayList<TIntSet> partitions, double partitionTime) {
    long start;
    long end;
    start = System.nanoTime();
    // In g's namespace
    TIntObjectMap<TIntIntMap> globalNumPaths = new TIntObjectHashMap<>(targets.size()); // Bookkeeping for CalculateCentralities
    TIntObjectMap<TIntDoubleMap> globalDistances = new TIntObjectHashMap<>(targets.size());
    TIntIterator targetIt = targets.iterator();
    while (targetIt.hasNext()) {
      int v = targetIt.next();
      globalDistances.put(v, new TIntDoubleHashMap());
      globalNumPaths.put(v, new TIntIntHashMap());
    }

    ArrayList<TIntSet> frontiers = new ArrayList<>(partitions.size());
    ArrayList<TIntSet> nonFrontiers = new ArrayList<>(partitions.size());

    ArrayGraph skeleton = SkeletonCalculator.getSkeleton(g, targets, partitions, globalNumPaths, globalDistances, frontiers, nonFrontiers);
    // At this point, global distance just contains distances frontier->interior

    end = System.nanoTime();
    double skeletonTime = (end - start) / Math.pow(10, 9);
    print_debug(1, "Time taken to construct skeleton: " + (end - start) / Math.pow(10, 9));
    print_debug(3, "Size of skeleton: " + skeleton.size());
    start = System.nanoTime();

    TIntObjectMap<TIntDoubleMap> skDeltas = new TIntObjectHashMap<>(targets.size());

    double[] centralities = brandes_SK(skeleton, targets, globalNumPaths, globalDistances, skDeltas, g.size());
    // Global distances now contains s->f for each source and (reachable) f

    end = System.nanoTime();
    double brandesTime = (end - start) / Math.pow(10, 9);

    print_debug(1, "Time taken to run brandes on skeleton: " + (end - start) / Math.pow(10, 9));

    double frontierTime = 0;
    if (g.isDirected()) {
      start = System.nanoTime();

      calculateDistanceToFrontiers(g, partitions, frontiers, nonFrontiers, globalNumPaths, globalDistances);
      // Globals now contain c->c and c->f (where c is interior) for all c, f

      end = System.nanoTime();
      frontierTime = (end - start) / Math.pow(10, 9);
      print_debug(1, "Time taken to calculate distances to frontiers: " + (end - start) / Math.pow(10, 9));

    }

    start = System.nanoTime();
    int numcentralitiesUsed = calculateCentralities(targets, skDeltas, frontiers, nonFrontiers, globalNumPaths,
        globalDistances, centralities, g.isDirected());

    end = System.nanoTime();
    double centralitiesTime = (end - start) / Math.pow(10, 9);

    print_debug(1, "Time taken to compute other centralities: " + (end - start) / Math.pow(10, 9));

    if (!g.isDirected()) {
      centralities = Arrays.stream(centralities).map(v -> v / 2).toArray();
    }
    print_debug(1,"Number of node centralities actually used: "+numcentralitiesUsed);
    return new Statistics(centralities, numcentralitiesUsed, partitionTime, skeletonTime, brandesTime, frontierTime, centralitiesTime);
  }

  private static void calculateDistanceToFrontiers(ArrayGraph g, ArrayList<TIntSet> partitions,
      ArrayList<TIntSet> frontiers, ArrayList<TIntSet> nonFrontiers,
      TIntObjectMap<TIntIntMap> globalNumPaths,
      TIntObjectMap<TIntDoubleMap> globalDistances) {
    for (int i = 0; i < nonFrontiers.size(); i++) {
      TIntIterator nonFront = nonFrontiers.get(i).iterator();
      while (nonFront.hasNext()) {
        SkeletonCalculator.djikstra(g, frontiers.get(i), partitions.get(i), nonFront.next(), globalNumPaths,
            globalDistances);
      }
    }
  }


  private static double[] brandes_SK(ArrayGraph sk, TIntSet targets,
      TIntObjectMap<TIntIntMap> globalNumPaths, TIntObjectMap<TIntDoubleMap> globalDistances, // Write only, for bookkeeping
      TIntObjectMap<TIntDoubleMap> skDeltas, int size) {
    // Using sk's namespace

    double[] centrality = new double[size]; // betweeness result

    // Initialization

    //Main loop
    TIntArrayList stack = new TIntArrayList(sk.size());

    TIntIterator targetIt = targets.iterator();
    ProgressPrinter p = new ProgressPrinter(targets.size());
    int i = 0;
    while (targetIt.hasNext()) {
      p.print(i);
      i++;
      int a = targetIt.next();
      int s = sk.toLocalName(a);
      // Single-source shortest-paths problem
      //Initialization

      TIntArrayList[] pred = new TIntArrayList[sk.size()]; // predecessors on shortest path from s
      int[] sigma = new int[sk.size()]; // # shortest paths from s to t
      double[] delta = new double[sk.size()]; // dependency of source on v
      double[] dist = new double[sk.size()];

      djikstra_sk(sk, s, stack, dist, pred, sigma);

      if (!globalDistances.containsKey(a)) {
        globalDistances.put(a, new TIntDoubleHashMap());
      }
      if (!globalNumPaths.containsKey(a)) {
        globalNumPaths.put(a, new TIntIntHashMap());
      }
      if (!skDeltas.containsKey(a)) {
        skDeltas.put(a, new TIntDoubleHashMap());
      }

      accumulate_sk(sk, s, a, targets, stack, pred, sigma, delta, centrality, dist, globalNumPaths, globalDistances, skDeltas);
    }

    return centrality;
  }

  private static void djikstra_sk(ArrayGraph sk, int s, TIntArrayList stack, double[] dist,
      TIntArrayList[] pred, int[] sigma) {

    MinPriorityHeap queue = new MinBinaryHeap(sk.size(), dist);

    Arrays.fill(dist, Double.POSITIVE_INFINITY);
    dist[s] = 0.0;
    sigma[s] = 1;
    queue.insert(s);

    while (!queue.isEmpty()) {
      // Dequeue and push
      int v = queue.pop();
      stack.add(v);

      if (!sk.empty(v)) {
        for (int j = sk.start(v); j <= sk.end(v); j++) {
          int w = sk.adjacency[j];

          double newDist = dist[v] + sk.weights[j];

          // New path found
          if (newDist < dist[w]) {
            dist[w] = newDist;
            queue.insertOrDecrease(w);
            pred[w] = null;
            sigma[w] = 0;
          }

          // Path counting - shortest path to w via v?
          if (Math.abs(dist[w] - newDist) < MathUtils.EPSILON) {
            //set σ(s,w) to σ(s,w)+σ(s,v)*multiplicity(v,w)
            sigma[w] += sigma[v] * sk.numPaths.get(v).get(w);
            if (pred[w] == null) {
              pred[w] = new TIntArrayList();
            }
            pred[w].add(v);
          }
        }
      }
    }
  }

  private static void accumulate_sk(ArrayGraph sk, int s, int a, TIntSet targets, TIntArrayList stack, TIntArrayList[] pred, int[] sigma, double[] delta, double[] centrality,
      double[] dist, TIntObjectMap<TIntIntMap> globalNumPaths, TIntObjectMap<TIntDoubleMap> globalDistances, TIntObjectMap<TIntDoubleMap> skDeltas) {
    if (sk.toGlobalName(s) != a) {
      System.out.println("WARNING, incorrect local to global mapping");
    }
    // Missing delta = 0
    while (!stack.isEmpty()) {
      int w = stack.removeAt(stack.size() - 1);
      int c = sk.toGlobalName(w);
      // When popping, no more changes to sigma or dist will occur, this adds s->f distances/paths
      globalNumPaths.get(a).put(c, sigma[w]);
      globalDistances.get(a).put(c, dist[w]);

      if (pred[w] != null) {
        TIntIterator it = pred[w].iterator();
        while (it.hasNext()) {
          int v = it.next();
          int b = sk.toGlobalName(v);

          if (sigma[w] != 0) {
            int ind = targets.contains(c) ? 1 : 0; // as per formula, only add one if w in targets
            delta[v] +=
                ((double) sigma[v] / sigma[w]) * (ind + delta[w]) * sk.numPaths.get(v).get(w);
            skDeltas.get(a).put(b, delta[v]);
          }
        }
      }
      if (w != s) {
        centrality[c] += delta[w];
      }
    }
  }


  private static int calculateCentralities(TIntSet sources, TIntObjectMap<TIntDoubleMap> skDeltas,
      ArrayList<TIntSet> frontiers, ArrayList<TIntSet> nonFrontiers,
      TIntObjectMap<TIntIntMap> globalNumPaths,
      TIntObjectMap<TIntDoubleMap> globalDistances, double[] centralities,
      boolean isDirected) {

    int numCentralitiesUsed = 0;

    // delta(s|v)  = SUM_(f in frontier i) (delta(s|f)*sigma(v,f)*sigma(s,v)/sigma(s,f))
    // sigma(v,f) known from build_sk (in global map)
    // sigma(s,v) can be computed by summing sigma(s,f1)*sigma(f1,f2)*sigma(f2,v)
    // sigma(s,f) can be computed by summing sigma(s,f1)*sigma(f1,f)
    // delta(s|f) is in skDeltas
    TIntIterator sourceIt = sources.iterator();
    while (sourceIt.hasNext()) {
      int s = sourceIt.next();
      // Iterate over clusters
      for (int i = 0; i < nonFrontiers.size(); i++) {
        int[] nonFront = nonFrontiers.get(i).toArray();
        int[] front = frontiers.get(i).toArray();
        // Iterate over non-frontier nodes
        for (int v : nonFront) {
          // V is the node on the path s->v->f

          // Calculate distance s->x->v and sigma(s,v)
          double minDistStoV = Double.POSITIVE_INFINITY;
          int numPathsStoV = 0;

          for (int x : front) {

            double sToXDist = globalDistances.get(s).get(x);
            double xToVDist = globalDistances.get(x).get(v);

            if (sToXDist + xToVDist < minDistStoV) {
              int sToXPaths = globalNumPaths.get(s).get(x);
              int xToVPaths = globalNumPaths.get(x).get(v);

              minDistStoV = sToXDist + xToVDist;
              numPathsStoV = sToXPaths * xToVPaths;
            } else if (sToXDist + xToVDist == minDistStoV) {
              int sToXPaths = globalNumPaths.get(s).get(x);
              int xToVPaths = globalNumPaths.get(x).get(v);

              numPathsStoV += sToXPaths * xToVPaths;
            }
          }
          if (numPathsStoV != 0) {
            // Use distance s->x->v to calculate centrality
            // If s->x->v->f is a shortest path to f, need to do accumulation into v
            for (int f : front) {

              // Don't have to calculate v to f if undirected
              if (!isDirected) {
                if (globalDistances.containsKey(f) && globalDistances.get(f).containsKey(v)) {
                  // V is on the shortest path from s to f
                  if (Math.abs(minDistStoV + globalDistances.get(f).get(v) - globalDistances.get(s).get(f)) < MathUtils.EPSILON) {
                    // C(v) = SUM_{s in S}(SUM_{f in F_i}(delta(s|f)*sigma(v,f)*sigma(s,v)/sigma(s,f)))
                    // Don't add one to delta since v can't be in s by definition
                    centralities[v] +=
                        ((double) numPathsStoV) / ((double) globalNumPaths.get(s).get(f))
                            * globalNumPaths.get(f).get(v) * skDeltas.get(s).get(f);
                    numCentralitiesUsed++;
                  }
                } else {
                  // Same, but uses v->f rather than f->v
                  if (globalDistances.containsKey(v) && globalDistances.get(v).containsKey(f)) {
                    if (Math.abs(minDistStoV + globalDistances.get(v).get(f) - globalDistances.get(s).get(f)) < MathUtils.EPSILON) {
                      centralities[v] +=
                          ((double) numPathsStoV) / ((double) globalNumPaths.get(s).get(f))
                              * globalNumPaths.get(v).get(f) * skDeltas.get(s).get(f);
                      numCentralitiesUsed++;
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    return numCentralitiesUsed;
  }
}