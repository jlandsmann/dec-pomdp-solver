package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;

import java.util.List;

public class IsomorphicAgentBuilder<THIS extends IsomorphicAgentBuilder<?>> extends AgentBuilder<IsomorphicAgentWithStateController, THIS> {

  private int numberOfAgents = 1;
  protected FiniteStateController stateController;

  public THIS setNumberOfAgents(int numberOfAgents) {
    this.numberOfAgents = numberOfAgents;
    return (THIS) this;
  }

  public THIS setStateController(FiniteStateController stateController) {
    this.stateController = stateController;
    return (THIS) this;
  }

  @Override
  public IsomorphicAgentWithStateController createAgent() {
    setControllerIfNotSet();
    return new IsomorphicAgentWithStateController(name, actions, observations, stateController, numberOfAgents);
  }

  protected void setControllerIfNotSet() {
    if (stateController == null) {
      stateController = FiniteStateControllerBuilder.createArbitraryController(name, List.of(), actions, observations);
    }
  }
}
