package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

  @Test
  void parseAgents_ShouldThrowIfInvalidSectionGiven() {
    var invalidSections = List.of(
      "agent: A B C D",
      "agent: 2",
      "agents: -2",
      "agents: 0",
      "agents: 2 2"
    );
    for (var invalidSection : invalidSections) {
      assertThrows(
        IllegalArgumentException.class,
        () -> parser.parseAgents(invalidSection)
      );
    }
  }

  @Test
  void parseDiscount() {
  }

  @Test
  void parseValue() {
  }

  @Test
  void parseStates() {
  }

  @Test
  void parseStart() {
  }

  @Test
  void parseActions() {
  }

  @Test
  void parseObservations() {
  }

  @Test
  void parseTransitionEntry() {
  }

  @Test
  void parseRewardEntry() {
  }

  @Test
  void parseObservationEntry() {
  }
}