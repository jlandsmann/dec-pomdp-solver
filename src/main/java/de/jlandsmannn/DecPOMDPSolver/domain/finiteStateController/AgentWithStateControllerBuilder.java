package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

public class AgentWithStateControllerBuilder extends AgentBuilder<AgentWithStateController, AgentWithStateControllerBuilder> {

  protected FiniteStateController stateController;

  public AgentWithStateControllerBuilder withFirstActionInitialPolicy() {
    var policy = Distribution.createSingleEntryDistribution(actions.get(0));
    return withInitialPolicy(policy);
  }

  public AgentWithStateControllerBuilder withUniformInitialPolicy() {
    var policy = Distribution.createUniformDistribution(actions);
    return withInitialPolicy(policy);
  }

  public AgentWithStateControllerBuilder withRandomInitialPolicy() {
    var policy = Distribution.createRandomDistribution(actions);
    return withInitialPolicy(policy);
  }

  public AgentWithStateControllerBuilder withInitialPolicy(Distribution<Action> initialPolicy) {
    stateController = FiniteStateControllerBuilder.createSelfLoopController(name, actions, observations, initialPolicy);
    return this;
  }

  @Override
  public AgentWithStateController createAgent() {
    if (stateController == null) withFirstActionInitialPolicy();
    return new AgentWithStateController(name, actions, observations, stateController);
  }
}
