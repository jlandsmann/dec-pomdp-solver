package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatesSectionParserTest {

  private StatesSectionParser parser;

  @BeforeEach
  void setUp() {
    parser = new StatesSectionParser();
  }

  @Test
  void parseSection_ShouldCreateStateNamesIfGivenNumerical() {
    var statesDefinedByNumber = "states: 5";
    parser.parseSection(statesDefinedByNumber);

    var expectedStateCount = 5;
    var actualStates = parser.getStates();
    assertEquals(expectedStateCount, actualStates.size());
  }

  @Test
  void parseSection_ShouldCreateStateNamesIfGivenByName() {
    var statesDefinedByName = "states: A B C D";
    parser.parseSection(statesDefinedByName);

    var expectedStates = State.listOf("A", "B", "C", "D");
    var actualStates = parser.getStates();
    assertEquals(expectedStates, actualStates);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "state: A B C D",
    "state: 2",
    "states: -2",
    "states: 0",
    "states: 2 2"
  })
  void parseSection_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(invalidSection)
    );
  }

}