package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Histogram;

public interface ILiftedDecPOMDP<AGENT extends ILiftedAgent, ACTION, OBSERVATION> extends IDecPOMDP<AGENT, ACTION, OBSERVATION> {

  public int getTotalAgentCount();

}
