package framework.graphs.arraygraph;

import static utility.TMapUtility.getOrDefault;

import framework.graphs.Graph;
import framework.graphs.setgraph.SetGraph;
import framework.parsing.FileType;
import framework.parsing.FileTypeException;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import utility.ArrayGenerator;
import utility.Printing;

public class ArrayGraph extends Graph {

  public int[] adjacency;
  public double[] weights;
  public TIntObjectMap<TIntIntMap> numPaths;
  private final int size;
  private int edgeSize;
  private int[] nodes;
  private final int[] edgeList;
  private int[] toLocalNames;
  private double[] invertedWeights;
  private int[] toOriginalNumbers;
  private TIntSet nodeSet;

  public static long EdgeTraversals = 0;

  public ArrayGraph(File graph_file, FileType type)
      throws FileNotFoundException, FileTypeException {
    super(graph_file, type);

    SetGraph g = new SetGraph(graph_file, type);
    g.normalizeIDs();
    edgeList = new int[g.nodes.size() * 2];
    size = g.nodes.size();
    edgeSize = g.edgeSize();

    int[] sortedNodes = g.getNodesArray();
    Arrays.sort(sortedNodes);

    adjacency = new int[edgeList.length];
    Arrays.fill(adjacency, -1);
    weights = new double[edgeList.length];

    int currIndex = 0;

    for (int i : sortedNodes) {
      TIntIterator edgeIt = g.getEdges(i).iterator();
      edgeList[i * 2] = currIndex;
      if (!edgeIt.hasNext()) {
        edgeList[i * 2] = -1;
        edgeList[i * 2 + 1] = -1;
        noteAccess();
      } else {
        while (edgeIt.hasNext()) {
          noteAccess();
          int edge = edgeIt.next();
          adjacency[currIndex] = edge;
          weights[currIndex] = g.getEdgeWeights(i).get(edge);

          currIndex++;
          if (currIndex >= adjacency.length) {
            adjacency = Arrays.copyOf(adjacency, adjacency.length * 2);
            weights = Arrays.copyOf(weights, weights.length * 2);
          }
        }
        edgeList[i * 2 + 1] = currIndex - 1;
      }
    }
  }

  public ArrayGraph(int size, int edgeSize, int[] edgeList, int[] adjacency, double[] weights,
      boolean isWeighted,
      boolean isDirected) {
    super(isWeighted, isDirected);
    this.size = size;
    this.edgeList = edgeList;
    this.adjacency = adjacency;
    this.weights = weights;
    this.edgeSize = edgeSize;
    noteAccess();
  }

  public ArrayGraph(ArrayGraph g, TIntObjectMap<TIntDoubleMap> invertedEdges) {
    super(g.isWeighted, g.isDirected);
    this.size = g.size;
    edgeSize = 0;
    invertedEdges.forEachValue(tIntSet -> {
      edgeSize += tIntSet.size();
      return true;
    });

    edgeList = new int[size * 2];
    adjacency = new int[edgeSize + 1];
    weights = new double[edgeSize + 1];
    Arrays.fill(edgeList, -1);
    Arrays.fill(adjacency, -1);

    int currPos = 0;
    // Iterate over nodes
    for (int node = 0; node < size; node++) {
      // Mark as empty
      if (!invertedEdges.containsKey(node) || invertedEdges.get(node).isEmpty()) {
        edgeList[node * 2] = -1;
        edgeList[node * 2 + 1] = -1;
        noteAccess();
      }
      // Copy over nodes
      else {
        edgeList[node * 2] = currPos;
        for (int v : invertedEdges.get(node).keys()) {
          noteAccess();
          double weight = invertedEdges.get(node).get(v);

          adjacency[currPos] = v;
          weights[currPos] = weight;

          currPos++;
          // Too big
          if (currPos >= adjacency.length) {
            int[] newAdj = new int[adjacency.length * 2];
            double[] newWeights = new double[adjacency.length * 2];
            System.arraycopy(adjacency, 0, newAdj, 0, adjacency.length);
            System.arraycopy(weights, 0, newWeights, 0, adjacency.length);
            adjacency = newAdj;
            weights = newWeights;
          }
        }
        edgeList[node * 2 + 1] = currPos - 1;
      }
    }
    if (edgeSize != currPos) {
      System.out.println("WARNING: edgeSize is " + edgeSize + " but last currpos is " + currPos);
    }
    noteAccess();
  }

