package framework.graphs.richgraph;

import java.util.Collection;
import java.util.HashSet;

@Deprecated
public class RichNode extends Node {

  public Collection<Edge> getConnections() {
    return connections;
  }

  public void setConnections(HashSet<Edge> connections) {
    this.connections = connections;
  }

  HashSet<Edge> connections = new HashSet<>();

  public RichNode() {
    super();
  }

  public RichNode(Node n) {
    super(n);
  }

  public RichNode(int n) {
    super(n);
  }

}
