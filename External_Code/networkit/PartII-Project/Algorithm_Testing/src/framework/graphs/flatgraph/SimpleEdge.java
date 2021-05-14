package framework.graphs.flatgraph;

import java.util.concurrent.atomic.AtomicInteger;
@Deprecated
public class SimpleEdge {

  protected int from;
  protected int to;
  protected double weight = 1;

  static AtomicInteger ID = new AtomicInteger(0);

  protected int myID;

  public int getID() {
    return myID;
  }

  public void setID(int ID) {
    myID = ID;
  }

  public SimpleEdge(int from, int to) {
    this.from = from;
    this.to = to;
    myID = ID.getAndIncrement();
  }

  public SimpleEdge(int from, int to, double weight) {
    this(from, to);
    this.weight = weight;
  }

  public int getFrom() {
    return from;
  }

  public int getTo() {
    return to;
  }

  public double getWeight() {
    return weight;
  }


  // Uniquely combine two 32 bit integers
  @Override
  public int hashCode() {
    return Long.hashCode(this.getID());
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof SimpleEdge && ((SimpleEdge) obj).getID() == this.getID();
  }

  @Override
  public String toString() {
    return "(" + from + "-->" + to + ", " + String.format("%.2f", weight) + ")";
  }
}
