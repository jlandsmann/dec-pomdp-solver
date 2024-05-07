package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExhaustiveBackupPerformerTest {

  private DecPOMDPWithStateController decPOMDP;
  private ExhaustiveBackupPerformer exhaustiveBackupPerformer;

  @BeforeEach
  void setUp() {
    decPOMDP = DecPOMDPGenerator.getDecTigerPOMDP();
    exhaustiveBackupPerformer = spy(new ExhaustiveBackupPerformer());
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
  void performExhaustiveBackup_ShouldCallExhaustiveBackupForEachAgent() {
    exhaustiveBackupPerformer.setDecPOMDP(decPOMDP).performExhaustiveBackup();

    for (var agent : decPOMDP.getAgents()) {
      verify(exhaustiveBackupPerformer).performExhaustiveBackupForAgent(agent);
    }
  }

  @Test
  void performExhaustiveBackup_ShouldCallUpdateValueFunction() {
    exhaustiveBackupPerformer.setDecPOMDP(decPOMDP).performExhaustiveBackup();
    verify(exhaustiveBackupPerformer).updateValueFunction();
  }

  @Test
  void performExhaustiveBackupForAgent_ShouldAddNodesToAgentsController() {
    var agent = decPOMDP.getAgents().get(0);
    var actionCount = agent.getActions().size();
    var nodeCount = agent.getControllerNodes().size();
    var observationCount = agent.getObservations().size();
    var expectedNodeCount = nodeCount + actionCount * Math.pow(nodeCount, observationCount);

    exhaustiveBackupPerformer.setDecPOMDP(decPOMDP).performExhaustiveBackupForAgent(agent);
    var actualNodeCount = agent.getControllerNodes().size();

    assertEquals(expectedNodeCount, actualNodeCount);
  }

  @Test
  void performExhaustiveBackupForAgent_ShouldNotRemoveExistingNodes() {
    var agent = decPOMDP.getAgents().get(0);
    var originalNodes = List.copyOf(agent.getControllerNodes());

    exhaustiveBackupPerformer.setDecPOMDP(decPOMDP).performExhaustiveBackupForAgent(agent);
    var newNodes = List.copyOf(agent.getControllerNodes());

    assertTrue(newNodes.containsAll(originalNodes));
  }

  @Test
  void updateValueFunction_ShouldUpdateAllMissingValuesOfValueFunction() {
    var agent = decPOMDP.getAgents().get(0);
    var originalNodes = List.copyOf(agent.getControllerNodes());
    exhaustiveBackupPerformer.setDecPOMDP(decPOMDP).performExhaustiveBackupForAgent(agent);
    var addedNodes = agent.getControllerNodes();
    addedNodes.removeAll(originalNodes);

    var rawNodeCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (a.equals(agent)) return addedNodes;
      else return a.getControllerNodes();
    }).toList();
    var newNodeCombinations = VectorStreamBuilder.forEachCombination(rawNodeCombinations).toList();

    for (var state : decPOMDP.getStates()) {
      for (var nodeCombination : newNodeCombinations) {
        assertTrue(decPOMDP.getOptionalValue(state, nodeCombination).isEmpty());
      }
    }

    exhaustiveBackupPerformer.updateValueFunction();

    for (var state : decPOMDP.getStates()) {
      for (var nodeCombination : newNodeCombinations) {
        assertTrue(decPOMDP.getOptionalValue(state, nodeCombination).isPresent());
      }
    }
  }
}