package framework.graphs.setgraph;

import static utility.TMapUtility.getOrDefault;

import framework.graphs.Graph;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileReadException;
import framework.main.FileType;
import framework.main.FileTypeException;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import utility.Printing;

// Nodes and edges
public class SetGraph extends Graph {

  public TIntObjectMap<TIntDoubleMap> nodes;
  private int[] nodesArray = null; // Lazily initialized
  private final TIntSet keys = null; // Lazily initialized


  protected SetGraph(boolean isWeighted, boolean isDirected) {
    super(isWeighted, isDirected);
  }

  public SetGraph(int i, boolean isWeighted, boolean isDirected) {
    this(isWeighted, isDirected);
    nodes = new TIntObjectHashMap<>(i);
  }

  public SetGraph(File graph_file, FileType type) throws FileNotFoundException, FileTypeException {
    super(graph_file, type);
    isWeighted = type.isWeighted();
    isDirected = type.isDirected();
    switch (type.getType()) {
      case ID_ID_List:
        parseIDIDList(graph_file);
        break;
      case Weighted_CSV:
      case Unweighted_CSV:
      case Undirected_Weighted_CSV:
        parseCSV(graph_file);
        break;
      default:
        throw new FileTypeException("Could not find " + type.getType());
    }
  }


  public int[] getNodesArray() {
    if (nodesArray == null) {
      nodesArray = nodes.keys();
    }
    return nodesArray;
  }

  public TIntSet getEdges(int i) {
    return nodes.get(i).keySet();
  }

  public TIntDoubleMap getEdgeWeights(int i) {
    return nodes.get(i);
  }

  public int nodeSize() {
    return nodes.size();
  }

  public int edgeSize() {
    int size = 0;
    TIntIterator it = nodes.keySet().iterator();
    while (it.hasNext()) {
      size += getEdges(it.next()).size();
    }
    return size;
  }


  public void addRemains(TIntSet remains, TIntIntMap matching, SetGraph other) {
    TIntIterator remainsIt = remains.iterator();
    while (remainsIt.hasNext()) {
      int i = remainsIt.next();
      ArrayGraph.GraphAccesses++;

      TIntIterator edges = getEdges(i).iterator();
      other.nodes.put(i, new TIntDoubleHashMap());

      while (edges.hasNext()) {
        int edge = edges.next();
        double weight = getEdgeWeights(i).get(edge);
        other.nodes.get(i).put(getOrDefault(matching, edge, edge), weight);
      }
    }
  }

  public TIntDoubleMap mergeWeights(int a, int b, TIntIntMap mergeList) {
    TIntDoubleHashMap newNode = new TIntDoubleHashMap();
    addAllWeightsFirst(a, b, newNode, mergeList);
    addAllWeightsSecond(b, newNode, mergeList);
    return newNode;
  }

  private void addAllWeightsFirst(int a, int b, TIntDoubleMap newNode, TIntIntMap mergeList) {
    TIntDoubleMap aWeights = nodes.get(a);

    TIntIterator aNodeIt = getEdges(a).iterator();
    // Iterate over edges from a, add with weights (and new name)
    while (aNodeIt.hasNext()) {
      ArrayGraph.GraphAccesses++;
      int next = aNodeIt.next();
      int newName = getOrDefault(mergeList, next, next);
      double weight = aWeights.get(next);
      newNode.put(newName, weight);
    }
  }

  private void addAllWeightsSecond(int b, TIntDoubleMap newNode, TIntIntMap mergeList) {
    TIntDoubleMap bWeights = nodes.get(b);

    TIntIterator bNodeIt = getEdges(b).iterator();
    // Iterate over edges from b, add weights if duplicate
    while (bNodeIt.hasNext()) {
      ArrayGraph.GraphAccesses++;
      int next = bNodeIt.next();
      int newName = getOrDefault(mergeList, next, next);
      double weight;

      if (newNode.containsKey(newName)) {
        weight = newNode.get(newName) + bWeights.get(next);
      } else {
        weight = bWeights.get(next);
      }
      newNode.put(newName, weight);

    }
  }


