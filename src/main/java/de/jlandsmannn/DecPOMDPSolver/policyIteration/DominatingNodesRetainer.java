package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IAgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class finds the vectors of dominating nodes for all belief points,
 * and marks those nodes as initial nodes of the agent's state controllers.
 */
@Service
public class DominatingNodesRetainer {
  private static final Logger LOG = LoggerFactory.getLogger(DominatingNodesRetainer.class);
  private IDecPOMDPWithStateController<?> decPOMDP;
  private Map<IAgent, Set<Distribution<State>>> beliefPoints;

  public DominatingNodesRetainer setDecPOMDP(IDecPOMDPWithStateController<?> decPOMDP) {
    LOG.debug("Retrieving DecPOMDP: {}", decPOMDP);
    this.decPOMDP = decPOMDP;
    return this;
  }

  public DominatingNodesRetainer setBeliefPoints(Map<IAgent, Set<Distribution<State>>> beliefPoints) {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to validate belief points.");
    LOG.debug("Retrieving belief points: {}", beliefPoints);
    validateBeliefPoints(beliefPoints);
    this.beliefPoints = beliefPoints;
    return this;
  }

  public void retainDominatingNodes() {
    LOG.info("Retaining dominating nodes");
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to retain dominating nodes.");
    else if (beliefPoints == null)
      throw new IllegalStateException("Belief points must be set to retain dominating nodes.");

    decPOMDP.getAgents().stream()
      .parallel()
      .forEach(this::retainDominatingNodesForAgent);
  }

  protected void retainDominatingNodesForAgent(IAgentWithStateController agent) {
    LOG.info("Calculating dominating node vectors for {}", agent);
    var nodesToRetain = new HashSet<Node>();
    var agentIndex = decPOMDP.getAgents().indexOf(agent);
    for (var beliefState : beliefPoints.get(agent)) {
      var nodeCombination = decPOMDP.getBestNodeCombinationFor(beliefState);
      var nodeToRetain = nodeCombination.get(agentIndex);
      nodesToRetain.add(nodeToRetain);
    }
    LOG.info("Found {} dominating nodes for {}", nodesToRetain.size(), agent);
    LOG.debug("{}: Dominating Nodes: {}", agent, nodesToRetain);
    var originalNodeCount = agent.getControllerNodes().size();
    agent.setInitialControllerNodes(nodesToRetain);
    agent.retainNodesAndFollower(nodesToRetain);
    var newNodesCount = agent.getControllerNodes().size();
    LOG.info("Pruned {} nodes for {} simultaneously", originalNodeCount - newNodesCount, agent);
  }

  private void validateBeliefPoints(Map<IAgent, Set<Distribution<State>>> beliefPoints) {
    var entryForEachAgent = decPOMDP.getAgents().stream().allMatch(beliefPoints::containsKey);
    var someEntryEmpty = beliefPoints.values().stream().anyMatch(Collection::isEmpty);
    if (!entryForEachAgent || someEntryEmpty) {
      throw new IllegalArgumentException("Belief points must not be empty.");
    }
  }
}
