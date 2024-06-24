package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;
import java.util.Objects;

public class IsomorphicAgent extends Agent {

  protected final int numberOfAgents;

  protected IsomorphicAgent(String name, List<Action> actions, List<Observation> observations, int numberOfAgents) {
    super(name, actions, observations);
    this.numberOfAgents = numberOfAgents;
  }

  public int getNumberOfAgents() {
    return numberOfAgents;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IsomorphicAgent agent)) return false;
    return super.equals(agent) &&
      Objects.equals(getNumberOfAgents(), agent.getNumberOfAgents());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getNumberOfAgents());
  }
}