  public ArrayGraph(int size, TIntObjectHashMap<TIntSet> edges, int[] oldtoNew, TIntObjectMap<TIntDoubleMap> weightMap, TIntObjectMap<TIntIntMap> pathsMap,
      boolean isWeighted, boolean isDirected) {
    super(isWeighted, isDirected);
    this.size = size;
    edgeSize = 0;
    edges.forEachValue(tIntSet -> {
      edgeSize += tIntSet.size();
      return true;
    });

    this.toLocalNames = oldtoNew;
    this.toOriginalNumbers = new int[size];
    for (int i = 0; i < oldtoNew.length; i++) {
      this.toOriginalNumbers[oldtoNew[i]] = i;
    }

    edgeList = new int[size * 2];
    adjacency = new int[edgeSize];
    weights = new double[edgeSize];
    numPaths = pathsMap;

    Arrays.fill(edgeList, -1);
    Arrays.fill(adjacency, -1);

    int currPos = 0;
    // Iterate over nodes
    for (int node = 0; node < size; node++) {
      // Mark as empty
      if (!edges.containsKey(node) || edges.get(node).isEmpty()) {
        edgeList[node * 2] = -1;
        edgeList[node * 2 + 1] = -1;
        noteAccess();
      }
      // Copy over nodes
      else {
        edgeList[node * 2] = currPos;
        TIntIterator it = edges.get(node).iterator();
        while (it.hasNext()) {
          noteAccess();
          int v = it.next();
          adjacency[currPos] = v;
          if (weightMap.containsKey(node) && weightMap.get(node).containsKey(v)) {
            weights[currPos] = weightMap.get(node).get(v);
          }

          currPos++;
          // Too big
          if (currPos >= adjacency.length) {
            int[] newAdj = new int[adjacency.length * 2];
            double[] newWeights = new double[adjacency.length * 2];

            System.arraycopy(adjacency, 0, newAdj, 0, adjacency.length);
            System.arraycopy(weights, 0, newWeights, 0, adjacency.length);

            adjacency = newAdj;
            weights = newWeights;
          }
        }
        edgeList[node * 2 + 1] = currPos - 1;
      }
    }
    if (edgeSize != currPos) {
      System.out.println("WARNING: edgeSize is " + edgeSize + " but last currpos is " + currPos);
    }
    noteAccess();
  }


  public ArrayGraph(ArrayGraph g, TIntSet partition) {
    super(g.isWeighted, g.isDirected);
    edgeList = new int[partition.size() * 2];
    adjacency = new int[g.adjacency.length / 2 + 2];
    weights = new double[g.adjacency.length / 2 + 2];
    size = partition.size();

    int[] newToOld = newToOld(partition);
    int[] oldToNew = oldToNew(partition, g.size);
    toOriginalNumbers = newToOlder(g, partition);
    int currPos = 0;
    // Iterate over nodes
    for (int node = 0; node < newToOld.length; node++) {
      int old = newToOld[node];
      // Mark as empty
      if (g.empty(old)) {
        edgeList[node * 2] = -1;
        edgeList[node * 2 + 1] = -1;
        noteAccess();
      }
      // Copy over nodes
      else {
        edgeList[node * 2] = currPos;
        for (int j = g.start(old); j <= g.end(old); j++) {
          noteAccess();
          if (partition.contains(g.adjacency[j])) {
            adjacency[currPos] = oldToNew[g.adjacency[j]];
            weights[currPos] = g.weights[j];
            currPos++;
            // Too big
            if (currPos >= adjacency.length) {
              int[] newAdj = new int[adjacency.length * 2];
              double[] newWeights = new double[adjacency.length * 2];
              System.arraycopy(adjacency, 0, newAdj, 0, adjacency.length);
              System.arraycopy(weights, 0, newWeights, 0, adjacency.length);
              adjacency = newAdj;
              weights = newWeights;
            }
          }
        }
        edgeList[node * 2 + 1] = currPos - 1;
      }
    }
    edgeSize = currPos - 1;
    noteAccess();
  }


  public static ArrayGraph getInverseGraph(ArrayGraph g) {
    TIntObjectMap<TIntDoubleMap> inverseEdges = new TIntObjectHashMap<>();
    for (int v = 0; v < g.size; v++) {
      if (!g.empty(v)) {
        for (int j = g.start(v); j <= g.end(v); j++) {
          int w = g.adjacency[j];
          if (!inverseEdges.containsKey(w)) {
            inverseEdges.put(w, new TIntDoubleHashMap());
          }
          inverseEdges.get(w).put(v, g.weights[j]);
        }
      }
    }
    return new ArrayGraph(g, inverseEdges);
  }

