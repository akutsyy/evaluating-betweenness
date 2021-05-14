package testing.unitTests;

import static framework.parsing.Harness.parseOpts;

import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import framework.parsing.Harness;
import java.io.IOException;
import java.util.HashMap;
import utility.GraphStatistics;
import utility.Printing;

public class DiameterTester {

  public static void main(String[] args)
      throws IOException, FileTypeException {
    HashMap<String, String> options = parseOpts(args);

    Printing.set_debug_level(0);
    // Keep g for scope of calculation

    ArrayGraph g = new ArrayGraph(Harness.getFile(options), Harness.getFileType(options));
    ArrayGraph inverse = ArrayGraph.getInverseGraph(g);
    for (int i = 0; i < 10; i++) {
      System.out.println(GraphStatistics.approximateVertexDiameter(g, inverse, 5));
    }

  }
}
