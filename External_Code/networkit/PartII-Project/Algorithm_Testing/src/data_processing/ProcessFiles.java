package data_processing;

import static data_processing.NumberOfInversions.getOrder;
import static data_processing.ValuesFromFile.getValuesFromFile;

import evaluation.EvaluationHarness;
import gnu.trove.set.TIntSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.jgrapht.alg.util.Pair;
import utility.FileUtility;
import utility.Printing;

public class ProcessFiles {

  public static void main(String[] args) throws IOException {
    String[] shortGraphNames = {
        "wiki-vote",
        "4932-protein",
        "com-amazon",
        "slashdot0811",
        "as-caida20071105"
    };

    boolean[] isDirected ={
        true,
        false,
        false,
        true,
        false
    };

    String[] groundTruthFiles = {
        "Brandes_0_wiki-votecentrality.txt",
        "Brandes_0_4932-proteincentrality.txt",
        "Brandes_0_com-amazoncentrality.txt",
        "Brandes_0_slashdot0811centrality.txt",
        "Brandes_0_as-caida20071105centrality.txt"
    };

    double topPercentage = 0.01;

    String centralityDirectory = "centralities/";

    ArrayList<String> fileNames = getFileNamesInDirectory(centralityDirectory);
    System.out.println(fileNames);

    for(int i=0;i<shortGraphNames.length;i++){
      System.out.println("Processing "+shortGraphNames[i]);
      double[] groundTruth = getValuesFromFile(centralityDirectory+groundTruthFiles[i]);
      int[] ordered = getOrder(groundTruth);
      int[] truthMap = NumberOfInversions.getTruthMap(groundTruth);
      TIntSet top = PercentOfTop.getTop(topPercentage,ordered);


      for(String fileName:fileNames){
        String outputName = "statistics/"+fileName.split("centrality")[0]+"_statistics.txt";

        if(fileName.contains(shortGraphNames[i]) && !FileUtility.fileExists(outputName)){
          System.out.println("Working on "+outputName);
          double[] estimate = getValuesFromFile(centralityDirectory+fileName);
          doTests(truthMap,top,groundTruth,estimate,outputName,isDirected[i]);
        }
      }
    }
  }

  private static void doTests(int[] truthMap, TIntSet top, double[] truth, double[] estimate, String fileName, boolean isdirected) {
    Pair<Double,Double> averageMax = AverageError.averageAndMaxErrorNormalized(truth,estimate,isdirected);
    double averageError = averageMax.getFirst();
    double maxError = averageMax.getSecond();
    double percentOfTop = PercentOfTop.percentOfTop(top,estimate);
    double euclideanDistance = EuclideanDistance.getNormalizedEuclideanDistance(truth,estimate);
    double inversionPercent = NumberOfInversions.getPortionOfPossibleInversions(truthMap,estimate);
    String results = "averageError: "+averageError+"\n"+
        "maxError: "+maxError+"\n"+
        "percentOfTop: "+percentOfTop+"\n"+
        "euclideanDistance: "+euclideanDistance+"\n"+
        "inversionPercent: "+inversionPercent;
    Printing.printToFile(results,fileName);
  }

  // Modified from https://stackoverflow.com/questions/5694385/getting-the-filenames-of-all-files-in-a-folder
  public static ArrayList<String> getFileNamesInDirectory(String directory){
    File folder = new File(directory);
    File[] listOfFiles = folder.listFiles();
    if(listOfFiles==null || listOfFiles.length==0) return null;
    ArrayList<String> fileNames = new ArrayList<>();

    for (File file : listOfFiles) {
      if (file.isFile()) {
        fileNames.add(file.getName());
      }
    }
    return fileNames;
  }
}
