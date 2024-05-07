package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentTest {
  TestAgent agent;

  @BeforeEach
  void setUp() {
    agent = new TestAgent("A1", Action.setOf("A1", "A2", "A3"), Observation.setOf("O1", "O2"));
  }

  @Test
  void getName_ShouldReturnNamePutIn() {
    var expected = "A1";
    var actual = agent.getName();
    assertEquals(expected, actual);
  }

  @Test
  void getActions_ShouldReturnActionsPutIn() {
    var expected = Action.setOf("A1", "A2", "A3");
    var actual = agent.getActions();
    assertEquals(expected, actual);
  }

  @Test
  void getObservations_ShouldReturnObservationsPutIn() {
    var expected = Observation.setOf("O1", "O2");
    var actual = agent.getObservations();
    assertEquals(expected, actual);
  }
}