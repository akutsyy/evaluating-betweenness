package testing;

import evaluation.EvaluationHarness;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileTypeException;
import utility.Printing;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import static framework.main.Harness.parseOpts;

public class CorrectGraphParsingTest {
    public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {

        Printing.set_debug_level(3);

        final String[] graphParameters = {
                "--file datasets/final/wiki-vote_directed.txt --file_type Directed_ID_ID_List",
                "--file datasets/final/4932.protein.links_undirected_weighted.csv --file_type Undirected_Weighted_CSV",
                "--file datasets/final/com-amazon_undirected.txt --file_type Undirected_ID_ID_List",
                "--file datasets/final/slashdot0811_directed.txt --file_type Directed_ID_ID_List",
                "--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List"};
        for (String parameter:graphParameters){
            HashMap<String, String> options = parseOpts(parameter);
            ArrayGraph g = EvaluationHarness.getGraph(options);

            System.out.println("Graph: "+options.get("file"));
            System.out.println("Directed: "+g.isDirected());
            System.out.println("Weighted: "+g.isWeighted());
            System.out.println("Total edge size: "+g.edgeSize());
            System.out.println("Zero weights: "+(Arrays.stream(g.weights).filter(v -> v == 0.).count()-(g.weights.length-g.edgeSize())));
            System.out.println("One weights: "+Arrays.stream(g.weights).filter(v -> v == 1.).count());
            System.out.println("Two weights: "+Arrays.stream(g.weights).filter(v -> v == 2.).count());
            System.out.println("Other weights: "+Arrays.stream(g.weights).filter(v -> v != 0 && v != 1. && v != 2.).count());
            System.out.println();

        }
    }
}
