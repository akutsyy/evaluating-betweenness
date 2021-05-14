package utility;

import framework.graphs.arraygraph.ArrayGraph;
import gnu.trove.map.TIntDoubleMap;
import java.util.HashMap;
import java.util.Map;

public class Statistics {

  private final Map<Integer, Double> centrality;
  private final Map<Integer, Integer> degree;
  private final Map<Integer, Long> time;
  private double[] partTimes;
  private boolean endedFromCutoff;
  private int numCentralities;

  public Statistics(double[] centrality, boolean endedFromCutoff) {
    this(centrality);
    this.endedFromCutoff =endedFromCutoff;
  }

  public Statistics(double[] centrality, long[] timeArray) {
    this(centrality);
    if (timeArray != null) {
      for (int i = 0; i < timeArray.length; i++) {
        long t = timeArray[i];
        this.time.put(i, t);
      }
    }
  }

  public Statistics(TIntDoubleMap centrality) {
    this.centrality = new HashMap<>();
    this.time = new HashMap<>();
    for (int i = 0; i < centrality.size(); i++) {
      double v = centrality.get(i);
      this.centrality.put(i, v);
    }
    degree = new HashMap<>();
  }

  public Statistics(double[] centrality) {
    this.centrality = new HashMap<>();
    this.time = new HashMap<>();
    for (int i = 0; i < centrality.length; i++) {
      double v = centrality[i];
      this.centrality.put(i, v);
    }
    degree = new HashMap<>();
  }

  public Statistics(double[] centralities, int numcentralitiesUsed, double partitionTime, double skeletonTime, double brandesTime, double frontierTime, double centralitiesTime) {
  this(centralities);
    this.numCentralities = numcentralitiesUsed;
    this.partTimes = new double[] {partitionTime,skeletonTime,brandesTime,frontierTime,centralitiesTime};

  }

  public void addDegree(ArrayGraph g) {
    for(int i=0;i<g.size();i++){
      degree.put(i,g.degree(i));
    }
  }

  public boolean endedFromCutoff(){
    return endedFromCutoff;
  }
  public int getNumCentralities(){
    return numCentralities;
  }


  public Map<Integer, Integer> getDegree() {
    return degree;
  }

  public Map<Integer, Long> getTime() {
    return time;
  }

  public boolean hasTime() {
    return !time.isEmpty();
  }

  public Map<Integer, Double> getCentrality() {
    return centrality;
  }


  public int getNonZeroCentralities() {
    int i = 0;
    for (Double d : centrality.values()) {
      if (d < MathUtils.EPSILON) {
        i++;
      }
    }
    return centrality.size() - i;
  }

  public double[] getPartTimes() {
    return partTimes;
  }
}
