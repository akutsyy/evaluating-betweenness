package data_processing;


import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class ValuesFromFile {
  public static double[] getValuesFromFile(String filename) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(filename));
    String line = reader.readLine(); // Maps stored as a single line - the .toString() output of a HashMap
    TIntDoubleMap map = new TIntDoubleHashMap(line.length()/20);
    line = line.replaceAll("[{}]","");
    String[] items = line.split(", ");
    for(String item:items){
      String[] components = item.split("=");
      int key = Integer.parseInt(components[0]);
      double value = Double.parseDouble(components[1]);
      map.put(key,value);
    }
    double[] values = new double[map.size()];
    for(int i=0;i<values.length;i++){
      values[i] = map.get(i);
    }
    return values;
  }
}
