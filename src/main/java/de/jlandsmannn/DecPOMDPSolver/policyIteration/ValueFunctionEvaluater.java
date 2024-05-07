package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.EquationSystemSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.exceptions.SolvingFailedException;

public abstract class ValueFunctionEvaluater<DECPOMDP extends DecPOMDP<?>, MATRIX> {

  private final ValueFunctionTransformer<DECPOMDP, MATRIX> transformer;
  private final EquationSystemSolver<MATRIX> solver;

  private DECPOMDP decPOMDP;

  public ValueFunctionEvaluater(
    ValueFunctionTransformer<DECPOMDP, MATRIX> transformer,
    EquationSystemSolver<MATRIX> solver
  ) {
    this.transformer = transformer;
    this.solver = solver;
  }

  public ValueFunctionEvaluater<DECPOMDP, MATRIX> setDecPOMDP(DECPOMDP decPOMDP) {
    this.decPOMDP = decPOMDP;
    transformer.setDecPOMDP(decPOMDP);
    return this;
  }

  public void evaluateValueFunction() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to evaluate value function");
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
