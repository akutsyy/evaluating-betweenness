package evaluation;

import framework.main.FileTypeException;

import java.io.IOException;

public class RunAllTests {
    public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {
        EvaluationHarness.runMainTests(args);
        BaderEvauationHarness.runBaderTests(args);
        SubsetEvaluationHarness.runSubSetTests(args);
    }
}
