package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.BasicDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class IsomorphicDecPOMDP<AGENT extends IsomorphicAgent> extends BasicDecPOMDP<AGENT> {

  protected IsomorphicDecPOMDP(List<AGENT> agents, List<State> states, double discountFactor, Distribution<State> initialBeliefState,
                               Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                               Map<State, Map<Vector<Action>, Double>> rewardFunction,
                               Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(agents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  @Override
  public int getAgentCount() {
    return agents.stream().mapToInt(IsomorphicAgent::getNumberOfAgents).sum();
  }

  @Override
  public Distribution<State> getTransition(State currentState, Vector<Action> agentActions) {
    var expandedActionVector = expandVectorForLiftedAgents(agentActions);
    return super.getTransition(currentState, expandedActionVector);
  }

  @Override
  public Distribution<Vector<Observation>> getObservations(Vector<Action> agentActions, Distribution<State> nextBeliefState) {
    var expandedActionVector = expandVectorForLiftedAgents(agentActions);
    return super.getObservations(expandedActionVector, nextBeliefState);
  }

  @Override
  public double getReward(State currentState, Vector<Action> agentActions) {
    var expandedActionVector = expandVectorForLiftedAgents(agentActions);
    return super.getReward(currentState, expandedActionVector);
  }

  protected <U> Vector<U> expandVectorForLiftedAgents(Vector<U> inputVector) {
    var output = new ArrayList<U>();
    for (int i = 0; i < agents.size(); i++) {
      var agent = agents.get(i);
      var agentCount = agent.getNumberOfAgents();
      var element = inputVector.get(i);
      for (int j = 0; j < agentCount; j++) {
        output.add(element);
      }
    }
    return Vector.of(output);
  }
}
