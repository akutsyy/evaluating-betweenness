package algorithms.kadabra;

import static utility.GraphStatistics.approximateVertexDiameter;
import static utility.Printing.print_debug;

import algorithms.sssp.BBBFS;
import algorithms.sssp.BBDjikstra;
import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import java.util.Random;
import java.util.function.DoubleFunction;
import utility.BinarySearch;
import utility.Printing;
import utility.ProgressPrinter;
import utility.Statistics;

public class KADABRACalculator {

  public static Statistics KADABRA(ArrayGraph g, double lambda, double delta) {
    return KADABRA(g, lambda, delta, 1 / 100.0); // As recommended in the paper
  }

  public static Statistics KADABRA(ArrayGraph g, double lambda, double delta, double delta_calculation_percentage) {
    double[] centrality = new double[g.size()]; // betweeness result
    ArrayGraph inverse = ArrayGraph.getInverseGraph(g);
    double c = 0.5; // Universal constant, estimated in "M. Löffler and J. M. Phillips. Shape fitting on point sets withprobability distributions. ESA’09, 200"

    print_debug(5, "Computing vertex diameter");
    // Slight modifications to w in line with their code
    double log_2VertexDiameter = Math.floor(Math.log(approximateVertexDiameter(g, inverse, 5) - 1) / Math.log(2));
    double w = (c / (lambda * lambda)) * (log_2VertexDiameter + 1 + Math.log(0.5 / delta));

    double[] deltas = computeSimpleDeltas(g, inverse, w, lambda, delta, delta_calculation_percentage);

    int t = 0;

    print_debug(5, "Beginning Sampling, max end condition " + w);
    ProgressPrinter p = new ProgressPrinter((int) w);
    boolean stop = false;
    //long timeOnStopCondition = 0;
    print_debug(0, "Beginning main sequence:");
    while (!stop && t < w) {
      p.print(t);
      for (int i = 0; i <= 10; i++) {//As in their code, to reduce checking overhead
        TIntSet path = samplePath(g, inverse);
        TIntIterator it = path.iterator();
        while (it.hasNext()) {
          int v = it.next();
          centrality[v]++;
        }
        t++;
      }
      //long start = System.nanoTime();
      stop = haveToStopSimple(centrality, deltas, w, t, lambda);
      //long end = System.nanoTime();
      //timeOnStopCondition += end-start;
    }

    print_debug(2, "Iterated for " + t + " iterations, stopped due to " + ((t < w) ? "haveToStop" : "max iterations terminator"));
    for (int v = 0; v < g.size(); v++) {
      centrality[v] /= t; // Extrapolate
      centrality[v] *= (g.size() - 1) * (g.size() - 2); // de-normalize
      // Halve for undirected graphs
      if (!g.isDirected()) {
        centrality[v] /= 2.0;
      }
    }

    return new Statistics(centrality, t, (t >= w));
  }


  private static boolean haveToStopSimple(double[] centrality, double[] delta_l, double w, double t, double lambda) {
    // Here, delta_l = delta_u
    // If any v is unfinished, don't stop
    // Here, ∀ v ∈ V, lambda_l(v) = lambda_u(v) = lambda
    boolean allFinished = true;
    for (int i = 0; i < centrality.length && allFinished; i++) {
      boolean finished = f(centrality[i] / t, delta_l[i], w, t) < lambda;
      allFinished = allFinished && finished;
    }

    return allFinished;
  }

  private static double f(double centrality, double delta, double w, double t) {
    double log_d = Math.log(1 / delta);
    double sqrtTerm = Math.sqrt(Math.pow(1 / 3.0 - w / t, 2) + 2 * centrality * w / (log_d));
    double parenthTerm = 1 / 3.0 - w / t + sqrtTerm;
    return Math.min(centrality, log_d * parenthTerm / t);
  }


  private static double[] computeSimpleDeltas(ArrayGraph g, ArrayGraph inverse, double w, double lambda, double delta, double delta_calculation_percentage) {
    // Numbered as in Algorithm 2 in KADABRA paper
    int alpha = (int) Math.ceil(w * delta_calculation_percentage); // 1
    double epsilon = 0.00001; // 2

    int[] b = new int[g.size()]; // centralities

    double[] d_l = new double[g.size()];

    print_debug(0, "Computing deltas: ");
    ProgressPrinter p = new ProgressPrinter(alpha);
    for (int i = 0; i < alpha; i++) { // 3
      p.print(i);
      // 4
      TIntSet path = samplePath(g, inverse);
      path.forEach(i1 -> {
        b[i1]++;
        return true;
      }); // 5
    } // 6
    for (int i = 0; i < g.size(); i++) { // 7
      b[i] = b[i] / alpha; //8
      // 9 and 10 integrated into 12
    } // 11

    double max = 1 / (lambda * lambda) * Math.log(g.size() * 4 * (1 - epsilon) / delta);
    double C = -1 * BinarySearch.binarySearch(new DoubleFunction<Double>() {
                                                @Override
                                                public Double apply(double value) {
                                                  double sum = 0;
                                                  for (int i = 0; i < g.size(); i++) {
                                                    sum += Math.exp(-value * lambda * lambda / b[i]) * 2;
                                                  }
                                                  return sum;
                                                }
                                              }, -max, //min
        0, //max
        (delta / 2) * (1 - epsilon), //target
        lambda / 10);// permissible error    //12

    Printing.print_debug(5, "Selected Constant C to be " + C);

    for (int v = 0; v < g.size(); v++) {//13
      d_l[v] = Math.exp(-C * lambda * lambda / b[v]) + epsilon * delta / (4 * g.size());//14
    }//16

    return d_l;//17
  }


  private static TIntSet samplePath(ArrayGraph g, ArrayGraph inverse) {
    Random r = new Random();
    int s = r.nextInt(g.size());
    int t = r.nextInt(g.size());
    if (g.isWeighted()) {
      return BBDjikstra.findRandomShortestPath(g, inverse, s, t);
    }
    return BBBFS.findRandomShortestPath(g, inverse, s, t);
  }
}
