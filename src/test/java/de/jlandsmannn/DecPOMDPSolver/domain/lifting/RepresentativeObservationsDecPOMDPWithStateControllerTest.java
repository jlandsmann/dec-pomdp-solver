package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RepresentativeObservationsDecPOMDPWithStateControllerTest {

  RepresentativeObservationsDecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    decPOMDP = DecPOMDPGenerator.getRepresentativeDecPOMDP();
  }

  @Test
  void getNodeCombinations_ShouldReturnVectorsWithEntryForEachAgent() {
    for (var nodeVector :  decPOMDP.getNodeCombinations()) {
      var expected = decPOMDP.getTotalAgentCount();
      var actual = nodeVector.size();
      assertEquals(expected, actual);
    }
  }

  @Test
  void getNodeCombinations_ShouldReturnNodeCombinationsWithSameNodeForPartition() {
    for (var nodeVector : decPOMDP.getNodeCombinations()) {
      var offset = 0;
      for (var agent : decPOMDP.getAgents()) {
        var partitionNodes = nodeVector.toList().subList(offset, offset + agent.getPartitionSize());
        var uniquePartitionNodes = Set.copyOf(partitionNodes);
        var expectedUniquePartitionNodes = 1;
        assertEquals(expectedUniquePartitionNodes, uniquePartitionNodes.size());
        offset += agent.getPartitionSize();
      }
    }
  }

  @Test
  void getNodeCombinationsByCurrentNode_ShouldReturnVectorsWithEntryForEachAgent() {
    for (var currentNodeVector : decPOMDP.getNodeCombinations()) {
      for (var nodeVector : decPOMDP.getNodeCombinations(currentNodeVector)) {
        var expected = decPOMDP.getTotalAgentCount();
        var actual = nodeVector.size();
        assertEquals(expected, actual);
      }
    }
  }

  @Test
  void getNodeCombinationsByCurrentNode_ShouldReturnNodeCombinationsWithSameNodeForPartition() {
    for (var currentNodeVector : decPOMDP.getNodeCombinations()) {
      for (var nodeVector : decPOMDP.getNodeCombinations(currentNodeVector)) {
        var offset = 0;
        for (var agent : decPOMDP.getAgents()) {
          var partitionNodes = nodeVector.toList().subList(offset, offset + agent.getPartitionSize());
          var uniquePartitionNodes = Set.copyOf(partitionNodes);
          var expectedUniquePartitionNodes = 1;
          assertEquals(expectedUniquePartitionNodes, uniquePartitionNodes.size());
          offset += agent.getPartitionSize();
        }
      }
    }
  }

  @Test
  void getActionVectors_ShouldReturnActionCombinationsWithSameNodeForPartition() {
    for (var actionVector : decPOMDP.getActionVectors()) {
      var offset = 0;
      for (var agent : decPOMDP.getAgents()) {
        var partitionActions = actionVector.toList().subList(offset, offset + agent.getPartitionSize());
        var uniquePartitionActions = Set.copyOf(partitionActions);
        var expectedUniquePartitionActions = 1;
        assertEquals(expectedUniquePartitionActions, uniquePartitionActions.size());
        offset += agent.getPartitionSize();
      }
    }
  }

  @Test
  void getActionCombinationsByCurrentNode_ShouldReturnNodeCombinationsWithSameNodeForPartition() {
    for (var currentNodeVector : decPOMDP.getNodeCombinations()) {
      for (var actionVector : decPOMDP.getActionCombinations(currentNodeVector)) {
        var offset = 0;
        for (var agent : decPOMDP.getAgents()) {
          var partitionActions = actionVector.toList().subList(offset, offset + agent.getPartitionSize());
          var uniquePartitionActions = Set.copyOf(partitionActions);
          var expectedUniquePartitionActions = 1;
          assertEquals(expectedUniquePartitionActions, uniquePartitionActions.size());
          offset += agent.getPartitionSize();
        }
      }
    }
  }

  @Test
  void getObservationVectors_ShouldReturnActionCombinationsWithSameNodeForPartition() {
    for (var observationVector : decPOMDP.getObservationVectors()) {
      var offset = 0;
      for (var agent : decPOMDP.getAgents()) {
        var partitionObservations = observationVector.toList().subList(offset, offset + agent.getPartitionSize());
        var uniquePartitionObservations = Set.copyOf(partitionObservations);
        var expectedUniquePartitionObservations = 1;
        assertEquals(expectedUniquePartitionObservations, uniquePartitionObservations.size());
        offset += agent.getPartitionSize();
      }
    }
  }
}