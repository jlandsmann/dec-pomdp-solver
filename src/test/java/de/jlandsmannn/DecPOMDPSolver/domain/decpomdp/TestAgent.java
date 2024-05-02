package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

import java.util.Set;

class TestAgent extends Agent {

  protected TestAgent(String name, Set<Action> actions, Set<Observation> observations) {
    super(name, actions, observations);
  }
}
