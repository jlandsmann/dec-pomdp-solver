package de.jlandsmannn.DecPOMDPSolver.cmd;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationSolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Command(command = "test")
@Component
public class TestCommand {
  private final HeuristicPolicyIterationSolver solver;

  @Autowired
  public TestCommand(HeuristicPolicyIterationSolver solver) {
    this.solver = solver;
  }

  @Command(command = "init")
  public void initDecPOMDP() {
    var model = initializeDecPOMDP();
    var initialBeliefState = Distribution.createUniformDistribution(Set.copyOf(model.getStates()));
    var initialPolicies = generateInitialPolicies(model);
    var result = solver
      .setDecPOMDP(model)
      .setInitialBeliefState(initialBeliefState)
      .setNumberOfBeliefPoints(10)
      .setMaxIterations(20)
      .setInitialPolicies(initialPolicies)
      .solve();
    System.out.println("Result: " + result);
  }

  public DecPOMDPWithStateController initializeDecPOMDP() {
    var builder = new DecPOMDPBuilder();
    initializeStates(builder);
    initializeAgents(builder);
    initializeTransitions(builder);
    initializeRewards(builder);
    initializeObservations(builder);
    return builder.setDiscountFactor(0.99).createDecPOMDP();
  }

  public Map<Agent, Map<State, Distribution<Action>>> generateInitialPolicies(DecPOMDPWithStateController model) {
    var actionDistribution = Distribution.of(Map.of(
      Action.from("listen"), 0.8,
      Action.from("open-left"), 0.1,
      Action.from("open-right"), 0.1
    ));
    var policy = Map.of(
      State.from("tiger-left"), actionDistribution,
      State.from("tiger-right"), actionDistribution
    );
    return Map.of(
      model.getAgents().get(0), policy,
      model.getAgents().get(1), policy
    );
  }

  private void initializeStates(DecPOMDPBuilder builder) {
    builder
      .addState("tiger-left")
      .addState("tiger-right")
    ;
  }

  private void initializeAgents(DecPOMDPBuilder builder) {
    builder
      .addAgent(createAgent("A1"))
      .addAgent(createAgent("A2"))
    ;
  }

  private AgentWithStateController createAgent(String name) {
    var actions = Action.setOf("listen", "open-left", "open-right");
    var observations = Observation.setOf("hear-left", "hear-right");
    var controller = FiniteStateControllerBuilder.createArbitraryController(name, actions, observations);
    return new AgentWithStateController(name, actions, observations, controller);
  }

  private void initializeTransitions(DecPOMDPBuilder builder) {
    var uniformStateDistribution = Distribution.createUniformDistribution(State.setOf("tiger-left", "tiger-right"));
    builder
      .addTransition("tiger-left", createActionVector("listen", "listen"), "tiger-left")
      .addTransition("tiger-left", createActionVector("listen", "open-left"), uniformStateDistribution)
      .addTransition("tiger-left", createActionVector("listen", "open-right"), uniformStateDistribution)
      .addTransition("tiger-left", createActionVector("open-left", "listen"), uniformStateDistribution)
      .addTransition("tiger-left", createActionVector("open-left", "open-left"), uniformStateDistribution)
      .addTransition("tiger-left", createActionVector("open-left", "open-right"), uniformStateDistribution)
      .addTransition("tiger-left", createActionVector("open-right", "listen"), uniformStateDistribution)
      .addTransition("tiger-left", createActionVector("open-right", "open-left"), uniformStateDistribution)
      .addTransition("tiger-left", createActionVector("open-right", "open-right"), uniformStateDistribution)
      .addTransition("tiger-right", createActionVector("listen", "listen"), "tiger-right")
      .addTransition("tiger-right", createActionVector("listen", "open-left"), uniformStateDistribution)
      .addTransition("tiger-right", createActionVector("listen", "open-right"), uniformStateDistribution)
      .addTransition("tiger-right", createActionVector("open-left", "listen"), uniformStateDistribution)
      .addTransition("tiger-right", createActionVector("open-left", "open-left"), uniformStateDistribution)
      .addTransition("tiger-right", createActionVector("open-left", "open-right"), uniformStateDistribution)
      .addTransition("tiger-right", createActionVector("open-right", "listen"), uniformStateDistribution)
      .addTransition("tiger-right", createActionVector("open-right", "open-left"), uniformStateDistribution)
      .addTransition("tiger-right", createActionVector("open-right", "open-right"), uniformStateDistribution)
    ;
  }

