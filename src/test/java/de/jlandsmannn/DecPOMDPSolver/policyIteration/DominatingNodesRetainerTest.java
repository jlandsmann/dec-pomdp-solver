package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

class DominatingNodesRetainerTest {

  private DominatingNodesRetainer dominatingNodesRetainer;
  private DecPOMDPWithStateController decPOMDP;
  private Map<IAgent, Set<Distribution<State>>> beliefPoints;

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
  void setBeliefPoints_ShouldNotThrowIfDecPOMDPIsNotSet() {
    assertThrows(IllegalStateException.class, () ->
      dominatingNodesRetainer.setBeliefPoints(beliefPoints)
    );
  }

  @Test
  void setBeliefPoints_ShouldThrowIfBeliefPointsAreEmpty() {
    dominatingNodesRetainer.setDecPOMDP(decPOMDP);
    assertThrows(IllegalArgumentException.class, () ->
      dominatingNodesRetainer.setBeliefPoints(Map.of())
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

  private Map<IAgent, Set<Distribution<State>>> generateRandomBeliefPoints(int count) {
    Map<IAgent, Set<Distribution<State>>> beliefPoints = new HashMap<>();
    for (var agent : decPOMDP.getAgents()) {
      Set<Distribution<State>> agentBeliefPoints = new HashSet<>();
      for (int i = 0; i < count; i++) {
        var randomBeliefPoint = Distribution.createRandomDistribution(decPOMDP.getStates());
        agentBeliefPoints.add(randomBeliefPoint);
      }
      beliefPoints.put(agent, agentBeliefPoints);
    }
    return beliefPoints;
  }
}