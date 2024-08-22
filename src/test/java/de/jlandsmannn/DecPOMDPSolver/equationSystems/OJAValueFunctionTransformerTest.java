package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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
    int nodeCombinationCount = decPOMDP.getAgents().stream()
      .map(AgentWithStateController::getControllerNodes)
      .map(Collection::size)
      .reduce(1, Math::multiplyExact);
    ;

    var expectedRows = stateCount * nodeCombinationCount;
    var expectedCols = stateCount * nodeCombinationCount;

    assertEquals(expectedRows, matrix.getRowDim());
    assertEquals(expectedCols, matrix.getColDim());
  }

  @Test
  void getMatrixFromDecPOMDP_ShouldReturnMatrixWithRowSumOfNegativeHalf() {
    var matrix = transformer.getMatrixFromDecPOMDP();
    var expectedSumOfRow = -1 + decPOMDP.getDiscountFactor();
    var allowedDelta = 1e-7;

    for (int i = 0; i < matrix.getRowDim(); i++) {
      var actualSumOfRow = matrix.aggregateRow(i, Aggregator.SUM);
      assertEquals(expectedSumOfRow, actualSumOfRow, allowedDelta, "Row " + i + " should have sum of " + expectedSumOfRow + " but has sum of " + actualSumOfRow);
    }
  }

  @Test
  void getVectorFromDecPOMDP_ShouldReturnVectorWithRowForEachCombinationOfStateAndNodeVector() {
    var vector = transformer.getVectorFromDecPOMDP();
    var stateCount = decPOMDP.getStates().size();
    int nodeCombinationCount = decPOMDP.getAgents().stream()
      .map(AgentWithStateController::getControllerNodes)
      .map(Collection::size)
      .reduce(1, Math::multiplyExact);
    var expectedRows = stateCount * nodeCombinationCount;
    var actualRows = vector.getRowDim();
    assertEquals(expectedRows, actualRows);
  }

  @Test
  void getVectorFromDecPOMDP_ShouldReturnVectorWithNegativeSumOfRewards() {
    var vector = transformer.getVectorFromDecPOMDP();
    // -avg(sum of rewards)
    var expectedSum = 46.11;
    var actualSum = vector.aggregateColumn(0, Aggregator.AVERAGE);
    assertEquals(expectedSum, actualSum, 2e-1);
  }

  @Test
  void applyValuesToDecPOMDP_ShouldSetValueToDecPOMDP() {
    var stateCount = decPOMDP.getStates().size();
    var nodeCombination = VectorCombinationBuilder.listOf(decPOMDP.getAgents().stream().map(AgentWithStateController::getControllerNodes).toList());
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

  @Test
  void indexOfStateAndNodeVector_ShouldReturnIndexOfStateAndNodeVector() {
    LongStream.range(0, transformer.getNumberOfVariables()).forEach(idx -> {
      var state = transformer.getStateByIndex(idx);
      var nodeVector = transformer.getNodeVectorByIndex(idx);
      var actualIndex = transformer.indexOfStateAndNodeVector(state, nodeVector);
      assertEquals(idx, actualIndex);
    });
  }

  @Test
  void getStateByIndex_ShouldReturnFirstStateForAllNodeVectorBeforeReturningNextState() {
    var nodeCombinationCount = decPOMDP.getNodeCombinations().size();
    var firstState = decPOMDP.getStates().get(0);
    var secondState = decPOMDP.getStates().get(1);

    IntStream.range(0, nodeCombinationCount).forEach(idx -> {
      var actual = transformer.getStateByIndex(idx);
      assertEquals(firstState, actual);
    });
    IntStream.range(nodeCombinationCount, 2*nodeCombinationCount).forEach(idx -> {
      var actual = transformer.getStateByIndex(idx);
      assertEquals(secondState, actual);
    });
  }

  @Test
  void getNodeVectorByIndex_ShouldIterateOverNodeCombinations() {
    var nodeCombinations = decPOMDP.getNodeCombinations();

    IntStream.range(0, 2 * nodeCombinations.size()).forEach(idx -> {
      var expected = nodeCombinations.get(idx % nodeCombinations.size());
      var actual = transformer.getNodeVectorByIndex(idx);
      assertEquals(expected, actual);
    });
  }

  private DecPOMDPWithStateController generateDecPOMDP() {
    return DecPOMDPGenerator.getDecTigerPOMDPWithLargeFSC();
  }
}