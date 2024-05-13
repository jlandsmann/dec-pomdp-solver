package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DPOMDPFileParserTest {

  @Mock
  private DecPOMDPBuilder builder;

  @InjectMocks
  private DPOMDPFileParser parser;

  @Test
  void parseDecPOMDP() {
  }

  @Test
  void doParseDecPOMDP() {
  }

  @Test
  void parseLine() {
  }

  @Test
  void parseCurrentSection() {
  }

  @Test
  void startNewSection() {
  }

  @Test
  void parseSection() {
  }

  @Test
  void parseAgents_ShouldCreateAgentNamesIfGivenNumerical() {
    var agentsDefinedByNumber = "agents: 5";
    var expectedAgentCount = 5;
    parser.parseAgents(agentsDefinedByNumber);
    var actualAgentCount = parser.agentNames.size();
    assertEquals(expectedAgentCount, actualAgentCount);
  }

  @Test
  void parseAgents_ShouldCreateAgentNamesIfGivenByName() {
    var agentsDefinedByName = "agents: A B C D";
    var expectedAgentNames = List.of("A", "B", "C", "D");
    parser.parseAgents(agentsDefinedByName);
    var actualAgentNames = parser.agentNames;
    assertEquals(expectedAgentNames, actualAgentNames);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "agent: A B C D",
    "agent: 2",
    "agents: -2",
    "agents: 0",
    "agents: 2 2"
  })
  void parseAgents_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseAgents(invalidSection)
    );
  }

  @ParameterizedTest
  @ValueSource(doubles = {0, 0.1, 0.25, 0.5, 0.9, 0.99, 1.0})
  void parseDiscount_ShouldSetDiscountFactor(double discountFactor) {
    var section = "discount: " + discountFactor;
    parser.parseDiscount(section);
    verify(builder).setDiscountFactor(discountFactor);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "discounts: 1.0",
    "discount: 2",
    "discount: -0.1",
    "discount: -0.2",
    "discount: 1.1"
  })
  void parseDiscount_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseDiscount(invalidSection)
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"cost", "reward"})
  void parseRewardType_ShouldSetRewardType(String rewardType) {
    var section = "values: " + rewardType;
    var expected = DPOMDPRewardType.parse(rewardType);
    parser.parseRewardType(section);
    var actual = parser.rewardType;
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "value: cost",
    "value: reward",
    "values: c",
    "values: r",
    "values: 1"
  })
  void parseRewardType_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseRewardType(invalidSection)
    );
  }

  @Test
  void parseStates_ShouldCreateStateNamesIfGivenNumerical() {
    var statesDefinedByNumber = "states: 5";
    var expectedStateCount = 5;
    parser.parseStates(statesDefinedByNumber);
    verify(builder).addStates(argThat(c -> c.size() == expectedStateCount));
  }

  @Test
  void parseStates_ShouldCreateStateNamesIfGivenByName() {
    var statesDefinedByName = "states: A B C D";
    var expectedStateNames = State.listOf("A", "B", "C", "D");
    parser.parseStates(statesDefinedByName);
    verify(builder).addStates(expectedStateNames);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "state: A B C D",
    "state: 2",
    "states: -2",
    "states: 0",
    "states: 2 2"
  })
  void parseStates_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseStates(invalidSection)
    );
  }

  @Test
  void parseStart_ShouldThrowIfNoStatesHaveBeenInitialized() {
    var section = "start:\n" + "uniform";
    assertThrows(IllegalStateException.class, () ->
      parser.parseStart(section)
    );
  }

  @Test
  void parseStart_ShouldParseUniformDistribution() {
    var section = "start:\n" + "uniform";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);
  }

  @Test
  void parseStart_ShouldParseProbabilisticDistribution() {
    var section = "start:\n" + "0.3 0.1 0.2 0.4";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);
  }

  @Test
  void parseStart_ShouldParseSingleNamedStartState() {
    var section = "start: " + "A";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);
    var expectedProbability = 1D;
    var actualProbability = parser.initialBeliefState.getProbability(State.from("A"));
    assertEquals(expectedProbability, actualProbability);
  }

  @Test
  void parseStart_ShouldParseSingleIndexedStartState() {
    var section = "start: " + "1";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);
    var expectedProbability = 1D;
    var actualProbability = parser.initialBeliefState.getProbability(State.from("B"));
    assertEquals(expectedProbability, actualProbability);
  }

  @Test
  void parseStart_ShouldParseNamedIncludedStartStates() {
    var section = "start include: " + "A B";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityA = 0.5D;
    var actualProbabilityA = parser.initialBeliefState.getProbability(State.from("A"));
    var expectedProbabilityB = 0.5D;
    var actualProbabilityB = parser.initialBeliefState.getProbability(State.from("A"));
    assertEquals(expectedProbabilityA, actualProbabilityA);
    assertEquals(expectedProbabilityB, actualProbabilityB);
  }

  @Test
  void parseStart_ShouldParseIndexedIncludedStartStates() {
    var section = "start include: " + "0 1";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityA = 0.5D;
    var actualProbabilityA = parser.initialBeliefState.getProbability(State.from("A"));
    var expectedProbabilityB = 0.5D;
    var actualProbabilityB = parser.initialBeliefState.getProbability(State.from("A"));
    assertEquals(expectedProbabilityA, actualProbabilityA);
    assertEquals(expectedProbabilityB, actualProbabilityB);
  }

  @Test
  void parseStart_ShouldParseMixedIncludedStartStates() {
    var section = "start include: " + "A 1";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityA = 0.5D;
    var actualProbabilityA = parser.initialBeliefState.getProbability(State.from("A"));
    var expectedProbabilityB = 0.5D;
    var actualProbabilityB = parser.initialBeliefState.getProbability(State.from("A"));
    assertEquals(expectedProbabilityA, actualProbabilityA);
    assertEquals(expectedProbabilityB, actualProbabilityB);
  }

  @Test
  void parseStart_ShouldParseNamedExcludedStartStates() {
    var section = "start exclude: " + "A B";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityC = 0.5D;
    var actualProbabilityC = parser.initialBeliefState.getProbability(State.from("C"));
    var expectedProbabilityD = 0.5D;
    var actualProbabilityD = parser.initialBeliefState.getProbability(State.from("D"));
    assertEquals(expectedProbabilityC, actualProbabilityC);
    assertEquals(expectedProbabilityD, actualProbabilityD);
  }

  @Test
  void parseStart_ShouldParseIndexedExcludedStartStates() {
    var section = "start exclude: " + "0 1";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityC = 0.5D;
    var actualProbabilityC = parser.initialBeliefState.getProbability(State.from("C"));
    var expectedProbabilityD = 0.5D;
    var actualProbabilityD = parser.initialBeliefState.getProbability(State.from("D"));
    assertEquals(expectedProbabilityC, actualProbabilityC);
    assertEquals(expectedProbabilityD, actualProbabilityD);
  }

  @Test
  void parseStart_ShouldParseMixedExcludedStartStates() {
    var section = "start exclude: " + "A 1";
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    parser.parseStart(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityC = 0.5D;
    var actualProbabilityC = parser.initialBeliefState.getProbability(State.from("C"));
    var expectedProbabilityD = 0.5D;
    var actualProbabilityD = parser.initialBeliefState.getProbability(State.from("D"));
    assertEquals(expectedProbabilityC, actualProbabilityC);
    assertEquals(expectedProbabilityD, actualProbabilityD);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "start:\nA",
    "start: A B",
    "start: 2 4",
    "start exclude:\n0",
    "start include: 2 -2"
  })
  void parseStart_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    when(builder.getStates()).thenReturn(State.listOf("A", "B", "C", "D"));
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseStart(invalidSection)
    );
  }

  @Test
  void parseActions_ShouldThrowIfAgentsNotInitialized() {
    var section = "actions:\n" + "5";
    assertThrows(
      IllegalStateException.class,
      () -> parser.parseActions(section)
    );
  }

  @Test
  void parseActions_ShouldThrowIfActionsNotGivenForEveryAgent() {
    parser.agentNames = List.of("A1", "A2", "A3");
    var section = "actions:\n" + "5\n" + "5";
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseActions(section)
    );
  }

  @Test
  void parseActions_ShouldThrowIfMoreActionsGivenThanAgents() {
    parser.agentNames = List.of("A1");
    var section = "actions:\n" + "5\n" + "5";
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseActions(section)
    );
  }

  @Test
  void parseActions_ShouldGenerateActionNamesIfGivenNumerical() {
    parser.agentNames = List.of("A1");
    var section = "actions:\n" + "5";
    parser.parseActions(section);
    var expectedActionCount = 5;
    var actualActionCount = parser.agentActions.get(0).size();
    assertEquals(expectedActionCount, actualActionCount);
  }

  @Test
  void parseActions_ShouldGenerateActionNamesIfGivenByNames() {
    parser.agentNames = List.of("A1");
    var section = "actions:\n" + "A B C D";
    parser.parseActions(section);
    var expectedActionCount = 4;
    var actualActionCount = parser.agentActions.get(0).size();
    assertEquals(expectedActionCount, actualActionCount);

    var actualActions = parser.agentActions.get(0);
    assertEquals("A", actualActions.get(0).name());
    assertEquals("B", actualActions.get(1).name());
    assertEquals("C", actualActions.get(2).name());
    assertEquals("D", actualActions.get(3).name());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "actions: 5",
    "actions: A B",
    "actions:\n0 5",
    "actions:\n-2",
    "actions:\n2 A",
    "actions:\nB 2",
  })
  void parseActions_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    parser.agentNames = List.of("A1");
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseActions(invalidSection)
    );
  }

  @Test
  void parseObservations_ShouldThrowIfAgentsNotInitialized() {
    var section = "observations:\n" + "5";
    assertThrows(
      IllegalStateException.class,
      () -> parser.parseObservations(section)
    );
  }

  @Test
  void parseObservations_ShouldThrowIfActionsNotInitialized() {
    parser.agentNames = List.of("A1", "A2", "A3");
    var section = "observations:\n" + "5";
    assertThrows(
      IllegalStateException.class,
      () -> parser.parseObservations(section)
    );
  }

  @Test
  void parseObservations_ShouldThrowIfActionsNotGivenForEveryAgent() {
    parser.agentNames = List.of("A1", "A2", "A3");
    parser.agentActions = List.of(
      Action.listOf("A1-A1"),
      Action.listOf("A2-A1"),
      Action.listOf("A3-A1")
    );
    var section = "observations:\n" + "5\n" + "5";
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseObservations(section)
    );
  }

  @Test
  void parseObservations_ShouldGenerateObservationNamesIfGivenNumerical() {
    parser.agentNames = List.of("A1");
    parser.agentActions = List.of(List.of());
    var section = "observations:\n" + "5";
    parser.parseObservations(section);
    var expectedObservationCount = 5;
    var actualObservationCount = parser.agentObservations.get(0).size();
    assertEquals(expectedObservationCount, actualObservationCount);
  }

  @Test
  void parseObservations_ShouldGenerateObservationNamesIfGivenByNames() {
    parser.agentNames = List.of("A1");
    parser.agentActions = List.of(List.of());
    var section = "observations:\n" + "A B C D";
    parser.parseObservations(section);
    var expectedObservationCount = 4;
    var actualObservationCount = parser.agentObservations.get(0).size();
    assertEquals(expectedObservationCount, actualObservationCount);

    var actualObservations = parser.agentObservations.get(0);
    assertEquals("A", actualObservations.get(0).name());
    assertEquals("B", actualObservations.get(1).name());
    assertEquals("C", actualObservations.get(2).name());
    assertEquals("D", actualObservations.get(3).name());
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "observations: 5",
    "observations: A B",
    "observations:\n0 5",
    "observations:\n-2",
    "observations:\n2 A",
    "observations:\nB 2",
  })
  void parseObservations_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    parser.agentNames = List.of("A1");
    parser.agentActions = List.of(List.of());
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseObservations(invalidSection)
    );
  }

  @Test
  void parseTransitionEntry_ShouldThrowIfStatesNotGiven() {
    var section = "T: * : * : * : 0.1";
    assertThrows(IllegalStateException.class, () -> {
      parser.parseTransitionEntry(section);
    });
  }

  @Test
  void parseTransitionEntry_ShouldThrowIfAgentActionsNotGiven() {
    when(builder.getStates()).thenReturn(State.listOf("A", "B"));
    var section = "T: * : * : * : 0.1";
    assertThrows(IllegalStateException.class, () -> {
      parser.parseTransitionEntry(section);
    });
  }

  @Test
  void parseTransitionEntry_ShouldReplaceStartStateWildcardWithAllStates() {
    var states = State.listOf("A", "B");
    var actions = Action.listOf("A1", "A2");

    when(builder.getStates()).thenReturn(states);
    parser.agentActions = List.of(actions, actions);

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

    when(builder.getStates()).thenReturn(states);
    parser.agentActions = List.of(actions, actions);

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

    when(builder.getStates()).thenReturn(states);
    parser.agentActions = List.of(actions, actions);

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

    when(builder.getStates()).thenReturn(states);
    parser.agentActions = List.of(actions, actions);

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

    when(builder.getStates()).thenReturn(states);
    parser.agentActions = List.of(actions, actions);

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

    when(builder.getStates()).thenReturn(states);
    parser.agentActions = List.of(actions, actions);

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

    when(builder.getStates()).thenReturn(states);
    parser.agentActions = List.of(actions, actions);

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

    when(builder.getStates()).thenReturn(states);
    parser.agentActions = List.of(actions, actions);

    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseTransitionEntry(invalidSection)
    );
  }

  @Test
  void parseRewardEntry() {
  }

  @Test
  void parseObservationEntry() {
  }
}