package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class StartSectionParserTest {

  private StartSectionParser parser;

  @BeforeEach
  void setUp() {
    parser = new StartSectionParser();
  }

  @Test
  void parseSection_ShouldThrowIfNoStatesHaveBeenInitialized() {
    var section = "start:\n" + "uniform";
    assertThrows(ParsingFailedException.class, () ->
      parser.parseSection(section)
    );
  }

  @Test
  void parseStart_ShouldParseUniformDistribution() {
    var section = "start:\n" + "uniform";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);
  }

  @Test
  void parseStart_ShouldParseProbabilisticDistribution() {
    var section = "start:\n" + "0.3 0.1 0.2 0.4";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);
  }

  @Test
  void parseStart_ShouldParseSingleNamedSectionState() {
    var section = "start: " + "A";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);
    var expectedProbability = 1D;
    var actualProbability = parser.initialBeliefState.getProbability(State.from("A"));
    assertEquals(expectedProbability, actualProbability);
  }

  @Test
  void parseStart_ShouldParseSingleIndexedSectionState() {
    var section = "start: " + "1";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);
    var expectedProbability = 1D;
    var actualProbability = parser.initialBeliefState.getProbability(State.from("B"));
    assertEquals(expectedProbability, actualProbability);
  }

  @Test
  void parseStart_ShouldParseNamedIncludedSectionStates() {
    var section = "start include: " + "A B";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityA = 0.5D;
    var actualProbabilityA = parser.initialBeliefState.getProbability(State.from("A"));
    var expectedProbabilityB = 0.5D;
    var actualProbabilityB = parser.initialBeliefState.getProbability(State.from("A"));
    assertEquals(expectedProbabilityA, actualProbabilityA);
    assertEquals(expectedProbabilityB, actualProbabilityB);
  }

  @Test
  void parseStart_ShouldParseIndexedIncludedSectionStates() {
    var section = "start include: " + "0 1";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityA = 0.5D;
    var actualProbabilityA = parser.initialBeliefState.getProbability(State.from("A"));
    var expectedProbabilityB = 0.5D;
    var actualProbabilityB = parser.initialBeliefState.getProbability(State.from("A"));
    assertEquals(expectedProbabilityA, actualProbabilityA);
    assertEquals(expectedProbabilityB, actualProbabilityB);
  }

  @Test
  void parseStart_ShouldParseMixedIncludedSectionStates() {
    var section = "start include: " + "A 1";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityA = 0.5D;
    var actualProbabilityA = parser.initialBeliefState.getProbability(State.from("A"));
    var expectedProbabilityB = 0.5D;
    var actualProbabilityB = parser.initialBeliefState.getProbability(State.from("A"));
    assertEquals(expectedProbabilityA, actualProbabilityA);
    assertEquals(expectedProbabilityB, actualProbabilityB);
  }

  @Test
  void parseStart_ShouldParseNamedExcludedSectionStates() {
    var section = "start exclude: " + "A B";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityC = 0.5D;
    var actualProbabilityC = parser.initialBeliefState.getProbability(State.from("C"));
    var expectedProbabilityD = 0.5D;
    var actualProbabilityD = parser.initialBeliefState.getProbability(State.from("D"));
    assertEquals(expectedProbabilityC, actualProbabilityC);
    assertEquals(expectedProbabilityD, actualProbabilityD);
  }

  @Test
  void parseStart_ShouldParseIndexedExcludedSectionStates() {
    var section = "start exclude: " + "0 1";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
    assertNotNull(parser.initialBeliefState);

    var expectedProbabilityC = 0.5D;
    var actualProbabilityC = parser.initialBeliefState.getProbability(State.from("C"));
    var expectedProbabilityD = 0.5D;
    var actualProbabilityD = parser.initialBeliefState.getProbability(State.from("D"));
    assertEquals(expectedProbabilityC, actualProbabilityC);
    assertEquals(expectedProbabilityD, actualProbabilityD);
  }

  @Test
  void parseStart_ShouldParseMixedExcludedSectionStates() {
    var section = "start exclude: " + "A 1";
    parser.setStates(State.listOf("A", "B", "C", "D"));
    parser.parseSection(section);
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
  void parseSection_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    parser.setStates(State.listOf("A", "B", "C", "D"));
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(invalidSection)
    );
  }
}