  @Override
  // Assumes no negative IDs
  public void normalizeIDs() {
    TIntIntMap conversionMap = new TIntIntHashMap();
    TIntLinkedList missing = new TIntLinkedList();

    for (int i = 0; i < nodes.keySet().size(); i++) {
      if (!nodes.keySet().contains(i)) {
        missing.add(i);
      }
    }

    TIntIterator it = nodes.keySet().iterator();
    while (it.hasNext()) {
      int node = it.next();
      // Needs to be replaced
      if (node >= nodeSize()) {
        conversionMap.put(node, missing.removeAt(0));
      }
    }
    normalizeIDs(conversionMap);
  }

  private void normalizeIDs(TIntIntMap conversionMap) {
    TIntObjectMap<TIntDoubleMap> newNodes = new TIntObjectHashMap<>();

    TIntIterator it = nodes.keySet().iterator();
    while (it.hasNext()) {
      int i = it.next();
      TIntDoubleMap newNode = new TIntDoubleHashMap();
      newNodes.put(getOrDefault(conversionMap, i, i), newNode);
      TIntIterator it2 = nodes.get(i).keySet().iterator();
      while (it2.hasNext()) {
        int j = it2.next();
        newNode.put(getOrDefault(conversionMap, j, j), nodes.get(i).get(j));
      }
    }
    nodes = newNodes;
  }


  @Override
  public String toString() {
    StringBuilder toReturn = new StringBuilder("[\n");
    TIntIterator it = nodes.keySet().iterator();
    while (it.hasNext()) {
      int i = it.next();
      toReturn.append(i).append(": ");
      toReturn.append(nodes.get(i).toString());
      toReturn.append("\n");
    }
    toReturn.append("]");
    return toReturn.toString();
  }

  private void parseIDIDList(File graph_file) throws FileNotFoundException {
    parseFile(graph_file, "(\\s+)|(\\t+)");
  }

  private void parseCSV(File graph_file) throws FileNotFoundException {
    parseFile(graph_file, ",");
  }

  protected void parseFile(File graph_file, String separator) throws FileNotFoundException {
    nodes = new TIntObjectHashMap<>();
    boolean multigraph = false;
    BufferedReader reader = new BufferedReader(new FileReader(graph_file));
    int lines = 0;
    try {
      while (reader.readLine() != null) {
        lines++;
      }
    } catch (IOException e) {
      lines = (int) graph_file.length() / 10;
    }

    Scanner fileScanner = new Scanner(graph_file);
    String line = "";
    int i = 0;
    try {
      while (fileScanner.hasNextLine()) {
        Printing.printProgress(i, lines / 100);
        i++;

        line = fileScanner.nextLine();
        line = line.strip();
        // Ignore empty or commented lines
        if (line.length() != 0 && line.charAt(0) != '#' && line.charAt(0) != '%') {
          int first = Integer.parseInt(line.split(separator)[0]);
          int second = Integer.parseInt(line.split(separator)[1]);

          // Default weight of 1
          double weight = this.isWeighted ? Double.parseDouble(line.split(separator)[2]) : 1;

          if(weight<=0){
            System.out.println("WARNING: NON-POSITIVE WEIGHT OF "+weight+" ON EDGE "+first+"->"+second);
          }

          if (!nodes.containsKey(first)) {
            nodes.put(first, new TIntDoubleHashMap());
          }
          if (!nodes.containsKey(second)) {
            nodes.put(second, new TIntDoubleHashMap());
          }
          if (isDirected && nodes.get(first).containsKey(second)) {
            multigraph = true;
          }
          nodes.get(first).put(second, weight);

          // Add reverse edge
          if (!isDirected) {
            nodes.get(second).put(first, weight);
          }
        }
      }
    } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
      e.printStackTrace();
      throw new FileReadException(
          "Invalid ID-ID line: \n" + line);
    }
    if (multigraph) {
      System.out.println("WARNING, COMPRESSING MULTIGRAPH");
    }
  }
}
