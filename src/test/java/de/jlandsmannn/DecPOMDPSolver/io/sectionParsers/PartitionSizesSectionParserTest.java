package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PartitionSizesSectionParserTest {

  private PartitionSizesSectionParser parser;

  @BeforeEach
  void setUp() {
    parser = new PartitionSizesSectionParser();
  }

  @Test
  void parseSection_ShouldThrowIfAgentsNotInitialized() {
    var section = "partitionSizes:\n" + "5\n" + "5";
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(section)
    );
  }

  @Test
  void parseSection_ShouldThrowIfSizeNotGivenForEveryAgent() {
    parser.setAgentNames(List.of("A1", "A2", "A3"));
    var section = "partitionSizes:\n" + "5\n" + "5";
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(section)
    );
  }

  @Test
  void parseSection_ShouldThrowIfMoreSizesGivenThanAgents() {
    parser.setAgentNames(List.of("A1"));
    var section = "partitionSizes:\n" + "5\n" + "5";
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(section)
    );
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "partitionSizes: 1 1",
    "partitionSizes: 1\n1",
    "partitionSizes:\n0\n1",
    "partitionSizes:\n1\n0",
    "partitionSizes:\n-1\n1",
    "partitionSizes:\n1\n-1",
    "partitionSizes:\nA\n2",
    "partitionSizes:\n2\nB",
  })
  void parseSection_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    parser.setAgentNames(List.of("A1", "A2"));
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(invalidSection)
    );
  }

  @Test
  void parseSection_ShouldNotThrowIfAsManySizesGivenAsAgentNames() {
    parser.setAgentNames(List.of("A1", "A2"));
    var section = "partitionSizes:\n" + "5\n" + "5";
    assertDoesNotThrow(() -> parser.parseSection(section));
  }

  @Test
  void parseSection_ShouldSetPartitionSizes() {
    parser.setAgentNames(List.of("A1", "A2", "A3", "A4"));
    var section = "partitionSizes:\n" + "5\n" + "4\n" + "3\n" + "2";
    parser.parseSection(section);

    var expectedPartitionSize1 = 5;
    var expectedPartitionSize2 = 4;
    var expectedPartitionSize3 = 3;
    var expectedPartitionSize4 = 2;

    var actualPartitionSize1 = parser.getPartitionSizes().get(0);
    var actualPartitionSize2 = parser.getPartitionSizes().get(1);
    var actualPartitionSize3 = parser.getPartitionSizes().get(2);
    var actualPartitionSize4 = parser.getPartitionSizes().get(3);

    assertEquals(expectedPartitionSize1, actualPartitionSize1);
    assertEquals(expectedPartitionSize2, actualPartitionSize2);
    assertEquals(expectedPartitionSize3, actualPartitionSize3);
    assertEquals(expectedPartitionSize4, actualPartitionSize4);

  }

}