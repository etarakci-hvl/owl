// code by astoll, ynager
package ch.ethz.idsc.owl.glc.rl2;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.ethz.idsc.owl.data.GlobalAssert;
import ch.ethz.idsc.owl.glc.core.GlcNode;
import ch.ethz.idsc.owl.glc.core.GlcNodes;
import ch.ethz.idsc.owl.glc.core.HeuristicFunction;
import ch.ethz.idsc.owl.glc.core.StateTimeRaster;
import ch.ethz.idsc.owl.glc.core.TrajectoryPlanner;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.tensor.Tensor;

public abstract class RelaxedTrajectoryPlanner implements TrajectoryPlanner, Serializable {
  protected final StateTimeRaster stateTimeRaster;
  private final HeuristicFunction heuristicFunction;
  private final RelaxedPriorityQueue globalQueue;
  private final RelaxedDomainQueueMap domainMap;
  private final RelaxedPriorityQueue goalDomainQueue;

  // ---
  protected RelaxedTrajectoryPlanner( //
      StateTimeRaster stateTimeRaster, HeuristicFunction heuristicFunction, Tensor slacks) {
    this.stateTimeRaster = stateTimeRaster;
    this.heuristicFunction = heuristicFunction;
    this.globalQueue = new RelaxedGlobalQueue(slacks);
    this.domainMap = new RelaxedDomainQueueMap(slacks);
    this.goalDomainQueue = RelaxedDomainQueue.empty(slacks);
  }

  /** Adds node to global queue if it does not have children
   * @param node non-null */
  protected final void addToGlobalQueue(GlcNode node) {
    if (!node.isLeaf()) {
      System.err.println("The Inserted Node has children");
      throw new RuntimeException();
    }
    globalQueue.add(node);
  }

  /** Passes a node to the corresponding domain queue. The domain queue checks whether
   * the node will be added or not and if any other nodes have to be discarded
   * as a consequence.
   * 
   * @param domainKey
   * @param node
   * @return Collection of discarded nodes */
  protected final Collection<GlcNode> addToDomainMap(Tensor domainKey, GlcNode node) {
    return domainMap.addToDomainMap(domainKey, node);
  }

  /** Method is invoked to notify planner that the
   * intersection of the goal interface and the connector is non-empty and
   * the corresponding node will be added to the goal domain queue.
   * 
   * @param node
   * @param connector */
  protected final void offerDestination(GlcNode node, List<StateTime> connector) {
    goalDomainQueue.add(node);
  }

  /** @param domainKey
   * @return RLDomainQueue in domain or Optional.empty() if domain has not been assigned a node yet */
  protected final Optional<RelaxedPriorityQueue> getDomainQueue(Tensor domainKey) {
    return Optional.ofNullable(domainMap.getQueue(domainKey));
  }

  protected final RelaxedPriorityQueue getGlobalQueue() {
    return globalQueue;
  }

  /** Removes all nodes in the collection from th global queue.
   * 
   * @param toRemove */
  protected final void removeFromGlobal(Collection<GlcNode> toRemove) {
    globalQueue.removeAll(toRemove);
  }

  /** Removes the node from the corresponding domain queue in the domain map.
   * 
   * @param domainKey
   * @param glcNode */
  protected final void removeFromDomainQueue(Tensor domainKey, GlcNode glcNode) {
    if (!glcNode.isLeaf()) {
      System.err.println("The node to be removed has children");
    }
    domainMap.removeFromDomainMap(domainKey, glcNode);
  }

  // check if obsolete
  public final Map<Tensor, RelaxedPriorityQueue> getRelaxedDomainQueueMap() {
    return domainMap.getMap();
  }

  /** @return current most promising node in queue, i.d best merit */
  public final GlcNode peekBestFromGlobal() {
    return globalQueue.peekBest();
  }

  /** Polls most promising unexpanded node from the global queue. */
  @Override // from ExpandInterface
  public final Optional<GlcNode> pollNext() {
    // returns the head of queue, or null if queue is empty
    return Optional.ofNullable(globalQueue.pollBest());
  }

  /** Returns most promising unexpanded node without polling it from the global queue. */
  @Override // from ExpandInterface
  public final Optional<GlcNode> getBest() {
    return Optional.ofNullable(goalDomainQueue.collection().isEmpty() ? null : goalDomainQueue.peekBest());
  }

  /** Returns an unmodifiable view of the nodes in the goal domain queue.
   * 
   * @return unmodifiableCollection of GlcNodes */
  public final Collection<GlcNode> getAllNodesInGoal() {
    return Collections.unmodifiableCollection(goalDomainQueue.collection());
  }

  /***************************************************/
  @Override // from TrajectoryPlanner
  public final void insertRoot(StateTime stateTime) {
    GlobalAssert.that(globalQueue.collection().isEmpty() && domainMap.isEmpty()); // root insertion requires empty planner
    GlcNode root = GlcNodes.createRoot(stateTime, heuristicFunction);
    addToGlobalQueue(root);
    addToDomainMap(stateTimeRaster.convertToKey(stateTime), root);
  }

  @Override // from TrajectoryPlanner
  public final Optional<GlcNode> getBestOrElsePeek() {
    return Optional.ofNullable(getBest().orElse(globalQueue.peekBest()));
  }

  @Override // from TrajectoryPlanner
  public final HeuristicFunction getHeuristicFunction() {
    return heuristicFunction;
  }

  @Override // from TrajectoryPlanner
  public final Map<Tensor, GlcNode> getDomainMap() {
    // LONGTERM investigate unified design
    throw new UnsupportedOperationException();
  }

  @Override // from TrajectoryPlanner
  public final Collection<GlcNode> getQueue() {
    return Collections.unmodifiableCollection(getGlobalQueue().collection());
  }
}
