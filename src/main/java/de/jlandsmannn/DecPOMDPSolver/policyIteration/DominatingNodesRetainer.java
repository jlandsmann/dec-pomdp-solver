package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class DominatingNodesRetainer {
  private static final Logger LOG = LoggerFactory.getLogger(DominatingNodesRetainer.class);
  private DecPOMDPWithStateController decPOMDP;
  private Set<Distribution<State>> beliefPoints;

  public DominatingNodesRetainer setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    LOG.debug("Retrieving DecPOMDP: {}", decPOMDP);
    this.decPOMDP = decPOMDP;
    return this;
  }

  public DominatingNodesRetainer setBeliefPoints(Set<Distribution<State>> beliefPoints) {
    LOG.debug("Retrieving belief points: {}", beliefPoints);
    validateBeliefPoints(beliefPoints);
    this.beliefPoints = beliefPoints;
    return this;
  }

  public void retainDominatingNodes() {
    LOG.info("Retaining dominating nodes");
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to retain dominating nodes.");
    else if (beliefPoints == null) throw new IllegalStateException("Belief points must be set to retain dominating nodes.");

    var nodesToRetain = findDominatingNodes();
    pruneOtherNodes(nodesToRetain);
    LOG.info("Successfully pruned non-dominating nodes");
  }

  protected Set<Node> findDominatingNodes() {
    LOG.info("Calculating dominating nodes");
    var nodesToRetain = new HashSet<Node>();
    for (var beliefState : beliefPoints) {
      var nodeCombination = decPOMDP.getBestNodeCombinationFor(beliefState);
      nodesToRetain.addAll(nodeCombination.stream().toList());
    }
    LOG.info("Found {} dominating nodes", nodesToRetain.size());
    return nodesToRetain;
  }

  protected void pruneOtherNodes(Set<Node> nodesToRetain) {
    for (var agent : decPOMDP.getAgents()) {
      var agentNodesToPrune = new HashSet<>(agent.getControllerNodes());
      agentNodesToPrune.removeAll(nodesToRetain);
      LOG.info("Pruning {} nodes from {}", agentNodesToPrune.size(), agent);

      var agentNodesToRetain = new HashSet<>(agent.getControllerNodes());
      agentNodesToRetain.retainAll(nodesToRetain);
      LOG.info("Retaining {} nodes from {}", agentNodesToRetain.size(), agent);

      var distribution = Distribution.createUniformDistribution(agentNodesToRetain);
      agent.pruneNodes(agentNodesToPrune, distribution);
    }
  }

  private void validateBeliefPoints(Set<Distribution<State>> beliefPoints) {
    if (beliefPoints.isEmpty()) {
      throw new IllegalArgumentException("Belief points must not be empty.");
    };
  }
}
