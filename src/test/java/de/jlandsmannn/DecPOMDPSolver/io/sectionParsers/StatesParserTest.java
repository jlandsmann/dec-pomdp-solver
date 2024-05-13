package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

class StatesParserTest {

  private StatesParser parser;

  @BeforeEach
  void setUp() {
    parser = new StatesParser();
  }

  @Test
  void parseStates_ShouldCreateStateNamesIfGivenNumerical() {
    var statesDefinedByNumber = "states: 5";
    parser.parseStates(statesDefinedByNumber);

    var expectedStateCount = 5;
    var actualStates = parser.getStates();
    assertEquals(expectedStateCount, actualStates.size());
  }

  @Test
  void parseStates_ShouldCreateStateNamesIfGivenByName() {
    var statesDefinedByName = "states: A B C D";
    parser.parseStates(statesDefinedByName);

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
  void parseStates_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseStates(invalidSection)
    );
  }

}