// code by ynager
package ch.ethz.idsc.owl.glc.rl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.math.VectorScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;

public class RLQueue implements Iterable<GlcNode> {
  protected final List<GlcNode> list = new ArrayList<>(500);
  protected final int vectorSize;
  protected final Tensor slack;

  public RLQueue(Tensor slack) {
    this.slack = slack;
    this.vectorSize = slack.length();
  }

  public boolean add(GlcNode e) {
    return list.add(e);
  }

  public GlcNode poll() {
    GlcNode best = getFromBest();
    list.remove(best);
    return best;
  }

  public GlcNode peek() {
    return getFromBest();
  }

  public int size() {
    return list.size();
  }

  public boolean remove(GlcNode e) {
    return list.remove(e);
  }

  public boolean removeAll(Collection<GlcNode> c) {
    return list.removeAll(c);
  }

  /** @return first element from best set
   * @throws Exception if queue is empty */
  private GlcNode getFromBest() {
    List<GlcNode> queueCopy = new ArrayList<>(list);
    getBestSet(queueCopy, 0);
    return queueCopy.get(0);
  }

  /** iteratively find best set
   * 
   * @param list is modified by function
   * @param d level
   * @return list with inferior nodes removed
   * @throws Exception if queue is empty */
  private List<GlcNode> getBestSet(List<GlcNode> list, int d) {
    GlcNode minCostNode = Collections.min(list, new Comparator<GlcNode>() {
      @Override
      public int compare(GlcNode first, GlcNode second) {
        return Scalars.compare( //
            ((VectorScalar) first.merit()).vector().Get(d), //
            ((VectorScalar) second.merit()).vector().Get(d));
      }
    });
    Scalar minMerit = ((VectorScalar) minCostNode.merit()).vector().Get(d);
    Scalar threshold = minMerit.add(slack.Get(d));
    list.removeIf(node -> Scalars.lessThan(threshold, ((VectorScalar) node.merit()).vector().Get(d)));
    if (d == vectorSize - 1)
      return list;
    return getBestSet(list, d + 1);
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override // from Iterable
  public Iterator<GlcNode> iterator() {
    return list.iterator();
  }
}
