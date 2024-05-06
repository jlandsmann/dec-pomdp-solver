package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.exceptions.SolvingFailedException;

public abstract class ValueFunctionEvaluater<DECPOMDP extends DecPOMDP<?>, MATRIX> {

  private final ValueFunctionTransformer<DECPOMDP, MATRIX> transformer;
  private final EquationSystemSolver<MATRIX> solver;

  public ValueFunctionEvaluater(
    ValueFunctionTransformer<DECPOMDP, MATRIX> transformer,
    EquationSystemSolver<MATRIX> solver
  ) {
    this.transformer = transformer;
    this.solver = solver;
  }

  public void evaluateValueFunction(DECPOMDP decPOMDP) {
    transformer.setDecPOMDP(decPOMDP);
    var numberOfEquations = transformer.getNumberOfEquations();
    var numberOfVariables = transformer.getNumberOfVariables();
    var matrix = transformer.getMatrixFromDecPOMDP();
    var vector = transformer.getVectorFromDecPOMDP();
    var result = solver
      .setDimensions(numberOfEquations, numberOfVariables)
      .setMatrix(matrix)
      .setVector(vector)
      .solve()
      .orElseThrow(SolvingFailedException::new)
    ;
    transformer.applyValuesToDecPOMDP(result);
  }
}
