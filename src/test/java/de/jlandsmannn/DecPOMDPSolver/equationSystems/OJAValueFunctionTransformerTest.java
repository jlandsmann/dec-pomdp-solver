package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OJAValueFunctionTransformerTest {

  OJAValueFunctionTransformer transformer;
  DecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    decPOMDP = generateDecPOMDP();
    transformer = new OJAValueFunctionTransformer();
    transformer.setDecPOMDP(decPOMDP);
  }

  @Test
  void getMatrixFromDecPOMDP_ShouldReturnMatrixOfTransitionCoefficients() {
    var matrix = transformer.getMatrixFromDecPOMDP();
    var stateCount = decPOMDP.getStates().size();
    var nodeCombinationCount = decPOMDP.getAgents().stream().map(a -> a.getControllerNodes().size()).reduce(1, Math::multiplyExact);

    var expectedRows = stateCount * nodeCombinationCount;
    var expectedCols = stateCount * nodeCombinationCount;

    assertEquals(expectedRows, matrix.getRowDim());
    assertEquals(expectedCols, matrix.getColDim());
  }

  @Test
  void getMatrixFromDecPOMDP_ShouldReturnMatrixWithRowSumOfNegativeHalf() {
    var matrix = transformer.getMatrixFromDecPOMDP();
    var expectedSumOfRow = -0.5D;

    for (int i = 0; i < matrix.getRowDim(); i++) {
      var actualSumOfRow = matrix.aggregateRow(i, Aggregator.SUM);
      assertEquals(expectedSumOfRow, actualSumOfRow, "Row " + i + " should have sum of " + expectedSumOfRow +" but has sum of " + actualSumOfRow);
    }
  }

  @Test
  void getVectorFromDecPOMDP_ShouldReturnVectorWithRowForEachCombinationOfStateAndNodeVector() {
    var vector = transformer.getVectorFromDecPOMDP();
    var stateCount = decPOMDP.getStates().size();
    var nodeCombinationCount = decPOMDP.getAgents().stream().map(a -> a.getControllerNodes().size()).reduce(1, Math::multiplyExact);
    var expectedRows = stateCount * nodeCombinationCount;
    var actualRows = vector.getRowDim();
    assertEquals(expectedRows, actualRows);
  }

  @Test
  void getVectorFromDecPOMDP_ShouldReturnVectorWithNegativeSumOfRewards() {
    var vector = transformer.getVectorFromDecPOMDP();
    // this depends on the sum of rewards given by the reward function
    var expectedSum = -7.0D;
    var actualSum = vector.aggregateColumn(0, Aggregator.SUM);
    assertEquals(expectedSum, actualSum);
  }

  @Test
  void applyValuesToDecPOMDP_ShouldSetValueToDecPOMDP() {
    var stateCount = decPOMDP.getStates().size();
    var nodeCombination = VectorStreamBuilder.forEachCombination(decPOMDP.getAgents().stream().map(AgentWithStateController::getControllerNodes).toList()).toList();
    long nodeCombinationCount = nodeCombination.size();
    var vector = Primitive64Store.FACTORY.makeFilled(stateCount * nodeCombinationCount, 1, Uniform.of(10, 20));
    transformer.applyValuesToDecPOMDP(vector);

    AtomicLong index = new AtomicLong();
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : nodeCombination) {
        var expectedValue = vector.get(index.getAndAdd(1), 0);
        var actualValue = decPOMDP.getValue(state, nodeVector);
        assertEquals(expectedValue, actualValue);
      }
    }
  }

  private DecPOMDPWithStateController generateDecPOMDP() {
    List<State> states;
    List<AgentWithStateController> agents;
    double discountFactor;
    Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
    Map<State, java.util.Map<Vector<Action>, Double>> rewardFunction;
    Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction;

    var fsc1 = new FiniteStateControllerBuilder()
      .addNode("A1-N1")
      .addActionSelection("A1-N1", Distribution.createSingleEntryDistribution("A1-A1"))
      .addTransition("A1-N1", "A1-A1", "A1-O1", Distribution.createSingleEntryDistribution("A1-N1"))
      .addTransition("A1-N1", "A1-A1", "A1-O2", Distribution.createSingleEntryDistribution("A1-N2"))
      .addNode("A1-N2")
      .addActionSelection("A1-N2", Distribution.createSingleEntryDistribution("A1-A2"))
      .addTransition("A1-N2", "A1-A2", "A1-O1", Distribution.createSingleEntryDistribution("A1-N2"))
      .addTransition("A1-N2", "A1-A2", "A1-O2", Distribution.createSingleEntryDistribution("A1-N1"))
      .createFiniteStateController()
    ;

    var fsc2 = new FiniteStateControllerBuilder()
      .addNode("A2-N1")
      .addActionSelection("A2-N1", Distribution.createSingleEntryDistribution("A2-A1"))
      .addTransition("A2-N1", "A2-A1", "A2-O1", Distribution.createSingleEntryDistribution("A2-N1"))
      .addTransition("A2-N1", "A2-A1", "A2-O2", Distribution.createSingleEntryDistribution("A2-N2"))
      .addNode("A2-N2")
      .addActionSelection("A2-N2", Distribution.createSingleEntryDistribution("A2-A2"))
      .addTransition("A2-N2", "A2-A2", "A2-O1", Distribution.createSingleEntryDistribution("A2-N2"))
      .addTransition("A2-N2", "A2-A2", "A2-O2", Distribution.createSingleEntryDistribution("A2-N1"))
      .createFiniteStateController()
    ;
    var agent1 = new AgentWithStateController("A1", Action.setOf("A1-A1", "A1-A2"), Observation.setOf("A1-O1", "A1-O2"), fsc1);
    var agent2 = new AgentWithStateController("A2", Action.setOf("A2-A1", "A2-A2"), Observation.setOf("A2-O1", "A2-O2"), fsc2);
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
}