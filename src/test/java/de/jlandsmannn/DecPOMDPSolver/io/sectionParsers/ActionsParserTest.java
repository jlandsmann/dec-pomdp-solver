package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActionsParserTest {

  private ActionsParser parser;

  @BeforeEach
  void setUp() {
    parser = new ActionsParser();
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
    parser.setAgentNames(List.of("A1", "A2", "A3"));
    var section = "actions:\n" + "5\n" + "5";
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseActions(section)
    );
  }

  @Test
  void parseActions_ShouldThrowIfMoreActionsGivenThanAgents() {
    parser.setAgentNames(List.of("A1"));
    var section = "actions:\n" + "5\n" + "5";
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseActions(section)
    );
  }

  @Test
  void parseActions_ShouldGenerateActionNamesIfGivenNumerical() {
    parser.setAgentNames(List.of("A1"));
    var section = "actions:\n" + "5";
    parser.parseActions(section);
    var expectedActionCount = 5;
    var actualActionCount = parser.agentActions.get(0).size();
    assertEquals(expectedActionCount, actualActionCount);
  }

  @Test
  void parseActions_ShouldGenerateActionNamesIfGivenByNames() {
    parser.setAgentNames(List.of("A1"));
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
    parser.setAgentNames(List.of("A1"));
    assertThrows(
      IllegalArgumentException.class,
      () -> parser.parseActions(invalidSection)
    );
  }
}