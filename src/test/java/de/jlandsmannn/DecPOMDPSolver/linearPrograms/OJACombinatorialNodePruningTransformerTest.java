package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
class OJACombinatorialNodePruningTransformerTest {

  private DecPOMDPWithStateController decPOMDP;
  private AgentWithStateController agent;
  private Collection<Distribution<State>> beliefPoints;
  private Node node;

  private OJACombinatorialNodePruningTransformer transformer;

  @BeforeEach
  void setUp() {
    decPOMDP = spy(DecPOMDPGenerator.getDecTigerPOMDPWithLargeFSC());
    agent = decPOMDP.getAgents().get(0);
    beliefPoints = new ArrayList<>(List.of(
      Distribution.createRandomDistribution(decPOMDP.getStates()),
      Distribution.createRandomDistribution(decPOMDP.getStates())
    ));
    node = agent.getControllerNodes().get(0);
    transformer = spy(new OJACombinatorialNodePruningTransformer());
  }

  @Test
  void setDecPOMDP_ShouldNotThrow() {
    assertDoesNotThrow(() -> transformer.setDecPOMDP(decPOMDP));
  }

  @Test
  void setAgent_ShouldNotThrowIfDecPOMDPSetAndAgentPartOfDecPOMDP() {
    transformer.setDecPOMDP(decPOMDP);
    assertDoesNotThrow(() -> transformer.setAgent(agent));
  }

  @Test
  void setAgent_ShouldThrowIfDecPOMDPNotSet() {
    assertThrows(IllegalStateException.class, () ->
      transformer.setAgent(agent)
    );
  }

  @Test
  void setAgent_ShouldThrowIfDecPOMDPSetButAgentIsNotPartOfDecPOMDP() {
    doReturn(List.of()).when(decPOMDP).getAgents();
    transformer.setDecPOMDP(decPOMDP);
    assertThrows(IllegalArgumentException.class, () -> transformer.setAgent(agent));
  }

  @Test
  void setBeliefPoints_ShouldThrowIfNoBeliefPointsGiven() {
    assertThrows(IllegalArgumentException.class, () ->
      transformer.setBeliefPoints(List.of())
    );
  }

  @Test
  void setBeliefPoints_ShouldNotThrowIfBeliefPointsGiven() {
    assertDoesNotThrow(() ->
      transformer.setBeliefPoints(beliefPoints)
    );
  }

  @Test
  void getLinearProgramForNode_ShouldThrowIfDecPOMDPNotSet() {
    var node = Node.from("Q1");
    transformer.setBeliefPoints(beliefPoints);
    assertThrows(IllegalStateException.class, () -> transformer.getLinearProgramForNode(node));
  }

  @Test
  void getLinearProgramForNode_ShouldThrowIfAgentNotSet() {
    var node = Node.from("Q1");
    transformer.setDecPOMDP(decPOMDP);
    transformer.setBeliefPoints(beliefPoints);
    assertThrows(IllegalStateException.class, () -> transformer.getLinearProgramForNode(node));
  }

  @Test
  void getLinearProgramForNode_ShouldThrowIfBeliefPointsNotSet() {
    transformer.setDecPOMDP(decPOMDP);
    transformer.setAgent(agent);
    assertThrows(IllegalStateException.class, () ->
      transformer.getLinearProgramForNode(node)
    );
  }

  @Test
  void getLinearProgramForNode_ShouldThrowIfNodeToCheckIsNotPartOfAgent() {
    var node2 = Node.from("Hello World");
    transformer.setDecPOMDP(decPOMDP);
    transformer.setAgent(agent);
    transformer.setBeliefPoints(beliefPoints);
    assertThrows(IllegalArgumentException.class, () ->
      transformer.getLinearProgramForNode(node2)
    );
  }

  @Test
  void getLinearProgramForNode_ShouldNotThrowIfDependenciesGivenAndNodePartOfAgent() {
    transformer.setDecPOMDP(decPOMDP);
    transformer.setAgent(agent);
    transformer.setBeliefPoints(beliefPoints);
    assertDoesNotThrow(() -> transformer.getLinearProgramForNode(node));
  }

  @Nested
  class WhenInitialized {
    @BeforeEach
    void setUp() {
      transformer.setDecPOMDP(decPOMDP);
      transformer.setAgent(agent);
      transformer.setBeliefPoints(beliefPoints);
    }

    @Test
    void getLinearProgramForNode_ShouldCreateVariablesForEpsilonAndOtherNodes() {
      var lp = transformer.getLinearProgramForNode(node);
      var expected = 2 + agent.getInitialControllerNodes().size();
      var actual = lp.countVariables();
      assertEquals(expected, actual);
    }

    @Test
    void getLinearProgramForNode_ShouldCreateExpressionForNodeDistribution() {
      var lp = transformer.getLinearProgramForNode(node);
      assertTrue(lp.getExpression("x(q)") != null);
    }

    @Test
    void getLinearProgramForNode_ShouldCreateExpressionsForBeliefPointsAndNodeCombinations() {
      var lp = transformer.getLinearProgramForNode(node);
      var otherAgentsNodeCombinationCount = decPOMDP.getAgents().stream()
        .filter(a -> !a.equals(agent))
        .map(AgentWithStateController::getInitialControllerNodes)
        .map(Collection::size)
        .reduce(Integer::sum)
        .orElse(0);
      var beliefPointCount = beliefPoints.size();

      var expected = beliefPointCount * otherAgentsNodeCombinationCount;
      var actual = lp.countExpressions();
      assertTrue(expected <= actual);
    }

    @Test
    void getDominatingNodeDistributionFromResult_ShouldReturnEmptyIfEpsilonIsNegative() {
      var result = Map.of("epsilon", -1D);
      var actual = transformer.getDominatingNodeDistributionFromResult(result);
      assertTrue(actual.isEmpty());
    }

    @Test
    void getDominatingNodeDistributionFromResult_ShouldReturnNodeDistributionBasedOnResult() {
      var expected = Distribution.of(Map.of(
        Node.from("A0-Q1"), 0.2,
        Node.from("A0-Q2"), 0.6,
        Node.from("A0-Q3"), 0.2
      ));
      var result = Map.of(
        "epsilon", 1D,
        "A0-Q1", 0.2,
        "A0-Q2", 0.6,
        "A0-Q3", 0.2
      );
      var actual = transformer.getDominatingNodeDistributionFromResult(result);
      assertTrue(actual.isPresent());
      assertEquals(expected, actual.get());
    }
  }
}