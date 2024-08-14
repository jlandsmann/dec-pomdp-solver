package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;

public interface ILiftedAgent extends IAgent {
  int getPartitionSize();
}
