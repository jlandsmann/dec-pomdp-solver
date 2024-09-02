package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FiniteStateControllerTest {
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
  private FiniteStateController finiteStateController;

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
  void getFollowNodes_ShouldReturnFollowNodes() {
    var node = Node.from("N1");
    var expected = List.copyOf(nodes);
    var actual = finiteStateController.getFollowNodes(node);

    assertEquals(expected, actual);
  }

  @Test
  void getFollowNodes_ShouldThrowIfNodeDoesNotExist() {
    var nonExistingNode = Node.from("sdkjsahdkj");
    assertThrows(IllegalArgumentException.class, () -> finiteStateController.getFollowNodes(nonExistingNode));
  }

  @Test
  void getFollowNodes_ShouldReturnEmptyListIfNodeHasNoTransitions() {
    var newlyAddedNode = Node.from("sdkjsahdkj");
    var actionSelection = Distribution.createSingleEntryDistribution(Action.from("A1"));
    finiteStateController.addNode(newlyAddedNode, actionSelection);
    assertThrows(IllegalStateException.class, () -> finiteStateController.getFollowNodes(newlyAddedNode));
  }

  @Test
  void getFollowNodes_ShouldUpdateFollowNodesAfterTransitionAdded() {
    var node = Node.from("N6");
    var follower = Node.from("F1");
    var action = Action.from("A1");
    finiteStateController.addNode(node, action);
    finiteStateController.addNode(follower, action);
    finiteStateController.addTransition(node, action, Observation.from("O1"), follower);
    var expected = Node.listOf("F1");
    var actual = finiteStateController.getFollowNodes(node);

    assertEquals(expected, actual);
  }

  @Test
  void getFollowNodes_ShouldOutputEachNodeOnce() {
    var node = Node.from("N6");
    var followNode = Node.from("F1");
    var action = Action.from("A1");
    finiteStateController.addNode(node, action);
    finiteStateController.addNode(followNode, action);
    finiteStateController.addTransition(node, action, Observation.from("O1"), followNode);
    finiteStateController.addTransition(node, action, Observation.from("O2"), followNode);
    var expected = List.of(followNode);
    var actual = finiteStateController.getFollowNodes(node);

    assertEquals(expected, actual);
  }

  @Test
  void getFollowNodes_ShouldNotOutputPrunedNodes() {
    var node = Node.from("N6");
    var followNode = Node.from("F1");
    var action = Action.from("A1");
    finiteStateController.addNode(node, action);
    finiteStateController.addNode(followNode, action);
    finiteStateController.addTransition(node, action, Observation.from("O1"), followNode);

    finiteStateController.pruneNode(followNode, Node.from("N1"));
    var expected = Node.listOf("N1");
    var actual = finiteStateController.getFollowNodes(node);

    assertEquals(expected, actual);
  }

  @Test
  void getAction_ShouldReturnActionSelectionDistributionBasedOnGivenNode() {
    var node = Node.from("N1");
    var expected = 1D / actions.size();
    for (var action : actions) {
      var actual = finiteStateController.getActionSelectionProbability(node, action);
      assertEquals(expected, actual);
    }
  }

  @Test
  void getTransition_ShouldReturnStateDistributionBasedOnGivenTransition() {
    var node = new Node("N1");
    var action = new Action("A1");
    var observation = new Observation("O1");
    var expected = 1D / nodes.size();
    for (var followNode: nodes) {
      var actual = finiteStateController.getTransitionProbability(node, action, observation, followNode);
      assertEquals(expected, actual);
    }
  }

  @Test
  void addNode_ShouldAddNodeThrowIfNodeDoesAlreadyExist() {
    var existingNode = finiteStateController.getNodes().get(0);
    var actionDistribution = Distribution.createUniformDistribution(actions);
    assertThrows(IllegalArgumentException.class, () ->
      finiteStateController.addNode(existingNode, actionDistribution)
    );
  }

  @Test
  void addNode_ShouldAddNodeAndActionDistribution() {
    var newNode = Node.from("NN1");
    var action = Action.from("A1");
    var actionDistribution = Distribution.createSingleEntryDistribution(action);
    finiteStateController.addNode(newNode, actionDistribution);

    var expectedSize = nodes.size() + 1;
    var actualSize = finiteStateController.getNodes().size();
    assertEquals(expectedSize, actualSize);

    var actualDistribution = finiteStateController.getActionSelectionProbability(newNode, action);
    assertEquals(1D, actualDistribution);
  }

  @Test
  void addNode_ShouldCreateSingleEntryDistributionIfSingleActionGiven() {
    var newNode = new Node("NN1");
    var action = new Action("A1");
    finiteStateController.addNode(newNode, action);

    var expectedSize = nodes.size() + 1;
    var actualSize = finiteStateController.getNodes().size();
    assertEquals(expectedSize, actualSize);

    var actualDistribution = finiteStateController.getActionSelectionProbability(newNode, action);
    assertEquals(1D, actualDistribution);
  }

  @Test
  void addTransition_ShouldThrowIfNodeDoesNotExist() {
    var newNode = new Node("NN1");
    var action = new Action("A1");
    var observation = new Observation("O1");
    var nodeDistribution = Distribution.createUniformDistribution(nodes);
    assertThrows(IllegalArgumentException.class, () ->
      finiteStateController.addTransition(newNode, action, observation, nodeDistribution)
    );
  }

  @Test
  void addTransition_ShouldUpdateFollowNodeFunction() {
    var newNode = new Node("NN1");
    var action = new Action("A1");
    var observation = new Observation("O1");
    var followNode = Node.from("N1");
    var nodeDistribution = Distribution.createSingleEntryDistribution(followNode);
    finiteStateController.addNode(newNode, action);
    finiteStateController.addTransition(newNode, action, observation, nodeDistribution);

    var actualFollowNode = finiteStateController.getTransitionProbability(newNode, action, observation, followNode);
    assertEquals(1D, actualFollowNode);
  }

  @Test
  void addTransition_ShouldCreateSingleEntryDistributionIfSingleNodeGiven() {
    var newNode = new Node("NN1");
    var action = new Action("A1");
    var observation = new Observation("O1");
    var followNode = new Node("N1");
    finiteStateController.addNode(newNode, action);
    finiteStateController.addTransition(newNode, action, observation, followNode);

    var actualFollowNode = finiteStateController.getTransitionProbability(newNode, action, observation, followNode);
    assertEquals(1D, actualFollowNode);
  }

  @Test
  void pruneNode_ShouldRemoveNodeFromNodes() {
    var nodeToPrune = new Node("N1");
    var nodeDistribution = Distribution.createSingleEntryDistribution(new Node("N2"));
    finiteStateController.pruneNode(nodeToPrune, nodeDistribution);

    assertFalse(finiteStateController.getNodes().contains(nodeToPrune));
  }

  @Test
  void pruneNode_ShouldRemoveNodeFromActionSelection() {
    var nodeToPrune = new Node("N1");
    var nodeDistribution = Distribution.createSingleEntryDistribution(new Node("N2"));
    finiteStateController.pruneNode(nodeToPrune, nodeDistribution);

    for (var action : actions) {
      assertThrows(IllegalArgumentException.class, () ->
        finiteStateController.getActionSelectionProbability(nodeToPrune, action)
      );
    }
  }

  @Test
  void pruneNode_ShouldRemoveNodeFromTransitionFunction() {
    var nodeToPrune = nodes.stream().findFirst().orElseThrow();
    var nodeDistribution = Distribution.createSingleEntryDistribution(new Node("N2"));
    finiteStateController.pruneNode(nodeToPrune, nodeDistribution);

    for (var node : finiteStateController.getNodes()) {
      for (var action : actions) {
        for (var observation : observations) {
          assertThrows(IllegalArgumentException.class, () ->
            finiteStateController.getTransitionProbability(node, action, observation, nodeToPrune)
          );
          assertThrows(IllegalArgumentException.class, () ->
            finiteStateController.getTransitionProbability(nodeToPrune, action, observation, node)
          );
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
          var expectedProbabilityN2 = originalDistributionWeight + (nodeN2ReplacementWeight * originalDistributionWeight);
          var actualProbabilityN2 = finiteStateController.getTransitionProbability(otherNodes, action, observation, nodeN2);
          assertEquals(expectedProbabilityN2, actualProbabilityN2);

          var actualProbabilityN3 = finiteStateController.getTransitionProbability(otherNodes, action, observation, nodeN3);
          var expectedProbabilityN3 = originalDistributionWeight + (nodeN3ReplacementWeight * originalDistributionWeight);
          assertEquals(expectedProbabilityN3, actualProbabilityN3);

          var actualProbabilityN4 = finiteStateController.getTransitionProbability(otherNodes, action, observation, nodeN4);
          var expectedProbabilityN4 = originalDistributionWeight + (nodeN4ReplacementWeight * originalDistributionWeight);
          assertEquals(expectedProbabilityN4, actualProbabilityN4);
        }
      }
    }
  }

}