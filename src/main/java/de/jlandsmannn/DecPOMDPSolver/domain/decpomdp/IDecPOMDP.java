package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;

public interface IDecPOMDP<AGENT extends IAgent> {
  
  int getAgentCount();
  List<AGENT> getAgents();
  List<State> getStates();
  double getDiscountFactor();
  Distribution<State> getInitialBeliefState();

  double getTransitionProbability(Distribution<State> currentBeliefState, Vector<Action> agentActions, State followState);
  double getTransitionProbability(State currentState, Vector<Action> agentActions, State followState);
  double getReward(Distribution<State> currentBeliefState, Vector<Action> agentActions);
  double getReward(State currentState, Vector<Action> agentActions);
  double getObservationProbability(Vector<Action> agentActions, State followState, Vector<Observation> agentObservations);

  double getValue();
  double getValue(Distribution<State> beliefSate);

  List<Vector<Action>> getActionCombinations();
  List<Vector<Observation>> getObservationCombinations();
}
