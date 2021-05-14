package evaluation;

import algorithms.bader.BaderCalculator;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileTypeException;
import utility.Printing;
import utility.Statistics;

import java.io.IOException;
import java.util.HashMap;

import static data_processing.NumberOfInversions.getOrder;
import static data_processing.ValuesFromFile.getValuesFromFile;
import static evaluation.EvaluationHarness.*;
import static evaluation.GraphAccessHarness.printGraphAccesses;
import static framework.main.Harness.parseOpts;
import static utility.RandomHelper.getInPercentile;

public class BaderEvauationHarness {
    public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {
        Printing.set_debug_level(3);
        final int numberOfIterations = 10;

        final String[] graphParameters = {
                "--file datasets/final/wiki-vote_directed.txt --file_type Directed_ID_ID_List",
                "--file datasets/final/4932.protein.links_undirected_weighted.csv --file_type Undirected_Weighted_CSV",
                "--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List",
                "--file datasets/final/slashdot0811_directed.txt --file_type Directed_ID_ID_List",
                "--file datasets/final/com-amazon_undirected.txt --file_type Undirected_ID_ID_List"};

        final String[] shortGraphNames = {
                "wiki-vote",
                "4932-protein",
                "as-caida20071105",
                "slashdot0811",
                "com-amazon"
        };

        final String[] groundTruthFiles = {
                "Brandes_0_wiki-votecentrality.txt",
                "Brandes_0_4932-proteincentrality.txt",
                "Brandes_0_as-caida20071105centrality.txt",
                "Brandes_0_slashdot0811centrality.txt",
                "Brandes_0_com-amazoncentrality.txt"
        };

        final String centralityDirectory = "centralities/";

        final double[] lowPercentile = {0, 0, 0, 0};
        final  double[] highPercentile = {0.01, 0.1, 0.2, 0.001};

        final int[] alphas = {2,5};

        warmup("--file datasets/final/wiki-vote_directed.txt --file_type Directed_ID_ID_List");

        for (int i = 0; i < numberOfIterations; i++) {
            for(int alpha:alphas) {
                for (int graphNum = 0; graphNum < graphParameters.length - 1; graphNum++) {
                    doIteration(graphParameters[graphNum], centralityDirectory, groundTruthFiles[graphNum],
                            lowPercentile, highPercentile, i, shortGraphNames[graphNum],alpha);
                }
            }
        }
        // Amazon graph
        for (int i = 0; i < numberOfIterations; i++) {
            for(int alpha:alphas) {
                int graphNum = graphParameters.length - 1;
                doIteration(graphParameters[graphNum], centralityDirectory, groundTruthFiles[graphNum],
                        lowPercentile, highPercentile, i, shortGraphNames[graphNum],alpha);
            }
        }
    }

    private static void doIteration(String parameter, String centralityDirectory, String groundTruthFile,
                                    double[] lowPercentile, double[] highPercentile, int i, String shortGraphName, int alpha)
            throws IOException, FileTypeException, InterruptedException {
        HashMap<String, String> options = parseOpts(parameter);
        int[] ordered = getOrder(getValuesFromFile(centralityDirectory + groundTruthFile));
        ArrayGraph g;
        Statistics s;
        String name;

        long start;
        long end;


        // Bader et al.
        for (int j = 0; j < lowPercentile.length; j++) {
            double low = lowPercentile[j];
            double high = highPercentile[j];
            name = "Bader" + low + "_" + high + "_" + i + "_" + shortGraphName;
            if (!outputExists(name)) {
                int vertex = getInPercentile(ordered, low, high);
                g = getGraph(options);
                ArrayGraph.GraphAccesses = 0;
                start = System.nanoTime();
                s = BaderCalculator.Bader(g, vertex, alpha);
                end = System.nanoTime();
                printInfo("Bader", "Low=" + low + "'\nHigh=" + high + "\nTerminated from max iterations cutoff:" + s.endedFromCutoff(),
                        options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.GraphAccesses, name);
                printGraphAccesses("Bader", "Low=" + low + "'\nHigh=" + high,
                        options.get("file"), ArrayGraph.GraphAccesses, name);
                printCentrality(s, name);
            }
        }
    }
}
