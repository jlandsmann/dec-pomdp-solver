package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

import java.util.Objects;
import java.util.Set;

public abstract class Agent {
  protected final String name;
  protected final Set<Action> actions;
  protected final Set<Observation> observations;

  protected Agent(String name, Set<Action> actions, Set<Observation> observations) {
    this.name = name;
    this.actions = actions;
    this.observations = observations;
  }

  public String getName() {
    return name;
  }

  public Set<Action> getActions() {
    return actions;
  }

  public Set<Observation> getObservations() {
    return observations;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Agent agent)) return false;
    return Objects.equals(getName(), agent.getName())
      && Objects.equals(getActions(), agent.getActions())
      && Objects.equals(getObservations(), agent.getObservations());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getActions(), getObservations());
  }

  @Override
  public String toString() {
    return "Agent " + name;
  }
}
