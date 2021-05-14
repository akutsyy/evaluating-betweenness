package testing;

import data_processing.NumberOfInversions;
import java.io.IOException;

public class InversionsTest {
  public static void main(String[] args) throws IOException {
    double[] truth = {500,20,200,1,1000,2,-1};
    double[] estimate = {500,20,200,1,0,2,-1};
    int[] truthMap = NumberOfInversions.getTruthMap(truth);
    System.out.println(NumberOfInversions.getPortionOfPossibleInversions(truthMap,estimate)*(estimate.length*(estimate.length-1)/2.0));
  }
}
