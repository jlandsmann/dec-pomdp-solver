package de.jlandsmannn.DecPOMDPSolver;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.Map;
import java.util.Set;

public class DecPOMDPGenerator {

  private static final State tigerLeft = State.from("tiger-left");
  private static final State tigerRight = State.from("tiger-right");

  public static DecPOMDPWithStateController getDecTigerPOMDP() {
    var builder = new DecPOMDPBuilder();
    initializeStates(builder);
    initializeAgents(builder);
    initializeTransitions(builder);
    initializeRewards(builder);
    initializeObservations(builder);
    initializeInitialBeliefState(builder);
    return builder.setDiscountFactor(0.8).createDecPOMDP();
  }

  public static DecPOMDPWithStateController getDecTigerPOMDPWithLargeFSC() {
    var builder = new DecPOMDPBuilder();
    initializeStates(builder);
    initializeLargeAgents(builder);
    initializeTransitions(builder);
    initializeRewards(builder);
    initializeObservations(builder);
    initializeInitialBeliefState(builder);
    return builder.setDiscountFactor(0.8).createDecPOMDP();
  }

  private static void initializeStates(DecPOMDPBuilder builder) {
    builder
      .addState("tiger-left")
      .addState("tiger-right")
    ;
  }

  private static void initializeAgents(DecPOMDPBuilder builder) {
    builder
      .addAgent(createAgent("A1"))
      .addAgent(createAgent("A2"))
    ;
  }

  private static void initializeLargeAgents(DecPOMDPBuilder builder) {
    builder
      .addAgent(createLargeAgent("A1"))
      .addAgent(createLargeAgent("A2"))
    ;

  }

  private static AgentWithStateController createAgent(String name) {
    var actions = Action.listOf("listen", "open-left", "open-right");
    var observations = Observation.listOf("hear-left", "hear-right");
    var controller = FiniteStateControllerBuilder.createArbitraryController(name, actions, observations);
    return new AgentWithStateController(name, actions, observations, controller);
  }

  private static AgentWithStateController createLargeAgent(String name) {
    var actions = Action.listOf("listen", "open-left", "open-right");
    var observations = Observation.listOf("hear-left", "hear-right");
    var controller = FiniteStateControllerBuilder.createArbitraryController(name, 4, actions, observations);
    return new AgentWithStateController(name, actions, observations, controller);
  }

  private static void initializeInitialBeliefState(DecPOMDPBuilder builder) {
    var beliefState = Distribution.createUniformDistribution(builder.getStates());
    builder.setInitialBeliefState(beliefState);
  }

  private static void initializeTransitions(DecPOMDPBuilder builder) {
    var uniformStateDistribution = Distribution.createUniformDistribution(State.setOf("tiger-left", "tiger-right"));
    builder
      .addTransition(tigerLeft, createActionVector("listen", "listen"), tigerLeft)
      .addTransition(tigerLeft, createActionVector("listen", "open-left"), uniformStateDistribution)
      .addTransition(tigerLeft, createActionVector("listen", "open-right"), uniformStateDistribution)
      .addTransition(tigerLeft, createActionVector("open-left", "listen"), uniformStateDistribution)
      .addTransition(tigerLeft, createActionVector("open-left", "open-left"), uniformStateDistribution)
      .addTransition(tigerLeft, createActionVector("open-left", "open-right"), uniformStateDistribution)
      .addTransition(tigerLeft, createActionVector("open-right", "listen"), uniformStateDistribution)
      .addTransition(tigerLeft, createActionVector("open-right", "open-left"), uniformStateDistribution)
      .addTransition(tigerLeft, createActionVector("open-right", "open-right"), uniformStateDistribution)
      .addTransition(tigerRight, createActionVector("listen", "listen"), tigerRight)
      .addTransition(tigerRight, createActionVector("listen", "open-left"), uniformStateDistribution)
      .addTransition(tigerRight, createActionVector("listen", "open-right"), uniformStateDistribution)
      .addTransition(tigerRight, createActionVector("open-left", "listen"), uniformStateDistribution)
      .addTransition(tigerRight, createActionVector("open-left", "open-left"), uniformStateDistribution)
      .addTransition(tigerRight, createActionVector("open-left", "open-right"), uniformStateDistribution)
      .addTransition(tigerRight, createActionVector("open-right", "listen"), uniformStateDistribution)
      .addTransition(tigerRight, createActionVector("open-right", "open-left"), uniformStateDistribution)
      .addTransition(tigerRight, createActionVector("open-right", "open-right"), uniformStateDistribution)
    ;
  }

