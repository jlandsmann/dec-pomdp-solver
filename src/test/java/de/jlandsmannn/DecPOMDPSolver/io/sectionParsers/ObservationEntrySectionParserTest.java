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

class ObservationEntrySectionParserTest {

  private ObservationEntrySectionParser parser;

  @BeforeEach
  void setUp() {
    parser = new ObservationEntrySectionParser();
  }

  @Test
  void parseSection_ShouldThrowIfStatesNotInitialized() {
    var section = "O: * : * : * : 0.1";
    assertThrows(ParsingFailedException.class, () -> {
      parser.parseSection(section);
    });
  }

  @Test
  void parseSection_ShouldThrowIfAgentActionsNotInitialized() {
    var states = State.listOf("A", "B");
    parser.setStates(states);
    var section = "O: * : * : * : 0.1";
    assertThrows(ParsingFailedException.class, () -> {
      parser.parseSection(section);
    });
  }

  @Test
  void parseSection_ShouldThrowIfAgentObservationsNotInitialized() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions));
    var section = "O: * : * : * : 0.1";
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

    var section = "O: A1 A2 : * : O1 O2 : 0.1";
    var actionVector = Vector.of(Action.listOf("A1", "A2"));
    parser.parseSection(section);

    for (var state : states) {
      assertTrue(parser.observations.containsKey(actionVector));
      assertTrue(parser.observations.get(actionVector).containsKey(state));
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

    var section = "O: A1 * : A : O1 O2 : 0.1";
    parser.parseSection(section);
    var actionVectors = List.of(
      Vector.of(Action.listOf("A1", "A1")),
      Vector.of(Action.listOf("A1", "A2"))
    );
    var endState = State.from("A");

    for (var actionVector : actionVectors) {
      assertTrue(parser.observations.containsKey(actionVector));
      assertTrue(parser.observations.get(actionVector).containsKey(endState));
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

    var section = "O: * : A : O1 O2 : 0.1";
    parser.parseSection(section);
    var actionVectors = List.of(
      Vector.of(Action.listOf("A1", "A1")),
      Vector.of(Action.listOf("A1", "A2")),
      Vector.of(Action.listOf("A2", "A1")),
      Vector.of(Action.listOf("A2", "A2"))
    );
    var endState = State.from("A");

    for (var actionVector : actionVectors) {
      assertTrue(parser.observations.containsKey(actionVector));
      assertTrue(parser.observations.get(actionVector).containsKey(endState));
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

    var section = "O: A1 A1 : A :\n" + "0.1 0.2 0.3 0.4";
    parser.parseSection(section);
    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var stateA = State.from("A");
    var expectedDistribution = Map.of(
      Vector.of(Observation.listOf("O1", "O1")), 0.1,
      Vector.of(Observation.listOf("O1", "O2")), 0.2,
      Vector.of(Observation.listOf("O2", "O1")), 0.3,
      Vector.of(Observation.listOf("O2", "O2")), 0.4
    );
    var actualDistribution = parser.observations.get(actionVector).get(stateA);

    assertEquals(expectedDistribution, actualDistribution);
  }

  @Test
  void parseSection_ShouldCreateUniformDistributionWhenUniformKeywordUsed() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");
    var observations = Observation.listOf("O1", "O2");
    parser
      .setStates(states)
      .setAgentActions(List.of(actions, actions))
      .setAgentObservations(List.of(observations, observations));

    var section = "O: A1 A1 :\nuniform";
    parser.parseSection(section);
    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var endState = State.from("A");

    var expectedDistribution = Map.of(
      Vector.of(Observation.listOf("O1", "O1")), 0.25,
      Vector.of(Observation.listOf("O1", "O2")), 0.25,
      Vector.of(Observation.listOf("O2", "O1")), 0.25,
      Vector.of(Observation.listOf("O2", "O2")), 0.25
    );
    assertTrue(parser.observations.containsKey(actionVector));
    var actualDistribution = parser.observations.get(actionVector).get(endState);
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

    var section = "O: A1 A1 :\n" + "0.2 0.5 0.2 0.1\n" + "0.3 0.2 0.2 0.3";
    parser.parseSection(section);

    var actionVector = Vector.of(Action.listOf("A1", "A1"));
    var stateA = State.from("A");
    var stateB = State.from("B");

    var expectedDistributionA = Map.of(
      Vector.of(Observation.listOf("O1", "O1")), 0.2,
      Vector.of(Observation.listOf("O1", "O2")), 0.5,
      Vector.of(Observation.listOf("O2", "O1")), 0.2,
      Vector.of(Observation.listOf("O2", "O2")), 0.1
    );

    var expectedDistributionB = Map.of(
      Vector.of(Observation.listOf("O1", "O1")), 0.3,
      Vector.of(Observation.listOf("O1", "O2")), 0.2,
      Vector.of(Observation.listOf("O2", "O1")), 0.2,
      Vector.of(Observation.listOf("O2", "O2")), 0.3
    );

    var actualDistributionA = parser.observations.get(actionVector).get(stateA);
    var actualDistributionB = parser.observations.get(actionVector).get(stateB);

    assertEquals(expectedDistributionA, actualDistributionA);
    assertEquals(expectedDistributionB, actualDistributionB);
  }

}