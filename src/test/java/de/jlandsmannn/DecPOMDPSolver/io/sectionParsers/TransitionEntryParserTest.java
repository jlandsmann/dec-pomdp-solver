package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class TransitionEntryParserTest {

  private TransitionEntryParser parser;

  @BeforeEach
  void setUp() {
    parser = new TransitionEntryParser();
  }

  @Test
  void parseTransitionEntry_ShouldThrowIfStatesNotInitialized() {
    var section = "T: * : * : * : 0.1";
    assertThrows(IllegalStateException.class, () -> {
      parser.parseTransitionEntry(section);
    });
  }

  @Test
  void parseTransitionEntry_ShouldThrowIfAgentActionsNotInitialized() {
    parser.setStates(State.listOf("A", "B"));
    var section = "T: * : * : * : 0.1";
    assertThrows(IllegalStateException.class, () -> {
      parser.parseTransitionEntry(section);
    });
  }

  @Test
  void parseTransitionEntry_ShouldReplaceStartStateWildcardWithAllStates() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));

    var section = "T: A1 A2 : * : * : 0.1";
    var actionVector = Vector.of(Action.listOf("A1", "A2"));
    parser.parseTransitionEntry(section);

    for (var state : states) {
      assertTrue(parser.transitions.containsKey(state));
      assertTrue(parser.transitions.get(state).containsKey(actionVector));
    }
  }

  @Test
  void parseTransitionEntry_ShouldReplaceSingleActionWildcardWithAllActionsOfAgent() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));

    var section = "T: A1 * : A : B : 0.1";
    parser.parseTransitionEntry(section);
    var actionVectors = List.of(
      Vector.of(Action.listOf("A1", "A1")),
      Vector.of(Action.listOf("A1", "A2"))
    );
    var startState = State.from("A");

    for (var actionVector : actionVectors) {
      assertTrue(parser.transitions.get(startState).containsKey(actionVector));
    }
  }

  @Test
  void parseTransitionEntry_ShouldReplaceActionWildcardWithAllActionCombinations() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));

    var section = "T: * : A : B : 0.1";
    parser.parseTransitionEntry(section);
    var actionVectors = List.of(
      Vector.of(Action.listOf("A1", "A1")),
      Vector.of(Action.listOf("A1", "A2")),
      Vector.of(Action.listOf("A2", "A1")),
      Vector.of(Action.listOf("A2", "A2"))
    );
    var startState = State.from("A");

    for (var actionVector : actionVectors) {
      assertTrue(parser.transitions.get(startState).containsKey(actionVector));
    }
  }

  @Test
  void parseTransitionEntry_ShouldGenerateProbabilitiesFromProbabilityDistribution() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));

    var section = "T: A1 A1 : A :\n" + "0.1 0.9";
    parser.parseTransitionEntry(section);
    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var stateA = State.from("A");
    var stateB = State.from("B");
    var expectedProbabilityAToA = 0.1D;
    var expectedProbabilityAToB = 0.9D;
    var actualProbabilityAToA = parser.transitions.get(stateA).get(actionVector).get(stateA);
    var actualProbabilityAToB = parser.transitions.get(stateA).get(actionVector).get(stateB);

    assertEquals(expectedProbabilityAToA, actualProbabilityAToA);
    assertEquals(expectedProbabilityAToB, actualProbabilityAToB);
  }

  @Test
  void parseTransitionEntry_ShouldCreateUniformDistributionWhenUniformKeywordUsed() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));

    var section = "T: A1 A1 :\nuniform";
    parser.parseTransitionEntry(section);
    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var startState = State.from("A");
    var expectedDistribution = Distribution.createUniformDistribution(states).toMap();
    assertTrue(parser.transitions.containsKey(startState));
    assertTrue(parser.transitions.get(startState).containsKey(actionVector));
    var actualDistribution = parser.transitions.get(startState).get(actionVector);
    assertEquals(expectedDistribution, actualDistribution);
  }

  @Test
  void parseTransitionEntry_ShouldCreateSelfLoopWhenIdentityKeywordUsed() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));

    var section = "T: A1 A1 :\nidentity";
    parser.parseTransitionEntry(section);
    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var startState = State.from("A");
    var distribution = parser.transitions.get(startState).get(actionVector);
    var expectedProbability = 1D;
    var actualProbability = distribution.getOrDefault(startState, 0D);
    assertEquals(expectedProbability, actualProbability);
  }

  @Test
  void parseTransitionEntry_ShouldGenerateProbabilitiesFromMatrixSyntax() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));

    var section = "T: A1 A1 :\n" + "0.1 0.9\n" + "0.3 0.7";
    parser.parseTransitionEntry(section);
    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var stateA = State.from("A");
    var stateB = State.from("B");
    var expectedProbabilityAToA = 0.1D;
    var expectedProbabilityAToB = 0.9D;
    var expectedProbabilityBToA = 0.3D;
    var expectedProbabilityBToB = 0.7D;
    var actualProbabilityAToA = parser.transitions.get(stateA).get(actionVector).get(stateA);
    var actualProbabilityAToB = parser.transitions.get(stateA).get(actionVector).get(stateB);
    var actualProbabilityBToA = parser.transitions.get(stateB).get(actionVector).get(stateA);
    var actualProbabilityBToB = parser.transitions.get(stateB).get(actionVector).get(stateB);

    assertEquals(expectedProbabilityAToA, actualProbabilityAToA);
    assertEquals(expectedProbabilityAToB, actualProbabilityAToB);
    assertEquals(expectedProbabilityBToA, actualProbabilityBToA);
    assertEquals(expectedProbabilityBToB, actualProbabilityBToB);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "T: 5",
    "T: A1 A2 : A1 :\nidentity",
    "T: * : uniform",
    "T: * : identity",
  })
  void parseTransitionEntry_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));

    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseTransitionEntry(invalidSection)
    );
  }
}