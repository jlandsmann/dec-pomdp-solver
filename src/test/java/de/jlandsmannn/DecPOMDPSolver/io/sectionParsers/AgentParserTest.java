package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AgentParserTest {

  private AgentParser parser;

  @BeforeEach
  void setUp() {
    parser = new AgentParser();
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

}