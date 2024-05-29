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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

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
    LOG.info("Successfully pruned non-dominating nodes");
  }

  protected Set<Vector<Node>> findDominatingNodeVectors() {
    LOG.info("Calculating dominating nodes");
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
    var agentNodesToPrune = new HashSet<>(agent.getControllerNodes());
    agentNodesToPrune.removeAll(globalNodesToRetain);
    var agentNodesToRetain = new HashSet<>(agent.getControllerNodes());
    agentNodesToRetain.retainAll(globalNodesToRetain);

    var nonPrunableNodes = new HashSet<Node>();
    for (var nodeToPrune : agentNodesToPrune) {
      var dominatingNode = findDominatingNode(globalNodeVectorsToRetain, agentNodesToRetain, nodeToPrune);
      if (dominatingNode.isEmpty()) {
        LOG.debug("No dominating node found for {}", nodeToPrune);
        nonPrunableNodes.add(nodeToPrune);
        continue;
      }
      LOG.debug("Found dominating node {} for {}", dominatingNode.get(), nodeToPrune);
      agent.pruneNode(nodeToPrune, dominatingNode.get());
    }
    LOG.info("Cannot prune {} nodes because no dominating node exists", nonPrunableNodes.size());
    agentNodesToPrune.removeAll(nonPrunableNodes);
    LOG.info("Pruned {} nodes from {} with remaining {} nodes", agentNodesToPrune.size(), agent, agent.getControllerNodes().size());
    decPOMDP.removeNodesFromValueFunction(agentNodesToPrune);
  }

  private void validateBeliefPoints(Set<Distribution<State>> beliefPoints) {
    if (beliefPoints.isEmpty()) {
      throw new IllegalArgumentException("Belief points must not be empty.");
    };
  }

  private Optional<Node> findDominatingNode(Set<Vector<Node>> globalNodeVectorsToRetain, Set<Node> agentNodesToRetain, Node nodeToPrune) {
    LOG.debug("Finding dominating node for {}", nodeToPrune);
    if (agentNodesToRetain.size() == 1) {
      LOG.debug("Agent retains only a single node, that has to be the dominating node for {}", nodeToPrune);
      return agentNodesToRetain.stream().findFirst();
    }
    return agentNodesToRetain.stream()
      .filter(nodeToRetainA -> getMaxValueDifference(globalNodeVectorsToRetain, nodeToRetainA, nodeToPrune) > 0)
      .findAny();
  }

  private double getMaxValueDifference(Set<Vector<Node>> globalNodeVectorsToRetain, Node nodeToRetain, Node nodeToPrune) {
    return globalNodeVectorsToRetain.stream()
      .filter(v -> v.contains(nodeToRetain))
      .mapToDouble(vectorToRetain -> {
        var dominatedVector = vectorToRetain.replace(nodeToRetain, nodeToPrune);
        return calculateMinValueDifference(vectorToRetain, dominatedVector);
      })
      .max()
      .orElseThrow(() -> new IllegalStateException("No global node vectors to remain given, cannot calculate the max value difference."));
  }

  private double calculateMinValueDifference(Vector<Node> vectorA, Vector<Node> vectorB) {
    return beliefPoints.stream()
      .mapToDouble(beliefPoint ->
        decPOMDP.getValue(beliefPoint, vectorA) -
          decPOMDP.getValue(beliefPoint, vectorB)
      )
      .min()
      .orElseThrow(() -> new IllegalStateException("No belief points given, cannot calculate min value difference of two vectors."))
    ;
  }
}
