package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class finds the vectors of dominating nodes for all belief points,
 * and marks those nodes as initial nodes of the agent's state controllers.
 */
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

    var nodeVectorsToRetain = findDominatingNodeVectors();
    retainNodeVectors(nodeVectorsToRetain);
  }

  protected Set<Vector<Node>> findDominatingNodeVectors() {
    LOG.info("Calculating dominating node vectors");
    var nodeVectorsToRetain = new HashSet<Vector<Node>>();
    for (var beliefState : beliefPoints) {
      var nodeCombination = decPOMDP.getBestNodeCombinationFor(beliefState);
      nodeVectorsToRetain.add(nodeCombination);
    }
    LOG.info("Found {} dominating node vectors", nodeVectorsToRetain.size());
    return nodeVectorsToRetain;
  }

  protected void retainNodeVectors(Set<Vector<Node>> nodeVectorsToRetain) {
    for (var agent : decPOMDP.getAgents()) {
      retainNodeVectorsForAgent(nodeVectorsToRetain, agent);
    }
  }

  protected void retainNodeVectorsForAgent(Set<Vector<Node>> globalNodeVectorsToRetain, AgentWithStateController agent) {
    var globalNodesToRetain = globalNodeVectorsToRetain.stream().flatMap(Vector::stream).collect(Collectors.toSet());
    var agentNodesToRetain = new HashSet<>(agent.getControllerNodes());
    agentNodesToRetain.retainAll(globalNodesToRetain);
    agent.setInitialControllerNodes(agentNodesToRetain);
    LOG.info("Marked {} nodes as initial nodes for {}", agentNodesToRetain.size(), agent);
  }

  private void validateBeliefPoints(Set<Distribution<State>> beliefPoints) {
    if (beliefPoints.isEmpty()) {
      throw new IllegalArgumentException("Belief points must not be empty.");
    };
  }
}
