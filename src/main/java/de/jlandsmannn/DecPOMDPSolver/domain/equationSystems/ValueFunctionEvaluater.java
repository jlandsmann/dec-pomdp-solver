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
    solver.setMatrix(transformer.getMatrixFromDecPOMDP());
    solver.setVector(transformer.getVectorFromDecPOMDP());
    var result = solver.solve().orElseThrow(SolvingFailedException::new);
    transformer.applyValuesToDecPOMDP(result);
  }
}
