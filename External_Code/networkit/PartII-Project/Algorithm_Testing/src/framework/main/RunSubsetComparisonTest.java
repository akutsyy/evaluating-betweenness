package framework.main;

import algorithms.brandes.BrandesSubsetCalculator;
import algorithms.brandespp.BrandesPPCalculator;
import algorithms.brandespp.metis.Metis;
import evaluation.EvaluationHarness;
import framework.graphs.arraygraph.ArrayGraph;
import framework.main.FileTypeException;
import framework.main.Harness;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import utility.Statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static utility.Printing.printToFile;

public class RunSubsetComparisonTest {
    public static void main(String[] args) throws InterruptedException, FileTypeException, IOException {
        HashMap<String, String> options = Harness.parseOpts(args);
        ArrayGraph g = EvaluationHarness.getGraph(options);
        String graphName = options.get("file").split("/")[options.get("file").split("/").length-1].split("\\.")[0];
        System.out.println(graphName);
        TIntSet targets = BrandesSubsetCalculator.generateSources(g,0.01);
        printTargets(targets,"sources.txt");
        ArrayList<TIntSet> partitions = Metis.metisToPartitions(g,100,3,5,100);
        printPartitions(partitions,"clusterfile.txt");
        System.gc();
        Thread.sleep(5000);
        long start = System.nanoTime();
        Statistics s = BrandesSubsetCalculator.BrandesSubset(g,targets);
        long end = System.nanoTime();
        printToFile("File: "+options.get("file")+"\n Time: "+(end-start)/Math.pow(10,9),"evaluation/brandesPPData/descriptions/BrandesSubset_"+graphName+".txt");
        printToFile(s.toString(),"evaluation/brandesPPData/centralities/BrandesSubset_"+graphName+".txt");
        System.gc();
        Thread.sleep(5000);
        start = System.nanoTime();
        s = BrandesPPCalculator.brandesPP(g,targets,partitions);
        end = System.nanoTime();
        printToFile("File: "+options.get("file")+"\n Time: "+(end-start)/Math.pow(10,9),"evaluation/brandesPPData/descriptions/BrandesPP"+graphName+".txt");
        printToFile(s.toString(),"evaluation/brandesPPData/centralities/BrandesPP"+graphName+".txt");

    }

    // Prints partitions in format acceptable by Erdos Brandes++ implementation
    private static void printPartitions(ArrayList<TIntSet> partitions, String filename) {
        TIntIntMap map = new TIntIntHashMap();

        for (int i = 0; i < partitions.size(); i++) {
            TIntIterator it = partitions.get(i).iterator();
            while (it.hasNext()) {
                map.put(it.next(),i);
            }
        }
        StringBuilder s = new StringBuilder();
        for (int i = 0;i<map.size();i++) {
            if (!s.toString().equals("")) {
                s.append("\n");
            }
            s.append(map.get(i));
        }
        printToFile(s.toString(),filename);
    }

    // Prints targets in the fashion accepted by the Erdos et al. implementation of BrandesSubset and Brandes++
    private static void printTargets(TIntSet targets,String filename){
        StringBuilder s = new StringBuilder();
        TIntIterator it = targets.iterator();
        while (it.hasNext()){
            int i = it.next();
            s.append(i);
            if(it.hasNext()){
                s.append("\t");
            }
        }
        printToFile(s.toString(),filename);
    }
}
