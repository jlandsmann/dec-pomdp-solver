package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

import java.util.List;

public class TestAgent extends Agent {

  protected TestAgent(String name, List<Action> actions, List<Observation> observations) {
    super(name, actions, observations);
  }
}
