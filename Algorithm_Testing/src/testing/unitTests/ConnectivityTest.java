package testing.unitTests;

import static framework.parsing.Harness.defaultPercent;
import static framework.parsing.Harness.getFileType;

import algorithms.brandes.BrandesCalculator;
import com.sun.jdi.InvalidTypeException;
import data_processing.NumberOfInversions;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import framework.parsing.Harness;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import testing.jgraphtValidation.JGraphTValidate;
import utility.Printing;
import utility.Statistics;

public class ConnectivityTest {

    public static void main(String[] args)
            throws IOException, FileTypeException, InvalidTypeException, InterruptedException {

        Printing.set_debug_level(5);

        final String[] graphParameters = {
                "--file datasets/final/4932.protein.links_undirected_weighted.csv --file_type Undirected_Weighted_CSV",
                "--file datasets/final/com-amazon_undirected.txt --file_type Undirected_ID_ID_List",
                "--file datasets/final/slashdot0811_directed.txt --file_type Directed_ID_ID_List",
                "--file datasets/final/as-caida20071105_undirected.txt --file_type Undirected_ID_ID_List",
                "--file datasets/final/ca-astroph_undirected.txt --file_type Undirected_ID_ID_List"};

        for(String p:graphParameters) {
            HashMap<String, String> options = Harness.parseOpts(p);
            System.out.println(p);
            ArrayGraph g = new ArrayGraph(Harness.getFile(options), getFileType(options));
            JGraphTValidate.findConnected(g);
        }
    }
}
