package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DecPOMDPTest {
  private TestDecPOMDP decPOMDP;
  private List<State> states;
  private List<TestAgent> agents;
  private double discountFactor;
  private Distribution<State> initialBeliefState;
  private Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
  private Map<State, java.util.Map<Vector<Action>, Double>> rewardFunction;
  private Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction;

  @BeforeEach
  void setUp() {
    var agent1 = new TestAgent("A1", Action.listOf("A1-A1", "A1-A2"), Observation.listOf("A1-O1", "A1-O2"));
    var agent2 = new TestAgent("A2", Action.listOf("A2-A1", "A2-A2"), Observation.listOf("A2-O1", "A2-O2"));
    states = State.listOf("S1", "S2");
    agents = List.of(agent1, agent2);
    discountFactor = 0.5D;
    initialBeliefState = Distribution.createUniformDistribution(states);

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
    rewardFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A1")), 4D);
    rewardFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A2")), -4D);
    rewardFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A1")), -4D);
    rewardFunction.get(State.from("S1")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A2")), 4D);
    rewardFunction.putIfAbsent(State.from("S2"), new HashMap<>());
    rewardFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A1")), -4D);
    rewardFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A1", "A2-A2")), 4D);
    rewardFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A1")), -4D);
    rewardFunction.get(State.from("S2")).putIfAbsent(Vector.of(Action.listOf("A1-A2", "A2-A2")), 4D);

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

    decPOMDP = new TestDecPOMDP(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  @Test
  void getStates_ShouldReturnListOfStates() {
    var expected = states;
    var actual = decPOMDP.getStates();
    assertEquals(expected, actual);
  }

  @Test
  void getAgents_ShouldReturnListOfAgents() {
    var expected = agents;
    var actual = decPOMDP.getAgents();
    assertEquals(expected, actual);
  }


  @Test
  void getDiscountFactor_ShouldReturnDiscountFactor() {
    var expected = 0.5D;
    var actual = decPOMDP.getDiscountFactor();
    assertEquals(expected, actual);
  }

  @Test
  void getTransitionProbability_ShouldReturnBeliefStatePutIn() {
    var currentState = State.from("S1");
    var actionVector = Vector.of(Action.listOf("A1-A1", "A2-A1"));
    var expected = 1D;
    var actual = decPOMDP.getTransitionProbability(currentState, actionVector, State.from("S2"));
    assertEquals(expected, actual);

    actionVector = Vector.of(Action.listOf("A1-A1", "A2-A2"));
    expected = 1D;
    actual = decPOMDP.getTransitionProbability(currentState, actionVector, State.from("S1"));
    assertEquals(expected, actual);
  }

  @Test
  void getTransitionProbability_ShouldReturnWeightedBeliefStateForBeliefState() {
    var beliefState = Distribution.of(Map.of(
      State.from("S1"), 0.3,
      State.from("S2"), 0.7
    ));
    var actionVector = Vector.of(Action.listOf("A1-A1", "A2-A1"));
    var expectedS1 = 0.7;
    var expectedS2 = 0.3;
    var actualS1 = decPOMDP.getTransitionProbability(beliefState, actionVector, State.from("S1"));
    var actualS2 = decPOMDP.getTransitionProbability(beliefState, actionVector, State.from("S2"));
    assertEquals(expectedS1, actualS1);
    assertEquals(expectedS2, actualS2);
  }

  @Test
  void getReward_ShouldReturnRewardPutIn() {
    var currentState = State.from("S2");
    var actionVector = Vector.of(Action.listOf("A1-A2", "A2-A1"));
    var expected = -4D;
    var actual = decPOMDP.getReward(currentState, actionVector);
    assertEquals(expected, actual);
  }

  @Test
  void getReward_ShouldReturnWeightedRewardForBeliefState() {
    var beliefState = Distribution.of(Map.of(
      State.from("S1"), 0.2,
      State.from("S2"), 0.8
    ));
    var actionVector = Vector.of(Action.listOf("A1-A1", "A2-A1"));
    var expected = 0.2 * 4D + 0.8 * -4D;
    var actual = decPOMDP.getReward(beliefState, actionVector);
    assertEquals(expected, actual);
  }

  @Test
  void getObservationProbability_ShouldReturnObservationDistributionPutIn() {
    var actionVector = Vector.of(Action.listOf("A1-A2", "A2-A2"));
    var followState = State.from("S2");
    var observationVector = Vector.of(Observation.listOf("A1-O2", "A2-O2"));
    var expected = 1D;
    var actual = decPOMDP.getObservationProbability(actionVector, followState, observationVector);
    assertEquals(expected, actual);
  }

  @Test
  void getObservationProbability_ShouldReturnWeightedObservationForBeliefState() {
    var actionVector = Vector.of(Action.listOf("A1-A2", "A2-A1"));
    var nextBeliefState = Distribution.of(Map.of(
      State.from("S1"), 0.6,
      State.from("S2"), 0.4
    ));
    var observation1 = Vector.of(Observation.listOf("A1-O1", "A2-O1"));
    var actual1 = decPOMDP.getObservationProbability(actionVector, nextBeliefState, observation1);

    var observation2 = Vector.of(Observation.listOf("A1-O2", "A2-O2"));
    var actual2 = decPOMDP.getObservationProbability(actionVector, nextBeliefState, observation2);
    assertEquals(0.6, actual1);
    assertEquals(0.4, actual2);
  }
}