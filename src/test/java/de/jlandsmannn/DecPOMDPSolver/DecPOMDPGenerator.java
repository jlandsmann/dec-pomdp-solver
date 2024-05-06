package de.jlandsmannn.DecPOMDPSolver;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DecPOMDPGenerator {
  public static DecPOMDPWithStateController generateDecPOMDPWithTwoAgents() {
    List<State> states;
    List<AgentWithStateController> agents;
    double discountFactor;
    Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
    Map<State, java.util.Map<Vector<Action>, Double>> rewardFunction;
    Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction;

    var agent1 = createAgentWithStateController("A1");
    var agent2 = createAgentWithStateController("A2");
    states = State.listOf("S1", "S2");
    agents = List.of(agent1, agent2);
    discountFactor = 0.5D;

    transitionFunction = new HashMap<>();
    transitionFunction.putIfAbsent(State.from("S1"), new HashMap<>());
    transitionFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A1")), Distribution.createSingleEntryDistribution(State.from("S2")));
    transitionFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A2")), Distribution.createSingleEntryDistribution(State.from("S1")));
    transitionFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A1")), Distribution.createSingleEntryDistribution(State.from("S1")));
    transitionFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A2")), Distribution.createSingleEntryDistribution(State.from("S2")));
    transitionFunction.putIfAbsent(State.from("S2"), new HashMap<>());
    transitionFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A1")), Distribution.createSingleEntryDistribution(State.from("S1")));
    transitionFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A2")), Distribution.createSingleEntryDistribution(State.from("S2")));
    transitionFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A1")), Distribution.createSingleEntryDistribution(State.from("S1")));
    transitionFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A2")), Distribution.createSingleEntryDistribution(State.from("S2")));

    rewardFunction = new HashMap<>();
    rewardFunction.putIfAbsent(State.from("S1"), new HashMap<>());
    rewardFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A1")), 5D);
    rewardFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A2")), -1D);
    rewardFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A1")), -2D);
    rewardFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A2")), 4D);
    rewardFunction.putIfAbsent(State.from("S2"), new HashMap<>());
    rewardFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A1")), -3D);
    rewardFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A2")), 7D);
    rewardFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A1")), -9D);
    rewardFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A2")), 6D);

    observationFunction = new HashMap<>();
    observationFunction.putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A1")), new HashMap<>());
    observationFunction.get(Vector.of(Action.listOf("A1-A1", "A2-A1"))).putIfAbsent(State.from("S1"), Distribution.createSingleEntryDistribution(Vector.of(Observation.listOf("A1-O1", "A2-O1"))));
    observationFunction.get(Vector.of(Action.listOf("A1-A1", "A2-A1"))).putIfAbsent(State.from("S2"), Distribution.createSingleEntryDistribution(Vector.of(Observation.listOf("A1-O2", "A2-O2"))));
    observationFunction.putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A2")), new HashMap<>());
    observationFunction.get(Vector.of(Action.listOf("A1-A1", "A2-A2"))).putIfAbsent(State.from("S1"), Distribution.createSingleEntryDistribution(Vector.of(Observation.listOf("A1-O1", "A2-O1"))));
    observationFunction.get(Vector.of(Action.listOf("A1-A1", "A2-A2"))).putIfAbsent(State.from("S2"), Distribution.createSingleEntryDistribution(Vector.of(Observation.listOf("A1-O2", "A2-O2"))));
    observationFunction.putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A1")), new HashMap<>());
    observationFunction.get(Vector.of(Action.listOf("A1-A2", "A2-A1"))).putIfAbsent(State.from("S1"), Distribution.createSingleEntryDistribution(Vector.of(Observation.listOf("A1-O1", "A2-O1"))));
    observationFunction.get(Vector.of(Action.listOf("A1-A2", "A2-A1"))).putIfAbsent(State.from("S2"), Distribution.createSingleEntryDistribution(Vector.of(Observation.listOf("A1-O2", "A2-O2"))));
    observationFunction.putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A2")), new HashMap<>());
    observationFunction.get(Vector.of(Action.listOf("A1-A2", "A2-A2"))).putIfAbsent(State.from("S1"), Distribution.createSingleEntryDistribution(Vector.of(Observation.listOf("A1-O1", "A2-O1"))));
    observationFunction.get(Vector.of(Action.listOf("A1-A2", "A2-A2"))).putIfAbsent(State.from("S2"), Distribution.createSingleEntryDistribution(Vector.of(Observation.listOf("A1-O2", "A2-O2"))));

    return new DecPOMDPWithStateController(agents, states, discountFactor, transitionFunction, rewardFunction, observationFunction);
  }

  private static AgentWithStateController createAgentWithStateController(String name) {
    var finiteStateController = new FiniteStateControllerBuilder()
      .addNode(name + "-N1")
      .addActionSelection(name + "-N1", Distribution.createSingleEntryDistribution(name + "-A1"))
      .addTransition(name + "-N1", name + "-A1", name + "-O1", Distribution.createSingleEntryDistribution(name + "-N1"))
      .addTransition(name + "-N1", name + "-A1", name + "-O2", Distribution.createSingleEntryDistribution(name + "-N2"))
      .addNode(name + "-N2")
      .addActionSelection(name + "-N2", Distribution.createSingleEntryDistribution(name + "-A2"))
      .addTransition(name + "-N2", name + "-A2", name + "-O1", Distribution.createSingleEntryDistribution(name + "-N2"))
      .addTransition(name + "-N2", name + "-A2", name + "-O2", Distribution.createSingleEntryDistribution(name + "-N1"))
      .createFiniteStateController()
    ;
    return new AgentWithStateController(
      name,
      Action.setOf(name + "-A1", name + "-A2"),
      Observation.setOf(name + "-O1", name + "-O2"),
      finiteStateController
    );
  }
}
