package testing;

import static framework.parsing.Harness.parseOpts;

import algorithms.brandes.BrandesCalculator;
import algorithms.brandespich2007.BrandesPich2007;
import algorithms.geisberger.GeisbergerCalculator;
import algorithms.kadabra.KADABRACalculator;
import data_processing.AverageError;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import framework.parsing.Harness;
import java.io.IOException;
import java.util.HashMap;
import utility.Printing;
import utility.Statistics;

public class AccuracyTest {

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

    Statistics brandesRandom = BrandesPich2007.BrandesRandom(g, (int) (g.size()*0.01));

    end = System.nanoTime();

    System.out.println("Time taken for brandes random: " + (end - start) / Math.pow(10, 9));

    start = System.nanoTime();

    Statistics brandesCentrality = BrandesCalculator.Brandes(g);

    end = System.nanoTime();
    System.out.println("Time taken for brandes: " + (end - start) / Math.pow(10, 9));

    double avg = AverageError.averageAndMaxErrorNormalized(brandesCentrality.getCentralityArray(),kadabraCentrality.getCentralityArray()).getFirst();
    System.out.println("Average error for kadabra: " + avg);
    System.out.println("Normalized euclidean distance: " + CompareBetweenness.normalizedEuclideanDistance(brandesCentrality.getCentrality(), kadabraCentrality.getCentrality()));

    avg = AverageError.averageAndMaxErrorNormalized(brandesCentrality.getCentralityArray(),brandesRandom.getCentralityArray()).getFirst();;
    System.out.println("Average error for brandesRandom: " + avg);
    System.out.println("Normalized euclidean distance: " + CompareBetweenness.normalizedEuclideanDistance(brandesCentrality.getCentrality(), brandesRandom.getCentrality()));

  }
}
