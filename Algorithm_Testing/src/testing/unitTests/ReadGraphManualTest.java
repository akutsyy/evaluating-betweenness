package testing.unitTests;

import static framework.parsing.Harness.parseOpts;

import algorithm_evaluation.MainHarness;
import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileTypeException;
import java.io.IOException;
import java.util.HashMap;

public class ReadGraphManualTest {

  public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {
    String parameters = "--file datasets/toy2/toy2.txt --file_type Directed_ID_ID_List";
    HashMap<String, String> options = parseOpts(parameters);
    ArrayGraph g = MainHarness.getGraph(options);
    System.out.println(g);
  }
}
