package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;

import java.util.Collection;
import java.util.Set;

public class AgentBuilder {
  private String name;
  private Set<Action> actions;
  private Set<Observation> observations;

  public AgentBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public AgentBuilder setActions(Collection<Action> actions) {
    this.actions = Set.copyOf(actions);
    return this;
  }

  public AgentBuilder setObservations(Collection<Observation> observations) {
    this.observations = Set.copyOf(observations);
    return this;
  }

  public AgentWithStateController createAgent() {
    var controller = FiniteStateControllerBuilder.createArbitraryController(name, actions, observations);
    return new AgentWithStateController(name, actions, observations, controller);
  }
}