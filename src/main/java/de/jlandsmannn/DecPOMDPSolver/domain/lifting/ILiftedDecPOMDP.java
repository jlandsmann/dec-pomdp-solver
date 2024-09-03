package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;

public interface ILiftedDecPOMDP<AGENT extends ILiftedAgent> extends IDecPOMDP<AGENT> {

  int getTotalAgentCount();

}
