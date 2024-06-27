package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

import java.util.List;
import java.util.Objects;

public class LiftedAgent extends Agent {

  protected final int numberOfAgents;

  protected LiftedAgent(String name, List<Action> actions, List<Observation> observations, int numberOfAgents) {
    super(name, actions, observations);
    this.numberOfAgents = numberOfAgents;
  }

  public int getNumberOfAgents() {
    return numberOfAgents;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LiftedAgent agent)) return false;
    return super.equals(agent) &&
      Objects.equals(getNumberOfAgents(), agent.getNumberOfAgents());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getNumberOfAgents());
  }
}
