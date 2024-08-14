package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;

public class IsomorphicDecPOMDPWithStateControllerBuilder extends DecPOMDPBuilder<IsomorphicDecPOMDPWithStateController, IsomorphicAgentWithStateController, IsomorphicDecPOMDPWithStateControllerBuilder> {

  @Override
  public IsomorphicAgentBuilder<?> getAgentBuilder() {
    return new IsomorphicAgentBuilder<>();
  }

  @Override
  public IsomorphicDecPOMDPWithStateController createDecPOMDP() {
    return new IsomorphicDecPOMDPWithStateController(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  public RepresentativeObservationsDecPOMDPWithStateController createRepresentativeObservationsDecPOMDP() {
    return new RepresentativeObservationsDecPOMDPWithStateController(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }
}
