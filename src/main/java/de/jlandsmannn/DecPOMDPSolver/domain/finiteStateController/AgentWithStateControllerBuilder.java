package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;

import java.util.List;

public class AgentWithStateControllerBuilder extends AgentBuilder<AgentWithStateController, AgentWithStateControllerBuilder> {

  protected FiniteStateController stateController;

  public AgentWithStateControllerBuilder setStateController(FiniteStateController stateController) {
    this.stateController = stateController;
    return this;
  }

  @Override
  public AgentWithStateController createAgent() {
    if (stateController == null) {
      stateController = FiniteStateControllerBuilder.createArbitraryController(name, List.of(), actions, observations);
    }
    return new AgentWithStateController(name, actions, observations, stateController);
  }
}
