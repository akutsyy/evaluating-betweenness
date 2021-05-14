package framework.graphs.richgraph;


import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public class Edge {

  protected RichNode to;


  protected double weight = 1;

  static AtomicInteger ID = new AtomicInteger(0);

  protected int myID;

  public int getID() {
    return myID;
  }

  public void setID(int ID) {
    myID = ID;
  }

  public double getWeight() {
    return weight;
  }


  public Edge(RichNode to) {
    this.to = to;
    myID = ID.getAndIncrement();
  }

  public Edge(RichNode to, double weight) {
    this(to);
    this.weight = weight;
  }


  public RichNode getTo() {
    return to;
  }


  @Override
  public int hashCode() {
    return Long.hashCode(this.getID());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Node) {
      return obj.equals(getTo());
    }
    return obj instanceof Edge && ((Edge) obj).getID() == this.getID();
  }

  @Override
  public String toString() {
    return "(" + to + ", " + String.format("%.2f", weight) + ")";
  }

}
