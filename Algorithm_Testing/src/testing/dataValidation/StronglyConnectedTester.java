package testing.dataValidation;

import static framework.parsing.Harness.parseOpts;

import algorithms.sssp.DFS;
import com.sun.jdi.InvalidTypeException;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import framework.parsing.Harness;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import testing.jgraphtValidation.JGraphTValidate;
import utility.GraphStatistics;
import utility.Printing;

public class StronglyConnectedTester {

  public static void main(String[] args)
      throws IOException, FileTypeException, InvalidTypeException {
    HashMap<String, String> options = parseOpts(args);

    Printing.set_debug_level(5);
    // Keep g for scope of calculation

    ArrayGraph g = new ArrayGraph(Harness.getFile(options), Harness.getFileType(options));
    ArrayGraph inverse = ArrayGraph.getInverseGraph(g);

    System.out.println(GraphStatistics.findConnectedComponents(g, inverse).size());
    JGraphTValidate.findConnected(g);

    System.out.println(g.size());
    System.out.println(DFS.DFS(g, new Random().nextInt(g.size())).size());

  }
}