  public static int[] oldToNew(TIntSet partition, int size) {
    int[] map = new int[size];
    int i = 0;
    TIntIterator it = partition.iterator();
    while (it.hasNext()) {
      map[it.next()] = i;
      i++;
    }
    return map;
  }

  public static int[] newToOld(TIntSet partition) {
    int[] map = new int[partition.size()];
    int i = 0;
    TIntIterator it = partition.iterator();
    while (it.hasNext()) {
      map[i] = it.next();
      i++;
    }
    return map;
  }

  public static int[] newToOlder(ArrayGraph g, TIntSet partition) {
    int[] map = new int[partition.size()];
    Arrays.fill(map, -1);
    int i = 0;
    TIntIterator it = partition.iterator();
    while (it.hasNext()) {
      int n = it.next();
      map[i] = g.toGlobalName(n);
      i++;
    }
    return map;
  }

  public int[] getNodes() {
    if (nodes == null) {
      nodes = new int[size()];
      for (int i = 0; i < nodes.length; ++i) {
        nodes[i] = i;
      }
    }
    noteAccess();
    return nodes;
  }

  public int size() {
    noteAccess();
    return size;
  }

  public int edgeSize() {
    noteAccess();
    return edgeSize;
  }

  public int degree(int i) {
    noteAccess();
    if (edgeList[i * 2] == -1) {
      return 0;
    }
    return end(i) - start(i) + 1;
  }

  public int start(int i) {
    noteAccess();
    return edgeList[i * 2];
  }

  public int end(int i) {
    noteAccess();
    return edgeList[i * 2 + 1];
  }

  public boolean empty(int i) {
    noteAccess();
    return edgeList[i * 2] == -1;
  }

  public ArrayGraph coarsening(int[] projectionArray) {
    int[] matching = new int[size()];

    int numNewNodes = findMatching(matching);
    int[] cmap = calculateCmapAndProjection(matching, projectionArray);

    Printing.print_debug(10, "Cmap: " + Arrays.toString(cmap));

    int[] newNodes = new int[numNewNodes * 2];
    int[] newAdj = new int[adjacency.length];
    Arrays.fill(newAdj, -1);

    double[] newWeights = new double[weights.length];

    // Build adjacency map
    TIntDoubleMap[] adjMap = new TIntDoubleHashMap[numNewNodes];
    for (int old = 0; old < size(); old++) {
      int newName = cmap[old];
      // Old has edges
      if (!empty(old)) {
        if (adjMap[newName] == null) {
          adjMap[newName] = new TIntDoubleHashMap(2 * (degree(old)));
        }
        //Iterate over edges
        for (int j = start(old); j <= end(old); j++) {
          int x = adjacency[j];
          int newX = cmap[x];

          // Add edge to map
          adjMap[newName].put(newX,
              getOrDefault(adjMap[newName], newX, 0.0) + weights[j]);
        }
      }
    }

    // Copy over elements
    int currPos = 0;
    for (int i = 0; i < numNewNodes; i++) {
      TIntDoubleMap edges = adjMap[i];
      if (edges == null) {
        newNodes[i * 2] = -1;
        newNodes[i * 2 + 1] = -1;
        noteAccess();
      } else {
        newNodes[i * 2] = currPos;
        newNodes[i * 2 + 1] = currPos + edges.size() - 1;
        noteAccess();
        int[] keys = edges.keys();
        for (int j = 0; j < edges.size(); j++) {
          newAdj[currPos + j] = keys[j];
          newWeights[currPos + j] = edges.get(keys[j]);
        }
        currPos += adjMap[i].size();
      }
    }
    return new ArrayGraph(numNewNodes, currPos, newNodes, newAdj, newWeights, true,
        this.isDirected);
  }

  public int[] calculateCmapAndProjection(int[] matching, int[] projection) {
    int[] cmap = new int[matching.length];
    Arrays.fill(cmap, -1);
    int newName = 0;
    for (int i = 0; i < cmap.length; i++) {
      noteAccess();
      // Not already covered
      if (cmap[i] == -1) {
        cmap[i] = newName;
        projection[newName * 2] = i;
        // If this node was matched
        if (matching[i] != -1) {
          cmap[matching[i]] = newName;
          projection[newName * 2 + 1] = matching[i];
        }
        newName++;
      }
    }
    return cmap;
  }

