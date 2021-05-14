package algorithms.brandespp.metis;

import algorithms.heaps.binary.MaxBinaryHeap;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import java.util.ArrayList;

public class GainBucketHeap {


  private final ArrayList<MaxBinaryHeap> posBuckets = new ArrayList<>();
  private final ArrayList<MaxBinaryHeap> negBuckets = new ArrayList<>();
  private final TIntIntHashMap cells;
  private final double[] dis;
  private int maxBucket;
  private boolean maxBucketCached = false;


  public GainBucketHeap(int size, double[] dis) {
    this.cells = new TIntIntHashMap(size);
    this.dis = dis;
  }

  public GainBucketHeap(TIntSet nodes, double[] dis) {
    this(nodes.size(), dis);
    TIntIterator it = nodes.iterator();
    while (it.hasNext()) {
      int v = it.next();
      addToBucket(v);
    }
  }

  public GainBucketHeap(int[] nodes, double[] dis) {
    this(nodes.length, dis);
    for (int v : nodes) {
      addToBucket(v);
    }
  }

  private void addToBucket(int v) {
    int bucket = (int) Math.floor(dis[v]);
    if (bucket > maxBucket || (posBuckets.isEmpty() && negBuckets.isEmpty())) {
      maxBucket = bucket;
      maxBucketCached = true;
    }

    if (bucket >= 0) {
      if (posBuckets.size() <= bucket || posBuckets.get(bucket) == null) {
        fillTo(posBuckets, bucket);
      }
      posBuckets.get(bucket).insertOrIncrease(v);
      cells.put(v, bucket);
    } else {
      bucket = -1 * bucket;
      if (negBuckets.size() <= bucket || negBuckets.get(bucket) == null) {
        fillTo(negBuckets, bucket);
      }
      negBuckets.get(bucket).insertOrIncrease(v);
      cells.put(v, -1 * bucket);
    }
  }

  private int gains() {
    return posBuckets.size() + negBuckets.size() + 1;
  }

  private void removeFromBucket(int v) {
    int bucket = cells.get(v);
    boolean wasLast;

    if (bucket >= 0) {
      posBuckets.get(bucket).remove(v);
      wasLast = posBuckets.get(bucket).isEmpty();
    } else {
      negBuckets.get(-1 * bucket).remove(v);
      wasLast = negBuckets.get(-1 * bucket).isEmpty();
    }

    if (bucket == maxBucket && wasLast) {
      maxBucketCached = false;
    }

    cells.remove(v);
  }

  private void fillTo(ArrayList<MaxBinaryHeap> bucket, int v) {
    for (int i = bucket.size(); i <= v; i++) {
      bucket.add(new MaxBinaryHeap(size() / gains(), dis));
    }
  }

  public void update(int v) {
    removeFromBucket(v);
    addToBucket(v);
  }

  public boolean contains(int v) {
    return cells.containsKey(v);
  }

  public boolean isEmpty() {
    return cells.isEmpty();
  }

  public int size() {
    return cells.size();
  }

  public void insert(int v) {
    if (cells.keySet().contains(v)) {
      update(v);
    } else {
      addToBucket(v);
    }
  }

  public void remove(int v) {
    if (isEmpty()) {
      throw new NullPointerException("empty");
    }
    removeFromBucket(v);
  }

  private void findMaxBucket() {
    if (!posBuckets.isEmpty()) {
      for (int i = posBuckets.size() - 1; i >= 0; i--) {
        if (!posBuckets.get(i).isEmpty()) {
          maxBucket = i;
          maxBucketCached = true;
          return;
        }
      }
    }
    if (!negBuckets.isEmpty()) {
      for (int i = 0; i < negBuckets.size(); i++) {
        if (!negBuckets.get(i).isEmpty()) {
          maxBucket = -1 * i;
          maxBucketCached = true;
          return;
        }
      }
    }
  }

  public int peek() {
    if (isEmpty()) {
      throw new NullPointerException("empty");
    }
    if (!maxBucketCached) {
      findMaxBucket();
    }
    if (maxBucket >= 0) {
      return posBuckets.get(maxBucket).peek();
    } else {
      return negBuckets.get(-1 * maxBucket).peek();
    }
  }

  public int pop() {
    if (isEmpty()) {
      throw new NullPointerException("empty");
    }
    if (!maxBucketCached) {
      findMaxBucket();
    }
    int ret;
    if (maxBucket >= 0) {
      ret = posBuckets.get(maxBucket).peek();
    } else {
      ret = negBuckets.get(-1 * maxBucket).peek();
    }
    removeFromBucket(ret);
    return ret;
  }

  @Override
  public String toString() {
    StringBuilder ret = new StringBuilder();
    ret.append("{");
    for (int i = negBuckets.size() - 1; i >= 0; i--) {
      ret.append(-1 * i).append(": ").append(negBuckets.get(i)).append("\n");
    }
    for (int i = 0; i < posBuckets.size(); i++) {
      ret.append(i).append(": ").append(posBuckets.get(i)).append("\n");
    }
    ret.append("}");
    return ret.toString();
  }
}
