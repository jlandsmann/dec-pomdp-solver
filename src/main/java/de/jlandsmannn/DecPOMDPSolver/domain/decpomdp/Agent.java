package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

import java.util.List;
import java.util.Objects;

/**
 * This class represents the agent of a markov decision process and its more general cases.
 * This base version just handles the abilities of an agent,
 * but has apart from that no further business logic.
 */
public class Agent implements IAgent {
  protected final String name;
  protected final List<Action> actions;
  protected final List<Observation> observations;

  protected Agent(String name, List<Action> actions, List<Observation> observations) {
    this.name = name;
    this.actions = actions;
    this.observations = observations;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<Action> getActions() {
    return actions;
  }

  @Override
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
