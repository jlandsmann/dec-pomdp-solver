package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObservationsParserTest {

  private ObservationsParser parser;

  @BeforeEach
  void setUp() {
    parser = new ObservationsParser();
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
  void parseObservations_ShouldThrowIfObservationsNotGivenForEveryAgent() {
    parser.setAgentNames(List.of("A1", "A2", "A3"));
    var section = "observations:\n" + "5\n" + "5";
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseObservations(section)
    );
  }

  @Test
  void parseObservations_ShouldGenerateObservationNamesIfGivenNumerical() {
    var section = "observations:\n" + "5";
    parser.setAgentNames(List.of("A1"));
    parser.parseObservations(section);
    var expectedObservationCount = 5;
    var actualObservationCount = parser.agentObservations.get(0).size();
    assertEquals(expectedObservationCount, actualObservationCount);
  }

  @Test
  void parseObservations_ShouldGenerateObservationNamesIfGivenByNames() {
    var section = "observations:\n" + "A B C D";
    parser.setAgentNames(List.of("A1"));
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
    parser.setAgentNames(List.of("A1"));
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseObservations(invalidSection)
    );
  }

}