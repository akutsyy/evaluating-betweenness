package framework.graphs.richgraph;

import framework.graphs.Graph;
import framework.graphs.flatgraph.FlatGraph;
import framework.graphs.flatgraph.SimpleEdge;
import framework.parsing.FileType;
import framework.parsing.FileTypeException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;

// Nodes with Neighbors
@Deprecated
public class RichGraph extends Graph {

  @Override
  @Deprecated
  public void normalizeIDs() {

  }

  @Override
  public boolean isWeighted() {
    return isWeighted;
  }

  @Override
  public boolean isDirected() {
    return isDirected;
  }


  public HashSet<RichNode> getNodes() {
    this.noteAccess();
    return nodes;
  }

  HashSet<RichNode> nodes;

  public int nodeSize() {
    return nodeSize;
  }

  public int edgeSize() {
    return edgeSize;
  }

  int nodeSize;
  int edgeSize;

  public RichGraph(File graph_file, FileType type) throws FileNotFoundException, FileTypeException {
    super(graph_file, type);
    FlatGraph flat = new FlatGraph(graph_file, type);
    HashMap<Integer, RichNode> nodeMap = new HashMap<>();
    this.isDirected = flat.isDirected();
    this.isWeighted = flat.isWeighted();

    // Copy nodes
    for (Integer n : flat.getNodes()) {
      RichNode richNode;
      richNode = new RichNode(n);

      nodeMap.put(n, richNode);
    }
    // Add edges
    for (SimpleEdge e : flat.getEdges()) {
      RichNode from = nodeMap.get(e.getFrom());
      RichNode to = nodeMap.get(e.getTo());
      from.getConnections().add(new Edge(to, e.getWeight()));
    }

    edgeSize = flat.getEdges().size();
    nodeSize = flat.getNodes().size();

    // Discard indices
    nodes = new HashSet<>(nodeMap.values());
  }

  @Override
  public String toString() {
    StringBuilder s = new StringBuilder("{\n");
    for (RichNode n : nodes) {
      s.append(n.getID()).append(":").append(n.getConnections().toString()).append("\n");
    }
    s.append("}");
    return s.toString();
  }
}
