package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExhaustiveBackupPerformerTest {

  private DecPOMDPWithStateController decPOMDP;
  private ExhaustiveBackupPerformer exhaustiveBackupPerformer;
  private Map<Agent, Set<Distribution<State>>> beliefPoints;

  @BeforeEach
  void setUp() {
    decPOMDP = DecPOMDPGenerator.getDecTigerPOMDPWithLargeFSC();
    exhaustiveBackupPerformer = spy(new ExhaustiveBackupPerformer());
    var agentBeliefPoints = Set.of(
      Distribution.of(Map.of(
        decPOMDP.getStates().get(0), 0.5,
        decPOMDP.getStates().get(decPOMDP.getStates().size() - 1), 0.5
      )),
      Distribution.of(Map.of(
        decPOMDP.getStates().get(0), 0.6,
        decPOMDP.getStates().get(decPOMDP.getStates().size() - 1), 0.4
      )),
      Distribution.of(Map.of(
        decPOMDP.getStates().get(0), 0.4,
        decPOMDP.getStates().get(decPOMDP.getStates().size() - 1), 0.6
      ))
    );
    beliefPoints = Map.of(
      decPOMDP.getAgents().get(0), agentBeliefPoints,
      decPOMDP.getAgents().get(decPOMDP.getStates().size() - 1), agentBeliefPoints
    );
  }

  @Test
  void setDecPOMDP_ShouldNotThrow() {
    assertDoesNotThrow(() ->
      exhaustiveBackupPerformer.setDecPOMDP(decPOMDP)
    );
  }

  @Test
  void performExhaustiveBackup_ShouldThrowIfDecPOMDPNotSet() {
    assertThrows(IllegalStateException.class, () ->
      exhaustiveBackupPerformer.performExhaustiveBackup());
  }

  @Test
  void performExhaustiveBackup_ShouldThrowIfBeliefPointsNotSet() {
    assertThrows(IllegalStateException.class, () ->
      exhaustiveBackupPerformer.setDecPOMDP(decPOMDP).performExhaustiveBackup());
  }

  @Test
  void performExhaustiveBackup_ShouldCallExhaustiveBackupForEachAgent() {
    exhaustiveBackupPerformer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .performExhaustiveBackup();

    for (var agent : decPOMDP.getAgents()) {
      verify(exhaustiveBackupPerformer).performExhaustiveBackupForAgent(agent);
    }
  }

  @Test
  void performExhaustiveBackup_ShouldCallUpdateValueFunction() {
    exhaustiveBackupPerformer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .performExhaustiveBackup();
    verify(exhaustiveBackupPerformer).updateValueFunction();
  }

  @Test
  void performExhaustiveBackupForAgent_ShouldAddNodesToAgentsController() {
    var agent = decPOMDP.getAgents().get(0);
    var actionCount = agent.getActions().size();
    var nodeCount = agent.getControllerNodes().size();
    var observationCount = agent.getObservations().size();
    var expectedNodeCount = nodeCount + actionCount * Math.pow(nodeCount, observationCount);

    exhaustiveBackupPerformer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .performExhaustiveBackupForAgent(agent);
    var actualNodeCount = agent.getControllerNodes().size();

    assertEquals(expectedNodeCount, actualNodeCount);
  }

  @Test
  void performExhaustiveBackupForAgent_ShouldNotRemoveExistingNodes() {
    var agent = decPOMDP.getAgents().get(0);
    var originalNodes = List.copyOf(agent.getControllerNodes());

    exhaustiveBackupPerformer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .performExhaustiveBackupForAgent(agent);
    var newNodes = List.copyOf(agent.getControllerNodes());

    assertTrue(newNodes.containsAll(originalNodes));
  }

  @Test
  void updateValueFunction_ShouldUpdateAllMissingValuesOfValueFunctionForBeliefPoints() {
    var agent = decPOMDP.getAgents().get(0);
    var originalNodes = List.copyOf(agent.getControllerNodes());
    exhaustiveBackupPerformer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .performExhaustiveBackupForAgent(agent);
    var addedNodes = agent.getControllerNodes();
    addedNodes.removeAll(originalNodes);

    var rawNodeCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (a.equals(agent)) return addedNodes;
      else return a.getControllerNodes();
    }).toList();
    var newNodeCombinations = VectorCombinationBuilder.listOf(rawNodeCombinations);

    for (var state : decPOMDP.getStates()) {
      for (var nodeCombination : newNodeCombinations) {
        assertFalse(decPOMDP.hasValue(state, nodeCombination));
      }
    }

    exhaustiveBackupPerformer.updateValueFunction();

    var statesInBeliefPoints = beliefPoints.values().stream()
      .flatMap(Collection::stream)
      .map(Distribution::keySet)
      .flatMap(Set::stream)
      .collect(Collectors.toSet());
    for (var state : statesInBeliefPoints) {
      for (var nodeCombination : newNodeCombinations) {
        var optionalValue = decPOMDP.hasValue(state, nodeCombination);
        assertTrue(optionalValue, "Missing value for " + state + " and " + nodeCombination);
      }
    }
  }
}