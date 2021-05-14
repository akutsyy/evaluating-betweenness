package algorithm_evaluation;

import framework.parsing.FileTypeException;
import java.io.IOException;

public class RunAllEvaluations {

  public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {
    MainHarness.runMainTests(args);
    BaderEvauationHarness.runBaderTests(args);
    SubsetEvaluationHarness.runSubSetTests(args);
  }
}
