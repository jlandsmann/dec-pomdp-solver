package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;

/**
 * This interface describes a library and data type independent class
 * which creates a linear equation system from the given DecPOMDP.
 * Which, then can be solved by an {@link EquationSystemSolver}.
 *
 * @param <DECPOMDP> the concrete class of the {@link DecPOMDP}
 * @param <MATRIX>   the data type of matrices and vectors
 */
public interface ValueFunctionTransformer<DECPOMDP extends IDecPOMDP<?>, MATRIX> {

  void setDecPOMDP(DECPOMDP decPOMDP);

  long getNumberOfEquations();

  long getNumberOfVariables();

  MATRIX getMatrixFromDecPOMDP();

  MATRIX getVectorFromDecPOMDP();

  void applyValuesToDecPOMDP(MATRIX values);
}
