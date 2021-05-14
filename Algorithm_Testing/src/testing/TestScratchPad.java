package testing;

import static framework.parsing.Harness.parseOpts;

import algorithms.brandes.BrandesCalculator;
import algorithms.kadabra.KADABRACalculator;
import com.sun.jdi.InvalidTypeException;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import framework.parsing.Harness;
import java.io.IOException;
import java.util.HashMap;
import utility.Printing;
import utility.Statistics;

public class TestScratchPad {

  public static void main(String[] args)
      throws IOException, FileTypeException, InvalidTypeException {
    HashMap<String, String> options = parseOpts(args);

    Printing.set_debug_level(5);
    // Keep g for scope of calculation

    ArrayGraph g = new ArrayGraph(Harness.getFile(options), Harness.getFileType(options));
    ArrayGraph inverse = ArrayGraph.getInverseGraph(g);

    Statistics kadabra = KADABRACalculator.KADABRA(g, 0.001, 0.01, 1.0 / 100);

    Statistics brandes = BrandesCalculator.Brandes(g);
    System.out.println(brandes.getCentrality());
    System.out.println(kadabra.getCentrality());
  }
}
