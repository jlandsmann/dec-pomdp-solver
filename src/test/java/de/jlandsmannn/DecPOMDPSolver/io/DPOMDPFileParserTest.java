package de.jlandsmannn.DecPOMDPSolver.io;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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