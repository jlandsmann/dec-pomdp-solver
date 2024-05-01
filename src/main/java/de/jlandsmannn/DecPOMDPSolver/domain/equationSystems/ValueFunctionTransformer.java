package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;

public interface ValueFunctionTransformer<DECPOMDP extends DecPOMDP<? extends Agent>, MATRIX> {

  MATRIX getMatrixFromDecPOMDP(DECPOMDP decPOMDP);

  MATRIX getVectorFromDecPOMDP(DECPOMDP decPOMDP);

  void applyValuesToDecPOMDP(DECPOMDP decPOMDP, MATRIX values);
}
