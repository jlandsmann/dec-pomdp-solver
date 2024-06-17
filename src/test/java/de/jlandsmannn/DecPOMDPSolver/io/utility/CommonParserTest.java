package de.jlandsmannn.DecPOMDPSolver.io.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommonParserTest {

  @BeforeEach
  void setUp() {
  }

  @Test
  void parseStatesAndTheirDistributions_ShouldParseDistributionsBasedOnTheirOrder() {
    var states = State.listOf("S1", "S2");
    var inputString = "0.4 0.6";
    var expected = Map.of(states.get(0), 0.4, states.get(1), 0.6);
    var actual = CommonParser.parseStatesAndTheirDistributions(states, inputString);
    assertEquals(expected, actual);

    inputString = "0.7 0.3";
    expected = Map.of(states.get(0), 0.7, states.get(1), 0.3);
    actual = CommonParser.parseStatesAndTheirDistributions(states, inputString);
    assertEquals(expected, actual);
  }

  @Test
  void parseStatesAndTheirDistributions_ShouldThrowErrorIfLessNumbersThanStates() {
    var states = State.listOf("S1", "S2", "S3");
    var inputString = "0.4 0.6";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseStatesAndTheirDistributions(states, inputString)
    );
  }

  @Test
  void parseStatesAndTheirDistributions_ShouldThrowErrorIfMoreNumbersThanStates() {
    var states = State.listOf("S1", "S2", "S3");
    var inputString = "0.4 0.6 0.2 0.2";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseStatesAndTheirDistributions(states, inputString)
    );
  }

  @Test
  void parseStatesAndTheirDistributions_ShouldThrowErrorIfNumberNegative() {
    var states = State.listOf("S1", "S2", "S3");
    var inputString = "0.4 0.6 -0.2";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseStatesAndTheirDistributions(states, inputString)
    );
  }

  @Test
  void parseStatesAndTheirDistributions_ShouldThrowErrorIfNamesOccur() {
    var states = State.listOf("S1", "S2", "S3");
    var inputString = "0.4 A B";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseStatesAndTheirDistributions(states, inputString)
    );
  }

  @Test
  void parseObservationVectorsAndTheirDistributions_ShouldParseDistributionsBasedOnTheirOrder() {
    var observations = Observation.listOf("O1", "O2");
    var observationVectors = List.of(observations, observations);
    var inputString = "0.2 0.2 0.2 0.4";
    var distribution = CommonParser.parseObservationVectorsAndTheirDistributions(observationVectors, inputString);

    var observationVector = Vector.of(observations.get(0), observations.get(0));
    var expected = 0.2;
    var actual = distribution.get(observationVector);
    assertEquals(expected, actual);

    observationVector = Vector.of(observations.get(0), observations.get(1));
    expected = 0.2;
    actual = distribution.get(observationVector);
    assertEquals(expected, actual);

    observationVector = Vector.of(observations.get(1), observations.get(0));
    expected = 0.2;
    actual = distribution.get(observationVector);
    assertEquals(expected, actual);

    observationVector = Vector.of(observations.get(1), observations.get(1));
    expected = 0.4;
    actual = distribution.get(observationVector);
    assertEquals(expected, actual);
  }

  @Test
  void parseObservationVectorsAndTheirDistributions_ShouldThrowErrorIfLessNumbersThanObservations() {
    var observations = Observation.listOf("O1", "O2");
    var observationVectors = List.of(observations, observations);
    var inputString = "0.4 0.4 0.2";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationVectorsAndTheirDistributions(observationVectors, inputString)
    );
  }

  @Test
  void parseObservationVectorsAndTheirDistributions_ShouldThrowErrorIfMoreNumbersThanObservations() {
    var observations = Observation.listOf("O1", "O2");
    var observationVectors = List.of(observations, observations);
    var inputString = "0.4 0.5 0.2 0.2 0.1";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationVectorsAndTheirDistributions(observationVectors, inputString)
    );
  }

  @Test
  void parseObservationVectorsAndTheirDistributions_ShouldThrowErrorIfNumberNegative() {
    var observations = Observation.listOf("O1", "O2");
    var observationVectors = List.of(observations, observations);
    var inputString = "0.4 0.4 0.2 -0.2";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationVectorsAndTheirDistributions(observationVectors, inputString)
    );
  }

  @Test
  void parseObservationVectorsAndTheirDistributions_ShouldThrowErrorIfNamesOccur() {
    var observations = Observation.listOf("O1", "O2");
    var observationVectors = List.of(observations, observations);
    var inputString = "0.4 0.2 A B";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationVectorsAndTheirDistributions(observationVectors, inputString)
    );
  }

  @Test
  void parseActionVector_ShouldThrowIfMoreElementsGivenThanAgents() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions);
    var inputString = "* * *";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseActionVector(possibleActions, inputString)
    );
  }

  @Test
  void parseActionVector_ShouldThrowIfLessElementsGivenThanAgents() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions, actions);
    var inputString = "* *";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseActionVector(possibleActions, inputString)
    );
  }

  @Test
  void parseActionVector_ShouldReturnAllExistingIfWildcardIsGiven() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions);
    var input = "*";
    var expected = List.of(
      Vector.of(actions.get(0), actions.get(0)),
      Vector.of(actions.get(0), actions.get(1)),
      Vector.of(actions.get(1), actions.get(0)),
      Vector.of(actions.get(1), actions.get(1))
    );
    var actual = CommonParser.parseActionVector(possibleActions, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseActionVector_ShouldMapActionsByName() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions);
    var input = "A1 A2";
    var expected = List.of(
      Vector.of(actions.get(0), actions.get(1))
    );
    var actual = CommonParser.parseActionVector(possibleActions, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseActionVector_ShouldThrowIfUnknownNameGiven() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions);
    var input = "A1 A3";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseActionVector(possibleActions, input);
    });
  }

  @Test
  void parseActionVector_ShouldMapActionsByIndex() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions);
    var input = "1 0";
    var expected = List.of(
      Vector.of(actions.get(1), actions.get(0))
    );
    var actual = CommonParser.parseActionVector(possibleActions, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseActionVector_ShouldThrowIfIndexOutOfBounds() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions);
    var input = "20 0";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseActionVector(possibleActions, input)
    );
  }

  @Test
  void parseActionVector_ShouldThrowIfIndexNegative() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions);
    var input = "-2 0";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseActionVector(possibleActions, input)
    );
  }

  @Test
  void parseActionVector_ShouldMapWildcardForSingleActionWithAllPossible() {
    var actions = Action.listOf("A1", "A2");
    var possibleActions = List.of(actions, actions);
    var input = "A1 *";
    var expected = List.of(
      Vector.of(actions.get(0), actions.get(0)),
      Vector.of(actions.get(0), actions.get(1))
    );
    var actual = CommonParser.parseActionVector(possibleActions, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseObservationVector_ShouldThrowIfMoreElementsGivenThanAgents() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations);
    var inputString = "* * *";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationVector(possibleObservations, inputString)
    );
  }

  @Test
  void parseObservationVector_ShouldThrowIfLessElementsGivenThanAgents() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations, observations);
    var inputString = "* *";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationVector(possibleObservations, inputString)
    );
  }

  @Test
  void parseObservationVector_ShouldReturnAllExistingIfWildcardIsGiven() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations);
    var input = "*";
    var expected = List.of(
      Vector.of(observations.get(0), observations.get(0)),
      Vector.of(observations.get(0), observations.get(1)),
      Vector.of(observations.get(1), observations.get(0)),
      Vector.of(observations.get(1), observations.get(1))
    );
    var actual = CommonParser.parseObservationVector(possibleObservations, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseObservationVector_ShouldMapObservationsByName() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations);
    var input = "O1 O2";
    var expected = List.of(
      Vector.of(observations.get(0), observations.get(1))
    );
    var actual = CommonParser.parseObservationVector(possibleObservations, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseObservationVector_ShouldThrowIfUnknownNameGiven() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations);
    var input = "O1 O3";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseObservationVector(possibleObservations, input);
    });
  }

  @Test
  void parseObservationVector_ShouldMapObservationsByIndex() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations);
    var input = "1 0";
    var expected = List.of(
      Vector.of(observations.get(1), observations.get(0))
    );
    var actual = CommonParser.parseObservationVector(possibleObservations, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseObservationVector_ShouldThrowIfIndexOutOfBounds() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations);
    var input = "20 0";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationVector(possibleObservations, input)
    );
  }

  @Test
  void parseObservationVector_ShouldThrowIfIndexNegative() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations);
    var input = "-2 0";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationVector(possibleObservations, input)
    );
  }

  @Test
  void parseObservationVector_ShouldMapWildcardForSingleObservationWithAllPossible() {
    var observations = Observation.listOf("O1", "O2");
    var possibleObservations = List.of(observations, observations);
    var input = "O1 *";
    var expected = List.of(
      Vector.of(observations.get(0), observations.get(0)),
      Vector.of(observations.get(0), observations.get(1))
    );
    var actual = CommonParser.parseObservationVector(possibleObservations, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseStateOrWildcard_ShouldMapStatesByName() {
    var states = State.listOf("S1", "S2");
    var input = "S1";
    var expected = List.of(states.get(0));
    var actual = CommonParser.parseStateOrWildcard(states, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseStateOrWildcard_ShouldThrowIfUnknownNameGiven() {
    var states = State.listOf("S1", "S2");
    var input = "S3";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseStateOrWildcard(states, input);
    });
  }

  @Test
  void parseStateOrWildcard_ShouldThrowIfEmptyInputGiven() {
    var states = State.listOf("S1", "S2");
    var input = "";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseStateOrWildcard(states, input);
    });
  }

  @Test
  void parseStateOrWildcard_ShouldThrowIfWhitespaceInInput() {
    var states = State.listOf("S1", "S2");
    var input = "S1 S2";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseStateOrWildcard(states, input);
    });
  }

  @Test
  void parseStateOrWildcard_ShouldMapStatesByIndex() {
    var states = State.listOf("S1", "S2");
    var input = "1";
    var expected = List.of(states.get(1));
    var actual = CommonParser.parseStateOrWildcard(states, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseStateOrWildcard_ShouldThrowIfIndexOutOfBounds() {
    var states = State.listOf("S1", "S2");
    var input = "20";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseStateOrWildcard(states, input)
    );
  }

  @Test
  void parseStateOrWildcard_ShouldThrowIfIndexNegative() {
    var states = State.listOf("S1", "S2");
    var input = "-2";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseStateOrWildcard(states, input)
    );
  }

  @Test
  void parseStateOrWildcard_ShouldMapWildcardForSingleStateWithAllPossible() {
    var states = State.listOf("S1", "S2");
    var input = "*";
    var expected = states;
    var actual = CommonParser.parseStateOrWildcard(states, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseActionOrWildcard_ShouldMapActionsByName() {
    var actions = Action.listOf("A1", "A2");
    var input = "A1";
    var expected = List.of(actions.get(0));
    var actual = CommonParser.parseActionOrWildcard(actions, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseActionOrWildcard_ShouldThrowIfUnknownNameGiven() {
    var actions = Action.listOf("A1", "A2");
    var input = "A3";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseActionOrWildcard(actions, input);
    });
  }

  @Test
  void parseActionOrWildcard_ShouldThrowIfEmptyInputGiven() {
    var actions = Action.listOf("A1", "A2");
    var input = "";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseActionOrWildcard(actions, input);
    });
  }

  @Test
  void parseActionOrWildcard_ShouldThrowIfWhitespaceInInput() {
    var actions = Action.listOf("A1", "A2");
    var input = "A1 A2";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseActionOrWildcard(actions, input);
    });
  }

  @Test
  void parseActionOrWildcard_ShouldMapActionsByIndex() {
    var actions = Action.listOf("A1", "A2");
    var input = "1";
    var expected = List.of(actions.get(1));
    var actual = CommonParser.parseActionOrWildcard(actions, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseActionOrWildcard_ShouldThrowIfIndexOutOfBounds() {
    var actions = Action.listOf("A1", "A2");
    var input = "20";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseActionOrWildcard(actions, input)
    );
  }

  @Test
  void parseActionOrWildcard_ShouldThrowIfIndexNegative() {
    var actions = Action.listOf("A1", "A2");
    var input = "-2";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseActionOrWildcard(actions, input)
    );
  }

  @Test
  void parseActionOrWildcard_ShouldMapWildcardForSingleActionWithAllPossible() {
    var actions = Action.listOf("A1", "A2");
    var input = "*";
    var expected = actions;
    var actual = CommonParser.parseActionOrWildcard(actions, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseObservationOrWildcard_ShouldMapObservationsByName() {
    var observations = Observation.listOf("O1", "O2");
    var input = "O1";
    var expected = List.of(observations.get(0));
    var actual = CommonParser.parseObservationOrWildcard(observations, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseObservationOrWildcard_ShouldThrowIfUnknownNameGiven() {
    var observations = Observation.listOf("O1", "O2");
    var input = "O3";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseObservationOrWildcard(observations, input);
    });
  }

  @Test
  void parseObservationOrWildcard_ShouldThrowIfEmptyInputGiven() {
    var observations = Observation.listOf("O1", "O2");
    var input = "";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseObservationOrWildcard(observations, input);
    });
  }

  @Test
  void parseObservationOrWildcard_ShouldThrowIfWhitespaceInInput() {
    var observations = Observation.listOf("O1", "O2");
    var input = "O1 O2";
    assertThrows(ParsingFailedException.class, () -> {
      CommonParser.parseObservationOrWildcard(observations, input);
    });
  }

  @Test
  void parseObservationOrWildcard_ShouldMapObservationsByIndex() {
    var observations = Observation.listOf("O1", "O2");
    var input = "1";
    var expected = List.of(observations.get(1));
    var actual = CommonParser.parseObservationOrWildcard(observations, input);
    assertEquals(expected, actual);
  }

  @Test
  void parseObservationOrWildcard_ShouldThrowIfIndexOutOfBounds() {
    var observations = Observation.listOf("O1", "O2");
    var input = "20";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationOrWildcard(observations, input)
    );
  }

  @Test
  void parseObservationOrWildcard_ShouldThrowIfIndexNegative() {
    var observations = Observation.listOf("O1", "O2");
    var input = "-2";
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseObservationOrWildcard(observations, input)
    );
  }

  @Test
  void parseObservationOrWildcard_ShouldMapWildcardForSingleObservationWithAllPossible() {
    var observations = Observation.listOf("O1", "O2");
    var input = "*";
    var expected = observations;
    var actual = CommonParser.parseObservationOrWildcard(observations, input);
    assertEquals(expected, actual);
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1.2", "1,2", "1.2", "A", "E"})
  void parseInteger_ShouldReturnOptionalEmptyIfInvalidIntegerStringGiven(String input) {
    var actual = CommonParser.parseInteger(input);
    assertTrue(actual.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 0, 1, 5000, Integer.MAX_VALUE, Integer.MIN_VALUE})
  void parseInteger_ShouldReturnIntegerIfValidIntegerGiven(int expected) {
    var input = String.valueOf(expected);
    var actual = CommonParser.parseInteger(input);
    assertTrue(actual.isPresent());
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1.2", "1,2", "0.0", "A", "E"})
  void parseIntegerOrThrow_ShouldThrowForInvalidInput(String input) {
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseIntegerOrThrow(input)
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1,2", "1,2", "A", "E"})
  void parseDouble_ShouldReturnOptionalEmptyIfInvalidDoubleStringGiven(String input) {
    var actual = CommonParser.parseDouble(input);
    assertTrue(actual.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(doubles = {-1.0, 0.0, 1.0, 5000.1, Double.MAX_VALUE, Double.MIN_VALUE})
  void parseDouble_ShouldReturnDoubleIfValidDoubleGiven(double expected) {
    var input = String.valueOf(expected);
    var actual = CommonParser.parseDouble(input);
    assertTrue(actual.isPresent());
  }

  @ParameterizedTest
  @ValueSource(strings = {"-1,2", "1,2", "A", "E"})
  void parseDoubleOrThrow_ShouldThrowForInvalidInput(String input) {
    assertThrows(ParsingFailedException.class, () ->
      CommonParser.parseDoubleOrThrow(input)
    );
  }
}