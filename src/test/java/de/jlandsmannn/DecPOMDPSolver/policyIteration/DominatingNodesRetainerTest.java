package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class DominatingNodesRetainerTest {

  private DominatingNodesRetainer dominatingNodesRetainer;
  private DecPOMDPWithStateController decPOMDP;
  private Set<Distribution<State>> beliefPoints;

  @BeforeEach
  void setUp() {
    dominatingNodesRetainer = spy(new DominatingNodesRetainer());
    decPOMDP = spy(DecPOMDPGenerator.getDecTigerPOMDPWithLargeFSC());
    beliefPoints = generateRandomBeliefPoints(10);
  }

  @Test
  void setDecPOMDP_ShouldNotThrow() {
    assertDoesNotThrow(() ->
      dominatingNodesRetainer.setDecPOMDP(decPOMDP)
    );
  }

  @Test
  void setBeliefPoints_ShouldNotThrowIfDecPOMDPIsSet() {
    dominatingNodesRetainer.setDecPOMDP(decPOMDP);
    assertDoesNotThrow(() ->
      dominatingNodesRetainer.setBeliefPoints(beliefPoints)
    );
  }

  @Test
  void setBeliefPoints_ShouldThrowIfBeliefPointsAreEmpty() {
    assertThrows(IllegalArgumentException.class, () ->
      dominatingNodesRetainer.setBeliefPoints(Set.of())
    );
  }


  @Test
  void retainDominatingNodes_ShouldThrowIfDecPOMDPIsNotSet() {
    assertThrows(IllegalStateException.class, () ->
      dominatingNodesRetainer.retainDominatingNodes()
    );
  }

  @Test
  void retainDominatingNodes_ShouldThrowIfBeliefPointsNotSet() {
    dominatingNodesRetainer.setDecPOMDP(decPOMDP);
    assertThrows(IllegalStateException.class, () ->
      dominatingNodesRetainer.retainDominatingNodes()
    );
  }

  @Test
  void retainDominatingNodes_ShouldCallFindDominatingNodes() {
    dominatingNodesRetainer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints);
    dominatingNodesRetainer.retainDominatingNodes();
    verify(dominatingNodesRetainer).findDominatingNodes();
  }

  @Test
  void retainDominatingNodes_ShouldCallPruneOtherNodes() {
    dominatingNodesRetainer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .retainDominatingNodes();

    verify(dominatingNodesRetainer).pruneOtherNodes(anySet());
  }

  @Test
  void findDominatingNodes_ShouldGetNodeCombinationForEachBeliefPoint() {
    dominatingNodesRetainer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .findDominatingNodes();

    for (var beliefPoint : beliefPoints) {
      verify(decPOMDP).getBestNodeCombinationFor(beliefPoint);
    }
  }

  @Test
  void pruneOtherNodes_ShouldRemoveEveryNodeNotContainedInNodesToRetain() {
    var nodesToRetain = decPOMDP.getAgents().stream()
      .map(a -> a.getControllerNodes().subList(0, 2))
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());

    dominatingNodesRetainer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .pruneOtherNodes(nodesToRetain);

    for (var agent : decPOMDP.getAgents()) {
      var agentNodes = agent.getControllerNodes();
      assertTrue(nodesToRetain.containsAll(agentNodes));
    }
  }

  private Set<Distribution<State>> generateRandomBeliefPoints(int count) {
    Set<Distribution<State>> beliefPoints = new HashSet<>();
    for (int i = 0; i < count; i++) {
      var randomBeliefPoint = Distribution.createRandomDistribution(decPOMDP.getStates());
      beliefPoints.add(randomBeliefPoint);
    }
    return beliefPoints;
  }
}