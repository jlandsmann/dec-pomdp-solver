package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OJAValueFunctionEvaluaterTest {

  private final long numberOfEquations = 3;
  private final long numberOfVariables = 3;

  @Mock
  private DecPOMDPWithStateController decPOMDP;

  @Mock
  private OJAValueFunctionTransformer valueFunctionTransformer;

  @Mock
  private OJAEquationSystemSolver solver;

  @InjectMocks
  private OJAValueFunctionEvaluater valueFunctionEvaluater;

  private MatrixStore<Double> coefficientMatrix = Primitive64Store.FACTORY.make(numberOfEquations, numberOfVariables);
  private MatrixStore<Double> rightHandVector = Primitive64Store.FACTORY.make(numberOfEquations, 1);
  private MatrixStore<Double> resultVector = Primitive64Store.FACTORY.make(numberOfVariables, 1);

  @BeforeEach
  void setUp() {
    lenient().when(solver.setDimensions(anyLong(), anyLong())).thenReturn(solver);
    lenient().when(solver.setMatrix(any())).thenReturn(solver);
    lenient().when(solver.setVector(any())).thenReturn(solver);
    lenient().when(solver.solve()).thenReturn(Optional.of(resultVector));

    lenient().when(valueFunctionTransformer.getNumberOfEquations()).thenReturn(numberOfEquations);
    lenient().when(valueFunctionTransformer.getNumberOfVariables()).thenReturn(numberOfVariables);
    lenient().when(valueFunctionTransformer.getMatrixFromDecPOMDP()).thenReturn(coefficientMatrix);
    lenient().when(valueFunctionTransformer.getVectorFromDecPOMDP()).thenReturn(rightHandVector);
  }

  @Test
  void setDecPOMDP_ShouldCallTransformerSetDecPOMDP() {
    valueFunctionEvaluater.setDecPOMDP(decPOMDP);
    Mockito.verify(valueFunctionTransformer).setDecPOMDP(decPOMDP);
  }

  @Test
  void evaluateValueFunction_ShouldThrowIfDecPOMDPNotSet() {
    assertThrows(IllegalStateException.class, () ->
      valueFunctionEvaluater.evaluateValueFunction()
    );
  }

  @Test
  void evaluateValueFunction_ShouldCallTransformerGetMatrix() {
    valueFunctionEvaluater.setDecPOMDP(decPOMDP).evaluateValueFunction();
    Mockito.verify(valueFunctionTransformer).getMatrixFromDecPOMDP();
  }

  @Test
  void evaluateValueFunction_ShouldCallTransformerGetVector() {
    valueFunctionEvaluater.setDecPOMDP(decPOMDP).evaluateValueFunction();
    Mockito.verify(valueFunctionTransformer).getVectorFromDecPOMDP();
  }

  @Test
  void evaluateValueFunction_ShouldCallEvaluatorSetMatrix() {
    valueFunctionEvaluater.setDecPOMDP(decPOMDP).evaluateValueFunction();
    Mockito.verify(solver).setMatrix(coefficientMatrix);
  }

  @Test
  void evaluateValueFunction_ShouldCallEvaluatorSetVector() {
    valueFunctionEvaluater.setDecPOMDP(decPOMDP).evaluateValueFunction();
    Mockito.verify(solver).setVector(rightHandVector);
  }

  @Test
  void evaluateValueFunction_ShouldCallEvaluatorSolve() {
    valueFunctionEvaluater.setDecPOMDP(decPOMDP).evaluateValueFunction();
    Mockito.verify(solver).solve();
  }

  @Test
  void evaluateValueFunction_ShouldCallTransformApplyValues() {
    valueFunctionEvaluater.setDecPOMDP(decPOMDP).evaluateValueFunction();
    Mockito.verify(valueFunctionTransformer).applyValuesToDecPOMDP(resultVector);
  }

}