  private void initializeRewards(DecPOMDPBuilder builder) {
    builder
      .addReward("tiger-left", createActionVector("listen", "listen"), -2D)
      .addReward("tiger-left", createActionVector("listen", "open-left"), -101D)
      .addReward("tiger-left", createActionVector( "listen", "open-right"), 9D)
      .addReward("tiger-left", createActionVector("open-left", "listen"), -101D)
      .addReward("tiger-left", createActionVector("open-left", "open-left"), -50D)
      .addReward("tiger-left", createActionVector("open-left", "open-right"), -100D)
      .addReward("tiger-left", createActionVector("open-right", "listen"), 9D)
      .addReward("tiger-left", createActionVector("open-right", "open-left"), -100D)
      .addReward("tiger-left", createActionVector("open-right", "open-right"), 20D)
      .addReward("tiger-right", createActionVector("listen", "listen"), -2D)
      .addReward("tiger-right", createActionVector("listen", "open-left"), 9D)
      .addReward("tiger-right", createActionVector( "listen", "open-right"), -101D)
      .addReward("tiger-right", createActionVector("open-left", "listen"), 9D)
      .addReward("tiger-right", createActionVector("open-left", "open-left"), 20D)
      .addReward("tiger-right", createActionVector("open-left", "open-right"), -100D)
      .addReward("tiger-right", createActionVector("open-right", "listen"), -101D)
      .addReward("tiger-right", createActionVector("open-right", "open-left"), -100D)
      .addReward("tiger-right", createActionVector("open-right", "open-right"), -50D)
    ;

  }

  private void initializeObservations(DecPOMDPBuilder builder) {
    var uniformObservationDistribution = Distribution.createUniformDistribution(Set.of(
      Vector.of(Observation.listOf("hear-left", "hear-left")),
      Vector.of(Observation.listOf("hear-right", "hear-left")),
      Vector.of(Observation.listOf("hear-left", "hear-right")),
      Vector.of(Observation.listOf("hear-right", "hear-right"))
    ));
    builder
      .addObservation(createActionVector("listen", "listen"), "tiger-left", Distribution.of(Map.of(
        Vector.of(Observation.listOf("hear-left", "hear-left")), 0.7225,
        Vector.of(Observation.listOf("hear-right", "hear-left")), 0.1275,
        Vector.of(Observation.listOf("hear-left", "hear-right")), 0.1275,
        Vector.of(Observation.listOf("hear-right", "hear-right")), 0.0225
      )))
      .addObservation(createActionVector("listen", "open-left"), "tiger-left", uniformObservationDistribution)
      .addObservation(createActionVector("listen", "open-right"), "tiger-left", uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "listen"), "tiger-left", uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "open-left"), "tiger-left", uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "open-right"), "tiger-left", uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "listen"), "tiger-left", uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "open-left"), "tiger-left", uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "open-right"), "tiger-left", uniformObservationDistribution)

      .addObservation(createActionVector("listen", "listen"), "tiger-right", Distribution.of(Map.of(
        Vector.of(Observation.listOf("hear-right", "hear-right")), 0.7225,
        Vector.of(Observation.listOf("hear-right", "hear-left")), 0.1275,
        Vector.of(Observation.listOf("hear-left", "hear-right")), 0.1275,
        Vector.of(Observation.listOf("hear-left", "hear-left")), 0.0225
      )))
      .addObservation(createActionVector("listen", "open-left"), "tiger-right", uniformObservationDistribution)
      .addObservation(createActionVector("listen", "open-right"), "tiger-right", uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "listen"), "tiger-right", uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "open-left"), "tiger-right", uniformObservationDistribution)
      .addObservation(createActionVector("open-left", "open-right"), "tiger-right", uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "listen"), "tiger-right", uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "open-left"), "tiger-right", uniformObservationDistribution)
      .addObservation(createActionVector("open-right", "open-right"), "tiger-right", uniformObservationDistribution)
    ;
  }

  private Vector<Action> createActionVector(String... actions) {
    var listOfActions = Action.listOf(actions);
    return new Vector<>(listOfActions);
  }
}
