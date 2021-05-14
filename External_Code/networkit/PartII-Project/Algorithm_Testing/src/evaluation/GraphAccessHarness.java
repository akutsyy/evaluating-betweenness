package evaluation;

import static evaluation.EvaluationHarness.getGraph;
import static framework.main.Harness.parseOpts;
import static utility.Printing.printToFile;
import static utility.Printing.print_debug;

import algorithms.brandes.BrandesCalculator;
import algorithms.brandespich2008.BrandesPich2008;
import algorithms.geisberger.GeisbergerCalculator;
import algorithms.kadabra.KADABRACalculator;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileTypeException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import utility.Printing;

public class GraphAccessHarness {

    public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {
        Printing.set_debug_level(3);
        final int numberOfIterations = 3;

        final String[] graphParameters = {
                "--file datasets/final/wiki-vote_directed.txt --file_type Directed_ID_ID_List",
                "--file datasets/final/4932.protein.links_undirected_weighted.csv --file_type Undirected_Weighted_CSV",
                "--file datasets/final/com-amazon_undirected.txt --file_type Undirected_ID_ID_List",
                "--file datasets/final/slashdot0811_directed.txt --file_type Directed_ID_ID_List",
                "--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List"};

        final String[] shortGraphNames = {
                "wiki-vote",
                "4932-protein",
                "com-amazon",
                "slashdot0811",
                "as-caida20071105"
        };

        final int[] pivots = {
                25, 50, 100, 200, 400, 800, 1600, 3200
        };

        final int[] geisbergerSamples = {1, 2, 4, 8, 16};
        final double[] kadabraLambdas = {0.01, 0.025, 0.02, 0.015, 0.01, 0.005};
        final double kadabraDelta = 0.1;


        for (int graphNum = 0; graphNum < graphParameters.length; graphNum++) {
            String parameter = graphParameters[graphNum];
            HashMap<String, String> options = parseOpts(parameter);
            ArrayGraph g;
            String name = "Brandes_" + 0 + "_" + shortGraphNames[graphNum];

            // Only do Brandes once
            if (!outputAccessesExists(name)) {
                System.out.println("Doing Brandes");
                // Brandes
                g = getGraph(options);
                ArrayGraph.GraphAccesses = 0;
                // Only do Brandes once on very large graphs
                BrandesCalculator.Brandes(g);
                printGraphAccesses("Brandes", "None", options.get("file"), ArrayGraph.GraphAccesses, name);
            }

            for (int i = 0; i < numberOfIterations; i++) {
                // Brandes and Pich (2008)
                for (int p : pivots) {
                    name = "BrandesAndPich2008_" + p + "_" + i + "_" + shortGraphNames[graphNum];
                    if (!outputAccessesExists(name)) {
                        g = getGraph(options);
                        ArrayGraph.GraphAccesses = 0;
                        BrandesPich2008.BrandesRandom(g, p);
                        printGraphAccesses("BrandesAndPich2008", "pivots=" + p, options.get("file"), ArrayGraph.GraphAccesses, name);
                    }
                }

                // Geisberger et al. Linear
                for (int p : pivots) {
                    name = "GeisbergerLinear" + p + "_" + i + "_" + shortGraphNames[graphNum];
                    if (!outputAccessesExists(name)) {
                        g = getGraph(options);
                        ArrayGraph.GraphAccesses = 0;
                        GeisbergerCalculator.GeisbergerLinear(g, p);
                        printGraphAccesses("GeisbergerLinear", "pivots=" + p, options.get("file"), ArrayGraph.GraphAccesses, name);
                    }
                }

                // Geisberger et al. Bisection
                for (int p : pivots) {
                    name = "GeisbergerBisection" + p + "_" + i + "_" + shortGraphNames[graphNum];
                    if (!outputAccessesExists(name)) {
                        g = getGraph(options);
                        ArrayGraph.GraphAccesses = 0;
                        GeisbergerCalculator.GeisbergerBisection(g, p);
                        printGraphAccesses("GeisbergerBisection", "pivots=" + p, options.get("file"), ArrayGraph.GraphAccesses, name);
                    }
                }

                // Geisberger et al. Bisection Sampled
                for (int p : pivots) {
                    for (int sampleNum : geisbergerSamples) {
                        name = "GeisbergerBisectionSampled" + p + "_" + sampleNum + "_" + i + "_" + shortGraphNames[graphNum];
                        if (!outputAccessesExists(name)) {
                            ArrayGraph.GraphAccesses = 0;
                            g = getGraph(options);
                            GeisbergerCalculator.GeisbergerBisectionSampling(g, p, sampleNum);
                            printGraphAccesses("GeisbergerBisectionSampled", "pivots=" + p + ", samples=" + sampleNum, options.get("file"), ArrayGraph.GraphAccesses, name);
                        }
                    }
                }
                // KADABRA
                for (double lambda : kadabraLambdas) {
                    name = "KADABRA_" + lambda + "_" + kadabraDelta + "_" + i + "_" + shortGraphNames[graphNum];
                    if (!outputAccessesExists(name)) {
                        g = getGraph(options);
                        ArrayGraph.GraphAccesses = 0;
                        KADABRACalculator.KADABRA(g, lambda, kadabraDelta);
                        printGraphAccesses("KADABRA", "lambda=" + lambda + ", delta=" + kadabraDelta, options.get("file"), ArrayGraph.GraphAccesses, name);
                    }
                }
            }
        }
    }

    private static boolean outputAccessesExists(String uniqueName) {
        File f = new File("accesses/" + uniqueName + "_accesses.txt");
        return f.exists() && !f.isDirectory();
    }

    public static void printGraphAccesses(String algorithm, String parameters, String name, long graphAccesses, String uniqueName) {
        print_debug(2, "Completed " + uniqueName + "with accessses: " + graphAccesses);
        printToFile("Running " + algorithm + " with parameters " + parameters + " on: " + name + "\nGraph accesses: " + graphAccesses, "accesses/" + uniqueName + "_accesses.txt");
    }
}
