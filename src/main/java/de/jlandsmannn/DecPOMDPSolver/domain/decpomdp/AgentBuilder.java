package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class AgentBuilder {
  private String name;
  private List<Action> actions;
  private List<Observation> observations;

  public AgentBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public AgentBuilder setActions(Collection<Action> actions) {
    this.actions = List.copyOf(Set.copyOf(actions));
    return this;
  }

  public AgentBuilder setObservations(Collection<Observation> observations) {
    this.observations = List.copyOf(Set.copyOf(observations));
    return this;
  }

  public AgentWithStateController createAgent() {
    var controller = FiniteStateControllerBuilder.createArbitraryController(name, actions, observations);
    return new AgentWithStateController(name, actions, observations, controller);
  }
}