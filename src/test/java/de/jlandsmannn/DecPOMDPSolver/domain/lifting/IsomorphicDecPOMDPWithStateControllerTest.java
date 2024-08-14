package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IsomorphicDecPOMDPWithStateControllerTest {

  IsomorphicDecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    decPOMDP = DecPOMDPGenerator.getIsomorphicDecPOMDP();
  }

  @Test
  void getNodeCombinations() {
    var totalAgentCount = decPOMDP.getTotalAgentCount();
    var nodeCombinations = decPOMDP.getNodeCombinations();

    for (var nodeVector : nodeCombinations) {
      assertEquals(totalAgentCount, nodeVector.size());
    }
  }
}