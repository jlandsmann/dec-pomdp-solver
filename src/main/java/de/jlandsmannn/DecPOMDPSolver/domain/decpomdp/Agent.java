package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

import java.util.List;
import java.util.Objects;

public class Agent {
  protected final String name;
  protected final List<Action> actions;
  protected final List<Observation> observations;

  protected Agent(String name, List<Action> actions, List<Observation> observations) {
    this.name = name;
    this.actions = actions;
    this.observations = observations;
  }

  public String getName() {
    return name;
  }

  public List<Action> getActions() {
    return actions;
  }

  public List<Observation> getObservations() {
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
