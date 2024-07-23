package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.function.aggregator.Aggregator;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OJAIsomorphicValueFunctionTransformerTest {

  OJAIsomorphicValueFunctionTransformer transformer;
  IsomorphicDecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    decPOMDP = generateDecPOMDP();
    transformer = new OJAIsomorphicValueFunctionTransformer();
    transformer.setDecPOMDP(decPOMDP);
  }

  @Test
  void getMatrixFromDecPOMDP_ShouldReturnMatrixOfTransitionCoefficients() {
    var matrix = transformer.getMatrixFromDecPOMDP();
    var stateCount = decPOMDP.getStates().size();
    int nodeCombinationCount = decPOMDP.getNodeCombinations().size();

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
    int nodeCombinationCount = decPOMDP.getNodeCombinations().size();

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
    var nodeCombinations = decPOMDP.getNodeCombinations();
    long nodeCombinationCount = nodeCombinations.size();
    var vector = Primitive64Store.FACTORY.makeFilled(stateCount * nodeCombinationCount, 1, Uniform.of(10, 20));
    transformer.applyValuesToDecPOMDP(vector);

    AtomicLong index = new AtomicLong();
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : nodeCombinations) {
        var expectedValue = vector.get(index.getAndAdd(1), 0);
        var actualValue = decPOMDP.getValue(state, nodeVector);
        assertEquals(expectedValue, actualValue);
      }
    }
  }

  private IsomorphicDecPOMDPWithStateController generateDecPOMDP() {
    return DecPOMDPGenerator.getIsomorphicDecPOMDP();
  }
}