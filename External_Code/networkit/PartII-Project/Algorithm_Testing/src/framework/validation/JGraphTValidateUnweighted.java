package framework.validation;

import com.sun.jdi.InvalidTypeException;
import framework.graphs.arraygraph.ArrayGraph;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.scoring.BetweennessCentrality;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

class JGraphTValidateUnweighted {

  private final DefaultDirectedGraph<Integer, DefaultEdge> jGraph;
  boolean isDirected;

  public JGraphTValidateUnweighted(ArrayGraph g) throws InvalidTypeException {
    if (g.isWeighted()) {
      throw new InvalidTypeException("g should be unweighted");
    }

    isDirected = g.isDirected();
    jGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

    for (int n : g.getNodes()) {
      jGraph.addVertex(n);
    }
    for (int i : g.getNodes()) {
      if (!g.empty(i)) {
        for (int j = g.start(i); j <= g.end(i); j++) {
          jGraph.addEdge(i, g.adjacency[j]);
        }
      }
    }
  }

  public Map<Integer, Double> getBetweeness() {
    BetweennessCentrality<Integer, DefaultEdge> centralityCalculator
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
    ConnectivityInspector<Integer, DefaultEdge> connectivityInspector = new ConnectivityInspector<>(jGraph);
    int largest = connectivityInspector.connectedSets().stream().mapToInt(Set::size).max().getAsInt();
    System.out.println(connectivityInspector.connectedSets().size() + " connected sets, largest is " + largest + " nodes");
  }

}
