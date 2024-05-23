package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RewardTypeSectionParserTest {

  private RewardTypeSectionParser parser;

  @BeforeEach
  void setUp() {
    parser = new RewardTypeSectionParser();
  }

  @ParameterizedTest
  @ValueSource(strings = {"cost", "reward"})
  void parseRewardType_ShouldSetSection(String rewardType) {
    var section = "values: " + rewardType;
    var expected = DPOMDPRewardType.parse(rewardType);
    parser.parseSection(section);
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
  void parseSection_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseSection(invalidSection)
    );
  }

}