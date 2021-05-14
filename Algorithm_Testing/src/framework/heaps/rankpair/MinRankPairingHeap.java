package framework.heaps.rankpair;


import framework.heaps.MinPriorityHeap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.util.LinkedList;
import java.util.List;


@SuppressWarnings("SuspiciousNameCombination")
public class MinRankPairingHeap implements MinPriorityHeap {

  class RankPairingNode {

    RankPairingNode left;
    RankPairingNode parent;
    RankPairingNode right; // can be leftmost sibling if last
    int rank;
    int element;

    public RankPairingNode(int element, int rank) {
      this.element = element;
      this.rank = rank;
    }

    double value() {
      return value[element];
    }

    @Override
    public String toString() {
      return "(" + element + ":" + value() + ",r:" + rank + ")" + "[p:" + (parent == null ? "null"
          : parent.element) + ",l:" + (left == null ? "null" : left.element) + ",r:" + (
          right == null ? "null" : right.element) + "]";
    }
  }

  // Beginning of PairingHeap
  private final double[] value;
  private LinkedList<RankPairingNode> nodes = new LinkedList<>();
  TIntObjectMap<RankPairingNode> pointerMap;

  public MinRankPairingHeap(int size, double[] value) {
    pointerMap = new TIntObjectHashMap<>(size);
    this.value = value;
  }


  @Override
  public int peek() {
    return nodes.get(0).element;
  }

  @Override
  public boolean isEmpty() {
    return nodes.isEmpty();
  }

  @Override
  public void insert(int elem) {
    if (pointerMap.containsKey(elem)) {
      throw new RuntimeException("Element already in heap: " + elem);
    }
    RankPairingNode newNode = new RankPairingNode(elem, 0);
    pointerMap.put(elem, newNode);

    addToList(nodes, newNode);
  }

  @Override
  public int pop() {
    LinkedList<RankPairingNode> newNodes = new LinkedList<>();
    RankPairingNode oldRoot = nodes.get(0);
    nodes.remove(0);
    pointerMap.remove(oldRoot.element);

    RankPairingNode[] buckets = new RankPairingNode[oldRoot.rank + 1];
    RankPairingNode currNode = oldRoot.left;
    RankPairingNode child;

    while (currNode != null) {
      currNode.parent = null;
      // Unlink
      child = currNode.right;
      currNode.right = null;

      // Process
      buckets = addToBuckets(currNode, buckets, newNodes);
      // Iterate
      currNode = child;
    }

    // Process remainders
    for (RankPairingNode n : nodes) {
      // Process
      buckets = addToBuckets(n, buckets, newNodes);
    }

    // Flush all from buckets
    for (RankPairingNode n : buckets) {
      if (n != null) {
        addToList(newNodes, n);
      }
    }
    nodes = newNodes;

    return oldRoot.element;
  }

  private static RankPairingNode[] addToBuckets(RankPairingNode newNode, RankPairingNode[] buckets,
      List<RankPairingNode> newNodeList) {
    if (buckets.length <= newNode.rank) {
      buckets = doubleSize(buckets, newNode.rank);
    }
    if (buckets[newNode.rank] == null) {
      buckets[newNode.rank] = newNode;
    } else {
      addToList(newNodeList, merge(newNode, buckets[newNode.rank]));
      buckets[newNode.rank] = null;
    }
    return buckets;
  }

  private static RankPairingNode merge(RankPairingNode a, RankPairingNode b) {
    if (b == null) {
      return a;
    }
    if (a == null) {
      return b;
    }
    RankPairingNode less;
    RankPairingNode more;
    if (a.value() < b.value()) {
      less = a;
      more = b;
    } else {
      less = b;
      more = a;
    }
    more.right = less.left; // Move old left node
    if (more.right != null) {
      more.right.parent = more;
    }
    less.left = more;
    more.parent = less;
    return less;
  }

  private static void addToList(List<RankPairingNode> list, RankPairingNode newNode) {
    if (list.isEmpty() || newNode.value() < list.get(0).value()) {
      list.add(0, newNode);
    } else {
      list.add(1, newNode);
    }
  }

  @Override
  public void insertOrDecrease(int element) {
    if (pointerMap.containsKey(element)) {
      decreaseKey(element);
    } else {
      insert(element);
    }
  }


  @Override
  public void remove(int element) {
    double oldVal = value[element];
    value[element] = Double.NEGATIVE_INFINITY;
    decreaseKey(element);
    pop();
    value[element] = oldVal;
  }

  @Override
  public void decreaseKey(int element) {
    RankPairingNode x = pointerMap.get(element);

    // x is not root
    if (x.parent != null) {
      RankPairingNode y = x.right;
      x.right = null;
      if (y != null) {
        y.parent = null;
      }

      replaceNode(x, y);
      fixAncestorRanks(x.parent);
      fixRankOfNode(x);

      x.parent = null;
      addToList(nodes, x);
    } else {
      if (x.value() < value[peek()]) {
        nodes.remove(x);
        nodes.add(0, x);
      }
    }
  }

  private static void replaceNode(RankPairingNode x, RankPairingNode y) {
    if (y != null) {
      y.parent = x.parent;
    }
    if (x.parent != null) {
      if (x.parent.left != null && x.parent.left == x) {
        x.parent.left = y;
      } else {
        x.parent.right = y;
      }
    }
  }

  private static void fixRankOfNode(RankPairingNode x) {
    // Fix rank of x
    if (x.left != null) {
      x.rank = x.left.rank + 1;
    } else {
      x.rank = 0;
    }
  }

  private static void fixAncestorRanks(RankPairingNode p) {
    RankPairingNode u = p;
    while (u != null) {
      int leftRank = u.left == null ? -1 : u.left.rank;
      int rightRank = u.right == null ? -1 : u.right.rank;

      // Root
      if (u.parent == null) {
        u.rank = leftRank + 1;
      } else {
        int k;
        if (Math.abs(leftRank - rightRank) > 1) {
          k = Math.max(leftRank, rightRank);
        } else {
          k = Math.max(leftRank, rightRank) + 1;
        }

        if (k >= u.rank) {
          break;
        } else {
          u.rank = k;
        }
      }

      u = u.parent;
    }
  }


  private static RankPairingNode[] doubleSize(RankPairingNode[] array, int atLeastThisBig) {
    RankPairingNode[] newArray = new RankPairingNode[atLeastThisBig * 2 + 1];
    System.arraycopy(array, 0, newArray, 0, array.length);
    return newArray;
  }
}
