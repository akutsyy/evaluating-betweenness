package evaluation;

import algorithms.brandes.BrandesSubsetCalculator;
import algorithms.brandespp.BrandesPPCalculator;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileTypeException;
import gnu.trove.set.TIntSet;
import utility.Printing;
import utility.Statistics;

import java.io.IOException;
import java.util.HashMap;

import static evaluation.EvaluationHarness.*;
import static evaluation.GraphAccessHarness.printGraphAccesses;
import static framework.main.Harness.parseOpts;

public class SubsetEvaluationHarness {
    public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {
        Printing.set_debug_level(3);
        final int numberOfIterations = 3;

        final String[] graphParameters = {
                "--file datasets/final/wiki-vote_directed.txt --file_type Directed_ID_ID_List",
                "--file datasets/final/4932.protein.links_undirected_weighted.csv --file_type Undirected_Weighted_CSV",
                "--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List",
                "--file datasets/final/slashdot0811_directed.txt --file_type Directed_ID_ID_List"};

        final String[] shortGraphNames = {
                "wiki-vote",
                "4932-protein",
                "as-caida20071105",
                "slashdot0811"
        };

        final double[] setPercentages = {0.001, 0.01, 0.05, 0.1, 0.5, 0.8};
        final int[] numPartitions = {2, 8, 32, 128, 512, 2048};


        warmup("--file datasets/final/wiki-vote_directed.txt --file_type Directed_ID_ID_List");

        for (int graphNum = 0; graphNum < graphParameters.length; graphNum++) {
            String parameter = graphParameters[graphNum];
            HashMap<String, String> options = parseOpts(parameter);
            ArrayGraph g;
            Statistics s;
            String name;

            long start;
            long end;


            for (int i = 0; i < numberOfIterations; i++) {
                for (double p : setPercentages) {
                    TIntSet set = BrandesSubsetCalculator.generateSources(getGraph(options), 0.01);
                    name = "BrandesSubset" + p + "_" + i + "_" + shortGraphNames[graphNum];
                    if (!outputExists(name)) {
                        g = getGraph(options);
                        ArrayGraph.GraphAccesses = 0;
                        start = System.nanoTime();
                        s = BrandesSubsetCalculator.BrandesSubset(g, set);
                        end = System.nanoTime();
                        printInfo("BrandesSubset", "Set Percentage=" + p + "'\nSet Size=" + set.size(),
                                options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.GraphAccesses, name);
                        printGraphAccesses("BrandesSubset","Set Percentage=" + p + "'\nSet Size=" + set.size(),options.get("file"),ArrayGraph.GraphAccesses,name);
                        printCentrality(s, name);
                    }

                    for (int partitions : numPartitions) {
                        name = "BrandesPP" + p + "_" + partitions + "_" + i + "_" + shortGraphNames[graphNum];
                        if (!outputExists(name)) {
                            g = getGraph(options);
                            ArrayGraph.GraphAccesses = 0;
                            start = System.nanoTime();
                            s = BrandesPPCalculator.brandesPP(g, set, partitions);
                            end = System.nanoTime();
                            printInfo("BrandesPP", "Set Percentage=" + p + "'\nSet Size=" + set.size() + "\nNumPartitions=" + partitions,
                                    options.get("file"), g.size(), g.edgeSize(), (end - start) / Math.pow(10, 9), ArrayGraph.GraphAccesses, name);
                            printGraphAccesses("BrandesPP", "Set Percentage=" + p + "'\nSet Size=" + set.size() + "\nNumPartitions=" + partitions,
                                    options.get("file"), ArrayGraph.GraphAccesses, name);
                            printCentrality(s, name);
                        }
                    }
                }
            }
        }
    }
}
