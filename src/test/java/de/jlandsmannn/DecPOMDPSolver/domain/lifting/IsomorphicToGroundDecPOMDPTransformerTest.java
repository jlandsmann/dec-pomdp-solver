package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.io.IDPOMDPFileParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IsomorphicToGroundDecPOMDPTransformerTest {

  IsomorphicDecPOMDPWithStateController isomorphicDecPOMDPWithStateController;
  DecPOMDPWithStateController decPOMDPWithStateController;

  @BeforeEach
  void setUp() {
    isomorphicDecPOMDPWithStateController = IDPOMDPFileParser
      .parseDecPOMDP("problems/MedicalNanoscale2.idpomdp")
      .orElseThrow()
      .createDecPOMDP();
    decPOMDPWithStateController = IsomorphicToGroundDecPOMDPTransformer.transform(isomorphicDecPOMDPWithStateController);
  }

  @Test
  void transformDecPOMDP_ShouldCreateDecPOMDPWithSameStates() {
    var expected = isomorphicDecPOMDPWithStateController.getStates();
    var actual = decPOMDPWithStateController.getStates();
    assertEquals(expected, actual);
  }

  @Test
  void transformDecPOMDP_ShouldCreateDecPOMDPWithSameDiscountFactor() {
    var expected = isomorphicDecPOMDPWithStateController.getDiscountFactor();
    var actual = decPOMDPWithStateController.getDiscountFactor();
    assertEquals(expected, actual);
  }

  @Test
  void transformDecPOMDP_ShouldCreateDecPOMDPWithSameInitialBeliefState() {
    var expected = isomorphicDecPOMDPWithStateController.getInitialBeliefState();
    var actual = decPOMDPWithStateController.getInitialBeliefState();
    assertEquals(expected, actual);
  }

  @Test
  void transformDecPOMDP_ShouldCreateDecPOMDPWithSameAmountOfAgents() {
    var expected = isomorphicDecPOMDPWithStateController.getTotalAgentCount();
    var actual = decPOMDPWithStateController.getAgentCount();
    assertEquals(expected, actual);
  }
}