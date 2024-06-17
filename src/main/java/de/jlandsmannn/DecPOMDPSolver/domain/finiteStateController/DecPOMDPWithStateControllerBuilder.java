package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.AgentBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;

public class DecPOMDPWithStateControllerBuilder extends DecPOMDPBuilder<DecPOMDPWithStateController, AgentWithStateController, DecPOMDPWithStateControllerBuilder> {

  @Override
  public AgentBuilder<AgentWithStateController, ?> getAgentBuilder() {
    return new AgentWithStateControllerBuilder();
  }

  @Override
  public DecPOMDPWithStateController createDecPOMDP() {
    return new DecPOMDPWithStateController(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }
}
