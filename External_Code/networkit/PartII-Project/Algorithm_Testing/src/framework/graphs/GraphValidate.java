package framework.graphs;

import framework.graphs.flatgraph.FlatGraph;
import framework.graphs.flatgraph.SimpleEdge;
import framework.graphs.richgraph.Edge;
import framework.graphs.richgraph.RichGraph;
import framework.graphs.richgraph.RichNode;
import java.util.Collection;
import utility.MathUtils;

public class GraphValidate {

  public static boolean validate(FlatGraph g1, RichGraph g2) {
    boolean issues = false;
    if (g1.isWeighted != g2.isWeighted || g1.isDirected != g2.isDirected) {
      System.out.println("PROBLEM: parameters different");
      return false;
    }
    for (Integer i : g1.getNodes()) {
      if (!hasNode(g2.getNodes(), i)) {
        System.out.println("Problem: RichGraph missing node " + i);
        issues = true;
      }
    }
    for (SimpleEdge e : g1.getEdges()) {
      if (!hasEdge(g2.getNodes(), e)) {
        System.out.println("Problem: RichGraph missing edge " + e);
        issues = true;
      }
    }
    //TODO: missing in FlatGraph
    for (RichNode n : g2.getNodes()) {
      if (!g1.getNodes().contains(n.getID())) {
        System.out.println("Problem: FlatGraph missing node " + n.getID());
        issues = true;
      }
      for (Edge e : n.getConnections()) {
        if (!hasEdge(g1.getEdges(), n.getID(), e)) {
          System.out.println("Problem: Flatgraph missing edge " + n.getID() + "-->" + e.getTo());
          issues = true;
        }
      }
    }
    return !issues;
  }

  private static boolean hasNode(Collection<RichNode> nodes, Integer i) {
    RichNode n = new RichNode(i);
    return nodes.contains(n);
  }

  private static boolean hasEdge(Collection<RichNode> nodes, SimpleEdge e) {
    for (RichNode n : nodes) {
      if (n.getID() == e.getFrom()) {
        boolean hasTo = false;
        for (Edge e2 : n.getConnections()) {
          if (e2.getTo().getID() == e.getTo()) {
            if (MathUtils.fuzzyEquals(e2.getWeight(),
                e.getWeight(), 0.00001)) {
              hasTo = true;
            }
          }
        }
        return hasTo;
      }
    }
    return false;
  }

  private static boolean hasEdge(Collection<SimpleEdge> edges, int from, Edge e) {
    boolean hasEdge = false;
    for (SimpleEdge e2 : edges) {
      if (e2.getFrom() == from && e2.getTo() == e.getTo().getID()) {
        if (MathUtils
            .fuzzyEquals(e.getWeight(), e2.getWeight(),
                0.00001)) {
          hasEdge = true;
        }
      }
    }
    return hasEdge;
  }
}
