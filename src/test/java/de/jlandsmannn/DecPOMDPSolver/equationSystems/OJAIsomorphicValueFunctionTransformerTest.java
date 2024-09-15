package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.SparseStore;
import org.ojalgo.random.Uniform;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OJAIsomorphicValueFunctionTransformerTest {

  OJAIsomorphicValueFunctionTransformer transformer;
  IsomorphicDecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    decPOMDP = DecPOMDPGenerator.getIsomorphicDecPOMDP(3, 2);
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
  void getVectorFromDecPOMDP_ShouldReturnVectorWithRowForEachCombinationOfStateAndNodeVector() {
    var vector = transformer.getVectorFromDecPOMDP();
    var stateCount = decPOMDP.getStates().size();
    int nodeCombinationCount = decPOMDP.getNodeCombinations().size();

    var expectedRows = stateCount * nodeCombinationCount;
    var actualRows = vector.getRowDim();
    assertEquals(expectedRows, actualRows);
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

  @Test
  void calculateMatrixRow() {
  }

  @Test
  void indexOfStateAndNodeVector() {
    for (int idx = 0; idx < transformer.getNumberOfVariables(); idx++) {
      var state = transformer.getStateByIndex(idx);
      var vector = transformer.getNodeVectorByIndex(idx);
      var actualIndex = transformer.indexOfStateAndNodeVector(state, vector);
      assertEquals(idx, actualIndex);
    }
  }

  @Test
  void getStateByIndex_ShouldReturnCorrectState() {
    var stateCount = decPOMDP.getStates().size();
    var nodeCombinationCount = decPOMDP.getNodeCombinations().size();

    for (int i = 0; i < stateCount * nodeCombinationCount; i++) {
      var expectedStateIdx = i / nodeCombinationCount;
      var expectedState = decPOMDP.getStates().get(expectedStateIdx);
      var actualState = transformer.getStateByIndex(i);

      assertEquals(expectedState, actualState);
    }
  }

  @Test
  void getNodeVectorByIndex_ShouldReturnCorrectVector() {
    var stateCount = decPOMDP.getStates().size();
    var nodeCombinationCount = decPOMDP.getNodeCombinations().size();

    for (int i = 0; i < stateCount * nodeCombinationCount; i++) {
      var expectedVectorIdx = i % nodeCombinationCount;
      var expectedVector = decPOMDP.getNodeCombinations().get(expectedVectorIdx);
      var actualVector = transformer.getNodeVectorByIndex(i);

      assertEquals(expectedVector, actualVector);
    }
  }

  @Test
  void testCalculateMatrixRow_ShouldCallGetCoefficientForEveryPossibleStateAndNodeVector() {
    var spy = spy(transformer);
    doReturn(0D).when(spy).getCoefficient(any(), any(), any(), any());

    var matrixBuilder = SparseStore.R032.make(transformer.getNumberOfEquations(), transformer.getNumberOfVariables());
    var currentState = transformer.getStateByIndex(1);
    var currentNodeVector = transformer.getNodeVectorByIndex(1);
    var rowIndex = 1;
    spy.calculateMatrixRow(matrixBuilder, currentState, currentNodeVector, rowIndex);

    var followNodeVectors = decPOMDP.getNodeCombinations(currentNodeVector).size();
    var stateCount = decPOMDP.getStates().size();

    verify(spy, times(stateCount * followNodeVectors)).getCoefficient(eq(currentState), eq(currentNodeVector), any(), any());
  }

  @Test
  void normalizeVector_ShouldOrderPartitionNodesInVector() {
    var originalVector = Vector.of(Node.listOf("A1-Q1", "A1-Q3", "A1-Q0", "A0-Q1", "A0-Q0", "A0-Q1"));
    var expectedNormalizedVector = Vector.of(Node.listOf("A1-Q0", "A1-Q1", "A1-Q3", "A0-Q0", "A0-Q1", "A0-Q1"));
    var actualNormalizedVector = transformer.normalizeVector(originalVector);
    assertEquals(expectedNormalizedVector, actualNormalizedVector);
  }

  @Test
  void getCoefficient() {
  }

  @Test
  void calculateAllRewardsForStateAndNodes() {
  }
}