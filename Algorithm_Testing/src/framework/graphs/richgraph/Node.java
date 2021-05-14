package framework.graphs.richgraph;


import framework.graphs.HasID;
import java.util.concurrent.atomic.AtomicInteger;

@Deprecated
public class Node implements HasID {

  public static Integer getNewID() {
    Node n = new Node();
    return n.getID();
  }

  private static final AtomicInteger ID = new AtomicInteger(0);


  protected int myID;

  public int getID() {
    return myID;
  }

  public void setID(int ID) {
    myID = ID;
  }


  public Node() {
    myID = Node.ID.getAndIncrement();
  }

  public Node(int ID) {
    myID = ID;

    // Set ID to the max of ID and its current value, atomically
    int witness;
    int current;
    do {
      current = Node.ID.get();
      witness = Node.ID.compareAndExchange(current, Math.max(current, ID));
    } while (witness != current);
  }

  public Node(Node other) {
    this.myID = other.myID;
  }

  @Override
  public int hashCode() {
    return myID;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Node) && ((Node) obj).getID() == this.getID();
  }

  @Override
  public String toString() {
    return Integer.toString(myID);
  }
}
