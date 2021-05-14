package framework.main;

import static framework.main.Harness.parseOpts;

import algorithms.brandes.BrandesCalculator;
import algorithms.brandespich2008.BrandesPich2008;
import algorithms.kadabra.KADABRACalculator;
import evaluation.CompareBetweenness;
import framework.graphs.arraygraph.ArrayGraph;
import java.io.IOException;
import java.util.HashMap;
import utility.Printing;
import utility.Statistics;

public class SimpleHarness {

  public static void main(String[] args)
          throws IOException, FileTypeException, InterruptedException {
    HashMap<String, String> options = parseOpts(args);

    Printing.set_debug_level(6);
    // Keep g for scope of calculation

    ArrayGraph g = new ArrayGraph(Harness.getFile(options), Harness.getFileType(options));
    System.out.println("initial size:" + g.size());

    System.gc();
    Thread.sleep(1000);
    long start = System.nanoTime();

    Statistics kadabraCentrality = KADABRACalculator.KADABRA(g, 0.01, 0.01, 1.0 / 100);
    long end = System.nanoTime();
    System.out.println("Time taken for KADABRA: " + (end - start) / Math.pow(10, 9));

    start = System.nanoTime();

    Statistics brandesRandom = BrandesPich2008.BrandesRandom(g, (int) (g.size()*0.01));

    end = System.nanoTime();

    System.out.println("Time taken for brandes random: " + (end - start) / Math.pow(10, 9));

    start = System.nanoTime();

    Statistics brandesCentrality = BrandesCalculator.Brandes(g);

    end = System.nanoTime();
    System.out.println("Time taken for brandes: " + (end - start) / Math.pow(10, 9));

    double avg = 0;
    double tot = 0;
    for (int i = 0; i < brandesCentrality.getCentrality().size(); i++) {
      if (brandesCentrality.getCentrality().get(i) > 100) { //TODO
        avg += brandesCentrality.getCentrality().get(i) * (
            kadabraCentrality.getCentrality().get(i) / brandesCentrality.getCentrality().get(i));
        tot += brandesCentrality.getCentrality().get(i);
      }
    }
    avg /= tot;
    System.out.println("Average bias ratio for kadabra: " + avg);
    System.out.println("Normalized euclidean distance: " + CompareBetweenness.normalizedEuclideanDistance(brandesCentrality.getCentrality(), kadabraCentrality.getCentrality()));

    avg = 0;
    tot = 0;
    for (int i = 0; i < brandesCentrality.getCentrality().size(); i++) {
      if (brandesCentrality.getCentrality().get(i) > 100) { //TODO undo
        avg += brandesCentrality.getCentrality().get(i) * (
            brandesRandom.getCentrality().get(i) / brandesCentrality.getCentrality().get(i));
        tot += brandesCentrality.getCentrality().get(i);
      }
    }
    avg /= tot;
    System.out.println("Average bias ratio for brandesRandom: " + avg);
    System.out.println("Normalized euclidean distance: " + CompareBetweenness.normalizedEuclideanDistance(brandesCentrality.getCentrality(), brandesRandom.getCentrality()));

  }
}
