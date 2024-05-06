package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;

public interface ValueFunctionTransformer<DECPOMDP extends DecPOMDP<? extends Agent>, MATRIX> {

  void setDecPOMDP(DECPOMDP decPOMDP);

  long getNumberOfEquations();

  long getNumberOfVariables();

  MATRIX getMatrixFromDecPOMDP();

  MATRIX getVectorFromDecPOMDP();

  void applyValuesToDecPOMDP(MATRIX values);
}
