package framework.graphs;

import framework.graphs.arraygraph.ArrayGraph;
import framework.parsing.FileType;
import java.io.File;

public abstract class Graph {

  protected void noteAccess() {
    ArrayGraph.EdgeTraversals++;
  }


  public boolean isWeighted() {
    return isWeighted;
  }

  public boolean isDirected() {
    return isDirected;
  }

  protected boolean isWeighted;
  protected boolean isDirected;

  public Graph(File graph_file, FileType type) {
    this.isWeighted = type.isWeighted();
    this.isDirected = type.isDirected();
  }

  protected Graph(boolean isWeighted, boolean isDirected) {
    this.isWeighted = isWeighted;
    this.isDirected = isDirected;

  }

  public abstract void normalizeIDs();
}
