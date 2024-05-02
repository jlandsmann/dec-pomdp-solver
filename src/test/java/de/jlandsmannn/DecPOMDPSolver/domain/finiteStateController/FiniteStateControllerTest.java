package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FiniteStateControllerTest {
  private FiniteStateController finiteStateController;
  private final Set<Node> nodes = Set.of(
    new Node("N1"),
    new Node("N2"),
    new Node("N3"),
    new Node("N4")
  );
  private final Set<Action> actions = Set.of(
    new Action("A1"),
    new Action("A2"),
    new Action("A3"),
    new Action("A4"),
    new Action("A5")
  );
  private final Set<Observation> observations = Set.of(
    new Observation("O1"),
    new Observation("O2"),
    new Observation("O3"),
    new Observation("O4"),
    new Observation("O5")
  );

  @BeforeEach
  void setUp() {
    var builder = new FiniteStateControllerBuilder();
    for (var node : nodes) {
      builder
        .addNode(node)
        .addActionSelection(node, Distribution.createUniformDistribution(actions))
      ;
      for (var action : actions) {
        for (var observation : observations) {
          builder.addTransition(node, action, observation, Distribution.createUniformDistribution(nodes));
        }
      }
    }
    finiteStateController = builder.createFiniteStateController();
  }

  @Test
  void getNodes_ShouldReturnAllNodes() {
    var actual = finiteStateController.getNodes();
    assertTrue(nodes.containsAll(actual));
    assertTrue(actual.containsAll(nodes));
  }

  @Test
  void getAction_ShouldReturnActionDistributionBasedOnGivenNode() {
    var actual = finiteStateController.getAction(new Node("N1"));
    var expected = Distribution.createUniformDistribution(actions);
    assertEquals(expected, actual);
  }

  @Test
  void getFollowNode_ShouldReturnStateDistributionBasedOnGivenTransition() {
    var node = new Node("N1");
    var action = new Action("A1");
    var observation = new Observation("O1");
    var actual = finiteStateController.getFollowNode(node, action, observation);
    var expected = Distribution.createUniformDistribution(nodes);
    assertEquals(expected, actual);
  }

  @Test
  void addNode_ShouldAddNodeAndActionDistribution() {
    var newNode = new Node("NN1");
    var actionDistribution = Distribution.createUniformDistribution(actions);
    finiteStateController.addNode(newNode, actionDistribution);

    var expectedSize = nodes.size() + 1;
    var actualSize = finiteStateController.getNodes().size();
    assertEquals(expectedSize, actualSize);

    var actualDistribution = finiteStateController.getAction(newNode);
    assertEquals(actionDistribution, actualDistribution);
  }

  @Test
  void addNode_ShouldCreateSingleEntryDistributionIfSingleActionGiven() {
    var newNode = new Node("NN1");
    var action = new Action("A1");
    finiteStateController.addNode(newNode, action);

    var expectedSize = nodes.size() + 1;
    var actualSize = finiteStateController.getNodes().size();
    assertEquals(expectedSize, actualSize);

    var expectedDistribution = Distribution.createSingleEntryDistribution(action);
    var actualDistribution = finiteStateController.getAction(newNode);
    assertEquals(expectedDistribution, actualDistribution);
  }

  @Test
  void addTransition_ShouldUpdateFollowNodeFunction() {
    var newNode = new Node("NN1");
    var action = new Action("A1");
    var observation = new Observation("O1");
    var nodeDistribution = Distribution.createUniformDistribution(nodes);
    finiteStateController.addTransition(newNode, action, observation, nodeDistribution);

    var actualFollowNode = finiteStateController.getFollowNode(newNode, action, observation);
    assertEquals(nodeDistribution, actualFollowNode);
  }

  @Test
  void addTransition_ShouldCreateSingleEntryDistributionIfSingleNodeGiven() {
    var newNode = new Node("NN1");
    var action = new Action("A1");
    var observation = new Observation("O1");
    var followNode = new Node("N1");
    finiteStateController.addTransition(newNode, action, observation, followNode);

    var expectedFollowNode = Distribution.createSingleEntryDistribution(followNode);
    var actualFollowNode = finiteStateController.getFollowNode(newNode, action, observation);
    assertEquals(expectedFollowNode, actualFollowNode);
  }

  @Test
  void pruneNode_ShouldRemoveAllOccurrencesOfNode() {
    var nodeToPrune = new Node("N1");
    var nodeDistribution = Distribution.createSingleEntryDistribution(new Node("N2"));
    finiteStateController.pruneNode(nodeToPrune, nodeDistribution);

    assertFalse(finiteStateController.getNodes().contains(nodeToPrune));
    assertNull(finiteStateController.getAction(nodeToPrune));
    for (var action : actions) {
      for (var observation : observations) {
        assertNull(finiteStateController.getFollowNode(nodeToPrune, action, observation));
        for (var otherNodes : finiteStateController.getNodes()) {
          var followNodeDistribution = finiteStateController.getFollowNode(otherNodes, action, observation);
          var expectedProbability = 0D;
          var actualProbability = followNodeDistribution.getProbability(nodeToPrune);
          assertEquals(expectedProbability, actualProbability);
        }
      }
    }
  }

  @Test
  void pruneNode_ShouldReplaceNodeWithDistribution() {
    var nodeToPrune = new Node("N1");
    var nodeN2 = new Node("N2");
    var nodeN3 = new Node("N3");
    var nodeN4 = new Node("N4");
    var originalDistributionWeight = 1D / nodes.size();
    var nodeN2ReplacementWeight = 0.2D;
    var nodeN3ReplacementWeight = 0.8D;
    var nodeN4ReplacementWeight = 0.0D;
    var nodeDistribution = Distribution.of(
      Map.of(
        nodeN2, nodeN2ReplacementWeight,
        nodeN3, nodeN3ReplacementWeight,
        nodeN4, nodeN4ReplacementWeight
      )
    );
    finiteStateController.pruneNode(nodeToPrune, nodeDistribution);

    for (var otherNodes : finiteStateController.getNodes()) {
      for (var action : actions) {
        for (var observation : observations) {
          var followNodeDistribution = finiteStateController.getFollowNode(otherNodes, action, observation);
          var expectedProbabilityN2 = originalDistributionWeight + (nodeN2ReplacementWeight * originalDistributionWeight);
          var actualProbabilityN2 = followNodeDistribution.getProbability(nodeN2);
          assertEquals(expectedProbabilityN2, actualProbabilityN2);
          var expectedProbabilityN3 = originalDistributionWeight + (nodeN3ReplacementWeight * originalDistributionWeight);
          var actualProbabilityN3 = followNodeDistribution.getProbability(nodeN3);
          assertEquals(expectedProbabilityN3, actualProbabilityN3);
          var expectedProbabilityN4 = originalDistributionWeight + (nodeN4ReplacementWeight * originalDistributionWeight);
          var actualProbabilityN4 = followNodeDistribution.getProbability(nodeN4);
          assertEquals(expectedProbabilityN4, actualProbabilityN4);
        }
      }
    }
  }

  @Test
  void pruneNodes() {
  }

}