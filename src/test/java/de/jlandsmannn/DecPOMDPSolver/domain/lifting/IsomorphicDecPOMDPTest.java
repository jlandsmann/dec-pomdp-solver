package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsomorphicDecPOMDPTest {

  IsomorphicDecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    decPOMDP = DecPOMDPGenerator.getIsomorphicDecPOMDP(3);
  }

  @Test
  void getGroundings_ShouldReturnAnyCombinationOfElementsInVector() {
    var actionVector = decPOMDP.getActionCombinations().get(0);
    var groundings = decPOMDP.getGroundings(actionVector);
    var expectedNumberOfGroundings =  decPOMDP.getAgents().stream()
      .map(IsomorphicAgentWithStateController::getPartitionSize)
      .reduce((a,b) -> a * b)
      .orElse(0);

    assertEquals(expectedNumberOfGroundings, groundings.size());
  }
}