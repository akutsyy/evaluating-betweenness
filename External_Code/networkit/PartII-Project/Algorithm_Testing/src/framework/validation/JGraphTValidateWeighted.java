package framework.validation;

import com.sun.jdi.InvalidTypeException;
import framework.graphs.arraygraph.ArrayGraph;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

class JGraphTValidateWeighted {

  private final DefaultDirectedWeightedGraph<Integer, DefaultWeightedEdge> jGraph;
  private final boolean isDirected;

  public JGraphTValidateWeighted(ArrayGraph g) throws InvalidTypeException {
    this.isDirected = g.isDirected();
    if (!g.isWeighted()) {
      throw new InvalidTypeException("g should be weighted");
    }

    jGraph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    for (int n : g.getNodes()) {
      jGraph.addVertex(n);
    }

    for (int i : g.getNodes()) {
      if (!g.empty(i)) {
        for (int j = g.start(i); j <= g.end(i); j++) {
          DefaultWeightedEdge e = jGraph.addEdge(i, g.adjacency[j]);
          jGraph.setEdgeWeight(e, g.weights[j]);
        }
      }
    }
  }

  public Map<Integer, Double> getBetweeness() {
    BetweennessCentrality<Integer, DefaultWeightedEdge> centralityCalculator
        = new BetweennessCentrality<>(jGraph);
    Map<Integer, Double> betweeness = centralityCalculator.getScores();
    if (isDirected) {
      return betweeness;
    } else {
      Map<Integer, Double> divided = new HashMap<>();
      for (Entry<Integer, Double> e : betweeness.entrySet()) {
        divided.put(e.getKey(), e.getValue() / 2);
      }
      return divided;
    }
  }

  public void findConnected() {
    ConnectivityInspector<Integer, DefaultWeightedEdge> connectivityInspector = new ConnectivityInspector<>(jGraph);
    int largest = connectivityInspector.connectedSets().stream().mapToInt(Set::size).max().getAsInt();
    System.out.println(connectivityInspector.connectedSets().size() + " connected sets, largest is " + largest + " nodes");
  }
}
