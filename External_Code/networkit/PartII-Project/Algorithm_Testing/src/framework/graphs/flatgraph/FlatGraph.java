package framework.graphs.flatgraph;

import framework.graphs.Graph;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileReadException;
import framework.main.FileType;
import framework.main.FileTypeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

// Nodes and edges
@Deprecated
public class FlatGraph extends Graph {

  public HashSet<Integer> getNodes() {
    ArrayGraph.GraphAccesses++;
    return nodes;
  }

  public ArrayList<SimpleEdge> getEdges() {
    ArrayGraph.GraphAccesses++;
    return edges;
  }


  private HashSet<Integer> nodes = new HashSet<>();
  private final ArrayList<SimpleEdge> edges = new ArrayList<>();


  public FlatGraph(File graph_file, FileType type) throws FileNotFoundException, FileTypeException {
    super(graph_file, type);
    isWeighted = type.isWeighted();
    isDirected = type.isDirected();
    switch (type.getType()) {
      case ID_ID_List:
        parseIDIDList(graph_file);
        break;
      case Weighted_CSV:
      case Unweighted_CSV:
        parseCSV(graph_file);
        break;
      default:
        throw new FileTypeException("Could not find " + type.getType());
    }

    this.normalizeIDs();

  }

  private void parseCSV(File graph_file) throws FileNotFoundException {
    Scanner fileScanner = new Scanner(graph_file);
    while (fileScanner.hasNextLine()) {
      String line = fileScanner.nextLine();
      try {
        line = line.strip();
        // Ignore empty or commented lines
        if (line.length() != 0 && line.charAt(0) != '#') {
          int first = Integer.parseInt(line.split(",")[0]); //Any amount of whitespace
          int second = Integer.parseInt(line.split(",")[1]);
          nodes.add(first);
          nodes.add(second);

          // default weight is 1
          double weight = this.isWeighted ? Double.parseDouble(line.split(",")[2]) : 1;
          edges.add(new SimpleEdge(first, second, weight));
          if (!isDirected) {
            edges.add(new SimpleEdge(second, first, weight));
          }

        }
      } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
        e.printStackTrace();
        throw new FileReadException(
            "Invalid ID-ID line: \n" + line);
      }
    }
  }

  private void parseIDIDList(File graph_file) throws FileNotFoundException {
    Scanner fileScanner = new Scanner(graph_file);
    while (fileScanner.hasNextLine()) {
      String line = fileScanner.nextLine();
      try {
        line = line.strip();
        // Ignore empty or commented lines
        if (line.length() != 0 && line.charAt(0) != '#') {
          int first = Integer.parseInt(line.split("\\s+")[0]); //Any amount of whitespace
          int second = Integer.parseInt(line.split("\\s+")[1]);
          nodes.add(first);
          nodes.add(second);

          edges.add(new SimpleEdge(first, second));
          if (!isDirected) {
            edges.add(new SimpleEdge(second, first));
          }
        }
      } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
        e.printStackTrace();
        throw new FileReadException(
            "Invalid ID-ID line: \n" + line);
      }
    }
  }

  @Override
  public void normalizeIDs() {
    int newID = 0;
    HashSet<Integer> newNodes = new HashSet<>();
    for (Integer n : nodes) {
      newNodes.add(newID);
      for (SimpleEdge e : edges) {
        if (e.getFrom() == n) {
          e.from = newID;
        }
        if (e.getTo() == n) {
          e.to = newID;
        }
      }
      newID++;
    }
    nodes = newNodes;
  }

  @Override
  public String toString() {
    return "Nodes: " + nodes.toString() + "\n Edges: " + edges.toString();
  }
}
