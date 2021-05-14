package data_processing;

import static data_processing.NumberOfInversions.getOrder;
import static data_processing.ValuesFromFile.getValuesFromFile;

import gnu.trove.set.TIntSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.jgrapht.alg.util.Pair;
import utility.FileUtility;
import utility.Printing;

public class ProcessFiles {

    public static void main(String[] args) throws IOException {
        String[] shortGraphNames = {
                "4932-protein",
                "com-amazon",
                "slashdot0811",
                "as-caida20071105",
                "ca-astroph"
        };

        String[] groundTruthFiles = {
                "Brandes_0_4932-proteincentrality.txt",
                "Brandes_0_com-amazoncentrality.txt",
                "Brandes_0_slashdot0811centrality.txt",
                "Brandes_0_as-caida20071105centrality.txt",
                "Brandes_0_ca-astrophcentrality.txt"
        };

        double topPercentage = 0.01;

        String centralityDirectory = "centralities/";

        ArrayList<String> fileNames = getFileNamesInDirectory(centralityDirectory);

        for (int i = 0; i < shortGraphNames.length; i++) {
            System.out.println("Processing " + shortGraphNames[i]);
            double[] groundTruth = getValuesFromFile(centralityDirectory + groundTruthFiles[i]);
            int[] ordered = getOrder(groundTruth);
            int[] truthMap = NumberOfInversions.getTruthMap(groundTruth);
            TIntSet top = PercentOfTop.getTop(topPercentage, ordered);

            for (String fileName : fileNames) {

                String outputName = "statistics/" + fileName.split("centrality")[0] + "_statistics.txt";
                if (fileName.contains(shortGraphNames[i]) && !FileUtility.fileExists(outputName)) {
                    if (fileName.contains("BrandesSubset")) {

                        double[] estimate = getValuesFromFile(centralityDirectory + fileName);

                        double[] thisTruth = getValuesFromFile(centralityDirectory+fileName);
                        int[] thisOrdered = getOrder(thisTruth);
                        int[] thisTruthMap = NumberOfInversions.getTruthMap(thisTruth);
                        TIntSet thisTop = PercentOfTop.getTop(topPercentage, thisOrdered);
                        doTests(thisTruthMap, thisTop, thisTruth, estimate, outputName);
                    }

                    else if (fileName.contains("BrandesPP")) {
                        double[] estimate = getValuesFromFile(centralityDirectory + fileName);

                        String[] split = fileName.split("_");
                        String ending = split[split.length-1];
                        String start = "BrandesSubset";
                        String setSize = split[1];
                        String num = split[3];
                        double[] thisTruth = getValuesFromFile(centralityDirectory+start+"_"+setSize+"_"+num+"_"+ending);
                        int[] thisOrdered = getOrder(thisTruth);
                        int[] thisTruthMap = NumberOfInversions.getTruthMap(thisTruth);
                        TIntSet thisTop = PercentOfTop.getTop(topPercentage, thisOrdered);
                        doTests(thisTruthMap, thisTop, thisTruth, estimate, outputName);


                    } else {
                        System.out.println("Working on " + outputName);
                        double[] estimate = getValuesFromFile(centralityDirectory + fileName);
                        doTests(truthMap, top, groundTruth, estimate, outputName);
                    }
                }
            }
        }
    }

    private static void doTests(int[] truthMap, TIntSet top, double[] truth, double[] estimate, String fileName) {
        Pair<Double, Double> averageMax = AverageError.averageAndMaxErrorNormalized(truth, estimate);
        double averageError = averageMax.getFirst();
        double maxError = averageMax.getSecond();
        double percentOfTop = PercentOfTop.percentOfTop(top, estimate);
        double euclideanDistance = EuclideanDistance.getNormalizedEuclideanDistance(truth, estimate);
        double inversionPercent = NumberOfInversions.getPercentOfPossibleInversions(truthMap, estimate);
        String results = "averageError: " + averageError + "\n" +
                "maxError: " + maxError + "\n" +
                "percentOfTop: " + percentOfTop + "\n" +
                "euclideanDistance: " + euclideanDistance + "\n" +
                "inversionPercent: " + inversionPercent;
        Printing.printToFile(results, fileName);
    }

    public static ArrayList<String> getFileNamesInDirectory(String directory) {
        File folder = new File(directory);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null || listOfFiles.length == 0) {
            return null;
        }
        ArrayList<String> fileNames = new ArrayList<>();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                fileNames.add(file.getName());
            }
        }
        return fileNames;
    }
}
