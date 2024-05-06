package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.equationSystems.OJAEquationSystemSolver;
import de.jlandsmannn.DecPOMDPSolver.equationSystems.OJAValueFunctionEvaluater;
import de.jlandsmannn.DecPOMDPSolver.equationSystems.OJAValueFunctionTransformer;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValueFunctionEvaluaterTest {

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
    when(solver.setDimensions(anyLong(), anyLong())).thenReturn(solver);
    when(solver.setMatrix(any())).thenReturn(solver);
    when(solver.setVector(any())).thenReturn(solver);
    when(solver.solve()).thenReturn(Optional.of(resultVector));

    when(valueFunctionTransformer.getNumberOfEquations()).thenReturn(numberOfEquations);
    when(valueFunctionTransformer.getNumberOfVariables()).thenReturn(numberOfVariables);
    when(valueFunctionTransformer.getMatrixFromDecPOMDP()).thenReturn(coefficientMatrix);
    when(valueFunctionTransformer.getVectorFromDecPOMDP()).thenReturn(rightHandVector);
  }

  @Test
  void evaluateValueFunction_ShouldCallTransformerSetDecPOMDP() {
    valueFunctionEvaluater.evaluateValueFunction(decPOMDP);
    Mockito.verify(valueFunctionTransformer).setDecPOMDP(decPOMDP);
  }

  @Test
  void evaluateValueFunction_ShouldCallTransformerGetMatrix() {
    valueFunctionEvaluater.evaluateValueFunction(decPOMDP);
    Mockito.verify(valueFunctionTransformer).getMatrixFromDecPOMDP();
  }

  @Test
  void evaluateValueFunction_ShouldCallTransformerGetVector() {
    valueFunctionEvaluater.evaluateValueFunction(decPOMDP);
    Mockito.verify(valueFunctionTransformer).getVectorFromDecPOMDP();
  }

  @Test
  void evaluateValueFunction_ShouldCallEvaluatorSetMatrix() {
    valueFunctionEvaluater.evaluateValueFunction(decPOMDP);
    Mockito.verify(solver).setMatrix(coefficientMatrix);
  }

  @Test
  void evaluateValueFunction_ShouldCallEvaluatorSetVector() {
    valueFunctionEvaluater.evaluateValueFunction(decPOMDP);
    Mockito.verify(solver).setVector(rightHandVector);
  }

  @Test
  void evaluateValueFunction_ShouldCallEvaluatorSolve() {
    valueFunctionEvaluater.evaluateValueFunction(decPOMDP);
    Mockito.verify(solver).solve();
  }

  @Test
  void evaluateValueFunction_ShouldCallTransformApplyValues() {
    valueFunctionEvaluater.evaluateValueFunction(decPOMDP);
    Mockito.verify(valueFunctionTransformer).applyValuesToDecPOMDP(resultVector);
  }
}