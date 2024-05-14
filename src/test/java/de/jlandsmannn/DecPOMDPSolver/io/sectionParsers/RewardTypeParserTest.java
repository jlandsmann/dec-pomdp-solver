package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPRewardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RewardTypeParserTest {

  private RewardTypeParser parser;

  @BeforeEach
  void setUp() {
    parser = new RewardTypeParser();
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
      ParsingFailedException.class,
      () -> parser.parseRewardType(invalidSection)
    );
  }

}