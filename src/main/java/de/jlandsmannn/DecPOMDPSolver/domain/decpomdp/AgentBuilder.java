package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This abstract class is used to create an agent builder.
 * It ensures an agent has unique actions and observations.
 */
public abstract class AgentBuilder<AGENT extends IAgent, THIS extends AgentBuilder<AGENT, ?>> {
  protected String name;
  protected List<Action> actions;
  protected List<Observation> observations;

  public THIS setName(String name) {
    this.name = name;
    return (THIS) this;
  }

  public THIS setActions(List<Action> actions) {
    this.actions = actions.stream().distinct().toList();
    return (THIS) this;
  }

  public THIS setObservations(List<Observation> observations) {
    this.observations = observations.stream().distinct().toList();
    return (THIS) this;
  }

  /**
   * Creates the agent.
   * @return the agent
   */
  public abstract AGENT createAgent();
}