package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;

public class IsomorphicAgent extends Agent {

  protected final int numberOfAgents;

  protected IsomorphicAgent(String name, List<Action> actions, List<Observation> observations, int numberOfAgents) {
    super(name, actions, observations);
    this.numberOfAgents = numberOfAgents;
  }

}