  private static void initializeRewards(DecPOMDPBuilder builder) {
    builder
      .addReward(tigerLeft, createActionVector("listen", "listen"), -2D)
      .addReward(tigerLeft, createActionVector("listen", "open-left"), -101D)
      .addReward(tigerLeft, createActionVector( "listen", "open-right"), 9D)
      .addReward(tigerLeft, createActionVector("open-left", "listen"), -101D)
      .addReward(tigerLeft, createActionVector("open-left", "open-left"), -50D)
      .addReward(tigerLeft, createActionVector("open-left", "open-right"), -100D)
      .addReward(tigerLeft, createActionVector("open-right", "listen"), 9D)
      .addReward(tigerLeft, createActionVector("open-right", "open-left"), -100D)
      .addReward(tigerLeft, createActionVector("open-right", "open-right"), 20D)
      .addReward(tigerRight, createActionVector("listen", "listen"), -2D)
      .addReward(tigerRight, createActionVector("listen", "open-left"), 9D)
      .addReward(tigerRight, createActionVector( "listen", "open-right"), -101D)
      .addReward(tigerRight, createActionVector("open-left", "listen"), 9D)
      .addReward(tigerRight, createActionVector("open-left", "open-left"), 20D)
      .addReward(tigerRight, createActionVector("open-left", "open-right"), -100D)
      .addReward(tigerRight, createActionVector("open-right", "listen"), -101D)
      .addReward(tigerRight, createActionVector("open-right", "open-left"), -100D)
      .addReward(tigerRight, createActionVector("open-right", "open-right"), -50D)
    ;

  }

  private static void initializeObservations(DecPOMDPBuilder builder) {
    var uniformObservationDistribution = Distribution.createUniformDistribution(Set.of(
      Vector.of(Observation.listOf("hear-left", "hear-left")),
      Vector.of(Observation.listOf("hear-right", "hear-left")),
      Vector.of(Observation.listOf("hear-left", "hear-right")),
      Vector.of(Observation.listOf("hear-right", "hear-right"))
    ));
    builder
      .addObservation(createActionVector("listen", "listen"), tigerLeft, Distribution.of(Map.of(
        Vector.of(Observation.listOf("hear-left", "hear-left")), 0.7225,
        Vector.of(Observation.listOf("hear-right", "hear-left")), 0.1275,
        Vector.of(Observation.listOf("hear-left", "hear-right")), 0.1275,
        Vector.of(Observation.listOf("hear-right", "hear-right")), 0.0225
      )))
      .addObservation(createActionVector("listen", "open-left"), tigerLeft, uniformObservationDistribution)
      .addObservation(createActionVector("listen", "open-right"), tigerLeft, uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "listen"), tigerLeft, uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "open-left"), tigerLeft, uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "open-right"), tigerLeft, uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "listen"), tigerLeft, uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "open-left"), tigerLeft, uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "open-right"), tigerLeft, uniformObservationDistribution)

      .addObservation(createActionVector("listen", "listen"), tigerRight, Distribution.of(Map.of(
        Vector.of(Observation.listOf("hear-right", "hear-right")), 0.7225,
        Vector.of(Observation.listOf("hear-right", "hear-left")), 0.1275,
        Vector.of(Observation.listOf("hear-left", "hear-right")), 0.1275,
        Vector.of(Observation.listOf("hear-left", "hear-left")), 0.0225
      )))
      .addObservation(createActionVector("listen", "open-left"), tigerRight, uniformObservationDistribution)
      .addObservation(createActionVector("listen", "open-right"), tigerRight, uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "listen"), tigerRight, uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "open-left"), tigerRight, uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "open-right"), tigerRight, uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "listen"), tigerRight, uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "open-left"), tigerRight, uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "open-right"), tigerRight, uniformObservationDistribution)
    ;
  }

  private static Vector<Action> createActionVector(String... actions) {
    var listOfActions = Action.listOf(actions);
    return new Vector<>(listOfActions);
  }
}
