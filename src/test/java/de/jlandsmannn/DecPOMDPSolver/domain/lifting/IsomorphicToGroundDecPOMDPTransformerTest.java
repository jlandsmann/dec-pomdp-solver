package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.io.IDPOMDPFileParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

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

  @Test
  void transformDecPOMDP_ShouldCreateIndividualAgents() {
    var agents = decPOMDPWithStateController.getAgents();

    for (int i = 0; i < agents.size(); i++) {
      for (int j = 0; j < agents.size(); j++) {
        if (i == j) continue;
        assertNotSame(agents.get(i), agents.get(j));
      }
    }
  }

  @Test
  void transformDecPOMDP_ShouldCreateIndividualControllerForAgents() {
    var agents = decPOMDPWithStateController.getAgents();

    for (int i = 0; i < agents.size(); i++) {
      for (int j = 0; j < agents.size(); j++) {
        if (i == j) continue;
        assertNotSame(agents.get(i).getController(), agents.get(j).getController());
      }
    }
  }
}