  private int findMatching(int[] matching) {
    Arrays.fill(matching, -1);
    int[] shuffled = ArrayGenerator.shuffledInts(0, size);
    int numNewNodes = size;

    for (int i : shuffled) {
      noteAccess();
      // Only match if we haven't already matched this node
      if (matching[i] == -1) {
        int heaviest = findHeaviest(i, matching);
        // A matching was found
        if (heaviest != -1) {
          matching[i] = heaviest;
          matching[heaviest] = i;
          numNewNodes--;
        }
      }
    }

    return numNewNodes;
  }

  public int findHeaviest(int node, int[] matching) {
    if (empty(node)) {
      return -1;
    }
    int heaviest = -1;
    double heaviestWeight = -1;
    for (int i = start(node); i <= end(node); i++) {
      int v = adjacency[i];
      // Don't do already matched nodes or self-edges
      if (v != node && matching[v] == -1 && weights[i] > heaviestWeight) {
        heaviestWeight = weights[i];
        heaviest = v;
      }
    }
    return heaviest;
  }

  public double getEdgeWeight(int from, int to) {
    if (!empty(from)) {
      for (int i = start(from); i <= end(from); i++) {
        if (adjacency[i] == to) {
          return weights[i];
        }
      }
    }
    return 0;
  }

  public boolean containsEdge(int from, int to) {
    if (!empty(from)) {
      for (int i = start(from); i <= end(from); i++) {
        if (adjacency[i] == to) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  // Already normalized
  public void normalizeIDs() {
  }

  @Override
  public String toString() {
    return Arrays.toString(edgeList) + "\n" + Arrays.toString(adjacency) + "\n" + Arrays
        .toString(weights);
  }

  public int[] getToGlobalNames() {
    if (toOriginalNumbers == null) {
      return getNodes();
    } else {
      return toOriginalNumbers;
    }
  }

  public int toGlobalName(int i) {
    if (toOriginalNumbers == null) {
      return i;
    }
    return toOriginalNumbers[i];
  }

  public TIntSet getNodeSet() {
    if (nodeSet == null) {
      nodeSet = new TIntHashSet(this.getNodes());
    }
    return nodeSet;
  }


  public double getWeight(int v, int u) {
    if (empty(v)) {
      throw new RuntimeException(v + " is empty: ");
    }
    for (int j = start(v); j <= end(v); j++) {
      if (adjacency[j] == u) {
        return weights[j];
      }
    }

    throw new RuntimeException(u + " not found as weight of " + v + " our size is " + size);
  }

  public int maxOriginalValue() {
    if (this.toOriginalNumbers == null) {
      return size;
    } else {
      int max = 0;
      for (int i : toOriginalNumbers) {
        noteAccess();
        if (i > max) {
          max = i;
        }
      }
      return max;
    }
  }

  public int toLocalName(int v) {
    if (toOriginalNumbers == null) {
      return v;
    }
    if (toLocalNames == null) {
      toLocalNames = new int[maxOriginalValue() + 1];
      Arrays.fill(toLocalNames, -1);
      for (int i = 0; i < toOriginalNumbers.length; i++) {
        toLocalNames[toOriginalNumbers[i]] = i;
      }
    }
    return toLocalNames[v];
  }

  public boolean containsGlobal(int v) {
    if (toLocalNames == null || toOriginalNumbers == null) {
      return v < this.size;
    } else {
      if (v >= toLocalNames.length) {
        return false;
      }
      return toLocalNames[v] != -1;
    }
  }

  public void invertWeights() {
    if (!isWeighted) {
      return;
    }

    if (invertedWeights == null) {
      invertedWeights = new double[weights.length];
      for (int i = 0; i < weights.length; i++) {
        noteAccess();
        invertedWeights[i] = 1.0 / weights[i];
      }
    }

    double[] temp = weights;
    weights = invertedWeights;
    invertedWeights = temp;
  }

  public void exportToFile(String filename) {
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < this.size; i++) {
      if (!this.empty(i)) {
        for (int j = this.start(i); j <= this.end(i); j++) {
          output.append(i).append(";").append(this.adjacency[j]).append("\n");
        }
      }
    }
    output = new StringBuilder(output.substring(0, output.length() - 1));
    Printing.printToFile(output.toString(), filename);
  }
}
