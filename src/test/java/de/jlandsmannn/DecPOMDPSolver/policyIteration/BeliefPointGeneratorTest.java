package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeliefPointGeneratorTest {

  private BeliefPointGenerator beliefPointGenerator;
  private DecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    beliefPointGenerator = new BeliefPointGenerator();
    decPOMDP = DecPOMDPGenerator.generateDecPOMDPWithTwoAgents();
  }

  @Test
  void setDecPOMDP() {
    assertDoesNotThrow(() ->
      beliefPointGenerator.setDecPOMDP(decPOMDP)
    );
  }

  @Test
  void setDesiredNumberOfBeliefPoints() {
    assertDoesNotThrow(() ->
      beliefPointGenerator.setDesiredNumberOfBeliefPoints(10)
    );
  }

  @Test
  void setInitialBeliefState() {
    assertDoesNotThrow(() ->
      Distribution.createRandomDistribution(decPOMDP.getStates())
    );
  }

  @Test
  void generateRandomPolicies_ShouldThrowIfDecPOMDPNotSet() {
    assertThrows(IllegalStateException.class, () -> {
      beliefPointGenerator.generateRandomPolicies();
    });
  }

  @Test
  void generateRandomPolicies_ShouldNotThrowIfDecPOMDPSet() {
    beliefPointGenerator.setDecPOMDP(decPOMDP);
    assertDoesNotThrow(() -> {
      beliefPointGenerator.generateRandomPolicies();
    });
  }

  @Test
  void generateBeliefPointsForAgent_ShouldThrowIfDecPOMDPNotSet() {
    beliefPointGenerator.setInitialBeliefState(Distribution.createRandomDistribution(decPOMDP.getStates()));
    beliefPointGenerator.setDesiredNumberOfBeliefPoints(10);
    assertThrows(IllegalStateException.class, () -> {
      beliefPointGenerator.generateBeliefPointsForAgent(decPOMDP.getAgents().get(0));
    });
  }

  @Test
  void generateBeliefPointsForAgent_ShouldThrowIfDesiredNumberOfBeliefPointsNotSet() {
    beliefPointGenerator.setDecPOMDP(decPOMDP);
    beliefPointGenerator.setInitialBeliefState(Distribution.createRandomDistribution(decPOMDP.getStates()));
    beliefPointGenerator.generateRandomPolicies();
    assertThrows(IllegalStateException.class, () -> {
      beliefPointGenerator.generateBeliefPointsForAgent(decPOMDP.getAgents().get(0));
    });
  }

  @Test
  void generateBeliefPointsForAgent_ShouldThrowIfInitialBeliefStateNotSet() {
    beliefPointGenerator.setDecPOMDP(decPOMDP);
    beliefPointGenerator.setDesiredNumberOfBeliefPoints(10);
    beliefPointGenerator.generateRandomPolicies();
    assertThrows(IllegalStateException.class, () -> {
      beliefPointGenerator.generateBeliefPointsForAgent(decPOMDP.getAgents().get(0));
    });
  }

  @Test
  void generateBeliefPointsForAgent_ShouldThrowIfRandomPoliciesNotGenerated() {
    beliefPointGenerator.setDecPOMDP(decPOMDP);
    beliefPointGenerator.setInitialBeliefState(Distribution.createRandomDistribution(decPOMDP.getStates()));
    beliefPointGenerator.setDesiredNumberOfBeliefPoints(10);
    assertThrows(IllegalStateException.class, () -> {
      beliefPointGenerator.generateBeliefPointsForAgent(decPOMDP.getAgents().get(0));
    });
  }

  @Test
  void generateBeliefPointsForAgent_ShouldNotThrowIfAllDependenciesPresent() {
    beliefPointGenerator.setDecPOMDP(decPOMDP);
    beliefPointGenerator.setInitialBeliefState(Distribution.createRandomDistribution(decPOMDP.getStates()));
    beliefPointGenerator.setDesiredNumberOfBeliefPoints(10);
    beliefPointGenerator.generateRandomPolicies();
    assertDoesNotThrow(() -> {
      beliefPointGenerator.generateBeliefPointsForAgent(decPOMDP.getAgents().get(0));
    });
  }
}