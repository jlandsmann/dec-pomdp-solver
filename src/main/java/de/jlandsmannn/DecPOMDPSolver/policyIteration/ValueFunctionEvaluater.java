package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.EquationSystemSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;

/**
 * This abstract class describes an interface for evaluating the value function of a DecPOMDP.
 * It creates an equation system by using a {@link ValueFunctionTransformer}
 * and solves it with a suitable {@link EquationSystemSolver}.
 *
 * @param <DECPOMDP> the data type of the DecPOMDP
 * @param <MATRIX>   the data type of the matrices and vectors
 */
public class ValueFunctionEvaluater<DECPOMDP extends IDecPOMDP<?>, MATRIX> {

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
      .orElseThrow(() -> new IllegalStateException("Could not solve equation system to evaluate value function"));
    transformer.applyValuesToDecPOMDP(result);
  }
}
