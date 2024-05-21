package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BeliefPointGeneratorTest {

  private HeuristicPolicyIterationConfig config;
  private BeliefPointGenerator beliefPointGenerator;
  private DecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    config = HeuristicPolicyIterationConfig.getDefault();
    beliefPointGenerator = new BeliefPointGenerator(config);
    decPOMDP = DecPOMDPGenerator.getDecTigerPOMDP();
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
  void generateBeliefPoints_ShouldThrowIfDecPOMDPNotSetForAgent() {
    beliefPointGenerator.setDesiredNumberOfBeliefPoints(10);
    assertThrows(IllegalStateException.class, () -> {
      beliefPointGenerator.generateBeliefPoints();
    });
  }

  @Test
  void generateBeliefPoints_ShouldThrowIfDesiredNumberOfBeliefPointsNotSetForAgent() {
    beliefPointGenerator.setDecPOMDP(decPOMDP);
    beliefPointGenerator.setPolicies();
    assertThrows(IllegalStateException.class, () -> {
      beliefPointGenerator.generateBeliefPoints();
    });
  }

  @Test
  void generateBeliefPoints_ShouldThrowIfRandomPoliciesNotGeneratedForAgent() {
    beliefPointGenerator.setDecPOMDP(decPOMDP);
    beliefPointGenerator.setDesiredNumberOfBeliefPoints(10);
    assertThrows(IllegalStateException.class, () -> {
      beliefPointGenerator.generateBeliefPoints();
    });
  }

  @Test
  void generateBeliefPoints_ShouldNotThrowIfAllDependenciesPresentForAgent() {
    beliefPointGenerator.setDecPOMDP(decPOMDP);
    beliefPointGenerator.setDesiredNumberOfBeliefPoints(10);
    beliefPointGenerator.setPolicies(null);
    assertDoesNotThrow(() -> {
      beliefPointGenerator.generateBeliefPoints();
    });
  }
}