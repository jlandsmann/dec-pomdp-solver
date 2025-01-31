package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RewardEntrySectionParserTest {
  private RewardEntrySectionParser parser;

  @BeforeEach
  void setUp() {
    parser = new RewardEntrySectionParser();
  }

  @Test
  void parseSection_ShouldThrowIfStatesNotInitialized() {
    var section = "R: * : * : * : * : 0.1";
    assertThrows(ParsingFailedException.class, () -> {
      parser.parseSection(section);
    });
  }

  @Test
  void parseSection_ShouldThrowIfAgentActionsNotInitialized() {
    var states = State.listOf("A", "B");
    parser.setStates(states);
    var section = "R: * : * : * : * : 0.1";
    assertThrows(ParsingFailedException.class, () -> {
      parser.parseSection(section);
    });
  }

  @Test
  void parseSection_ShouldThrowIfAgentRewardsNotInitialized() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));
    var section = "R: * : * : * : * : 0.1";
    assertThrows(ParsingFailedException.class, () -> {
      parser.parseSection(section);
    });
  }

  @Test
  void parseSection_ShouldReplaceEndStateWildcardWithAllStates() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    var observations = Observation.listOf("O1", "O2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions))
      .setAgentObservations(List.of(observations, observations));

    var section = "R: A1 A2 : A : * : O1 O2 : 0.1";
    var actionVector = Vector.of(Action.listOf("A1", "A2"));
    parser.parseSection(section);

    var startState = State.from("A");
    for (var endState : states) {
      assertTrue(parser.rewards.containsKey(startState));
      assertTrue(parser.rewards.get(startState).containsKey(actionVector));
      assertTrue(parser.rewards.get(startState).get(actionVector).containsKey(endState));
    }
  }

  @Test
  void parseSection_ShouldReplaceStartStateWildcardWithAllStates() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    var observations = Observation.listOf("O1", "O2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions))
      .setAgentObservations(List.of(observations, observations));

    var section = "R: A1 A2 : * : A : O1 O2 : 0.1";
    var actionVector = Vector.of(Action.listOf("A1", "A2"));
    parser.parseSection(section);

    var endState = State.from("A");
    for (var startState : states) {
      assertTrue(parser.rewards.containsKey(startState));
      assertTrue(parser.rewards.get(startState).containsKey(actionVector));
      assertTrue(parser.rewards.get(startState).get(actionVector).containsKey(endState));
    }
  }

  @Test
  void parseSection_ShouldReplaceSingleActionWildcardWithAllActionsOfAgent() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    var observations = Observation.listOf("O1", "O2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions))
      .setAgentObservations(List.of(observations, observations));

    var section = "R: A1 * : A : A : O1 O2 : 0.1";
    parser.parseSection(section);
    var actionVectors = List.of(
      Vector.of(Action.listOf("A1", "A1")),
      Vector.of(Action.listOf("A1", "A2"))
    );
    var startState = State.from("A");
    var endState = State.from("A");

    for (var actionVector : actionVectors) {
      assertTrue(parser.rewards.containsKey(startState));
      assertTrue(parser.rewards.get(startState).containsKey(actionVector));
      assertTrue(parser.rewards.get(startState).get(actionVector).containsKey(endState));
    }
  }

  @Test
  void parseSection_ShouldReplaceActionWildcardWithAllActionCombinations() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    var observations = Observation.listOf("O1", "O2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions))
      .setAgentObservations(List.of(observations, observations));

    var section = "R: * : A : A : O1 O2 : 0.1";
    parser.parseSection(section);
    var actionVectors = List.of(
      Vector.of(Action.listOf("A1", "A1")),
      Vector.of(Action.listOf("A1", "A2")),
      Vector.of(Action.listOf("A2", "A1")),
      Vector.of(Action.listOf("A2", "A2"))
    );
    var startState = State.from("A");
    var endState = State.from("A");

    for (var actionVector : actionVectors) {
      assertTrue(parser.rewards.containsKey(startState));
      assertTrue(parser.rewards.get(startState).containsKey(actionVector));
      assertTrue(parser.rewards.get(startState).get(actionVector).containsKey(endState));
    }
  }

  @Test
  void parseSection_ShouldGenerateProbabilitiesFromProbabilityDistribution() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    var observations = Observation.listOf("O1", "O2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions))
      .setAgentObservations(List.of(observations, observations));

    var section = "R: A1 A1 : A : A :\n" + "10 20 30 40";
    parser.parseSection(section);
    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var stateA = State.from("A");
    var expectedDistribution = Map.of(
      Vector.of(Observation.listOf("O1", "O1")), 10.0,
      Vector.of(Observation.listOf("O1", "O2")), 20.0,
      Vector.of(Observation.listOf("O2", "O1")), 30.0,
      Vector.of(Observation.listOf("O2", "O2")), 40.0
    );
    var actualDistribution = parser.rewards.get(stateA).get(actionVector).get(stateA);

    assertEquals(expectedDistribution, actualDistribution);
  }

  @Test
  void parseSection_ShouldGenerateProbabilitiesFromMatrixSyntax() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    var observations = Observation.listOf("O1", "O2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions))
      .setAgentObservations(List.of(observations, observations));

    var section = "R: A1 A1 : A :\n" + "20 50 20 10\n" + "30 20 20 30";
    parser.parseSection(section);

    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var stateA = State.from("A");
    var stateB = State.from("B");

    var expectedDistributionA = Map.of(
      Vector.of(Observation.listOf("O1", "O1")), 20.0,
      Vector.of(Observation.listOf("O1", "O2")), 50.0,
      Vector.of(Observation.listOf("O2", "O1")), 20.0,
      Vector.of(Observation.listOf("O2", "O2")), 10.0
    );

    var expectedDistributionB = Map.of(
      Vector.of(Observation.listOf("O1", "O1")), 30.0,
      Vector.of(Observation.listOf("O1", "O2")), 20.0,
      Vector.of(Observation.listOf("O2", "O1")), 20.0,
      Vector.of(Observation.listOf("O2", "O2")), 30.0
    );

    var actualDistributionA = parser.rewards.get(stateA).get(actionVector).get(stateA);
    var actualDistributionB = parser.rewards.get(stateA).get(actionVector).get(stateB);

    assertEquals(expectedDistributionA, actualDistributionA);
    assertEquals(expectedDistributionB, actualDistributionB);
  }

}