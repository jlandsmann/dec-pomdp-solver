package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObservationsSectionParserTest {

  private ObservationsSectionParser parser;

  @BeforeEach
  void setUp() {
    parser = new ObservationsSectionParser();
  }

  @Test
  void parseSection_ShouldThrowIfAgentsNotInitialized() {
    var section = "observations:\n" + "5";
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(section)
    );
  }

  @Test
  void parseObservations_ShouldThrowIfSectionNotGivenForEveryAgent() {
    parser.setAgentNames(List.of("A1", "A2", "A3"));
    var section = "observations:\n" + "5\n" + "5";
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(section)
    );
  }

  @Test
  void parseSection_ShouldGenerateObservationNamesIfGivenNumerical() {
    var section = "observations:\n" + "5";
    parser.setAgentNames(List.of("A1"));
    parser.parseSection(section);
    var expectedObservationCount = 5;
    var actualObservationCount = parser.agentObservations.get(0).size();
    assertEquals(expectedObservationCount, actualObservationCount);
  }

  @Test
  void parseSection_ShouldGenerateObservationNamesIfGivenByNames() {
    var section = "observations:\n" + "A B C D";
    parser.setAgentNames(List.of("A1"));
    parser.parseSection(section);
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
  void parseSection_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    parser.setAgentNames(List.of("A1"));
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(invalidSection)
    );
  }

}