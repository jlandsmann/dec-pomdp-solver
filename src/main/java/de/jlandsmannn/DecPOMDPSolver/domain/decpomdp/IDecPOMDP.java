package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public interface IDecPOMDP<AGENT extends IAgent, ACTION, OBSERVATION> {
  
  int getAgentCount();
  List<AGENT> getAgents();
  List<State> getStates();
  double getDiscountFactor();
  Distribution<State> getInitialBeliefState();

  double getTransitionProbability(Distribution<State> currentBeliefState, Vector<ACTION> agentActions, State followState);
  double getTransitionProbability(State currentState, Vector<ACTION> agentActions, State followState);
  double getReward(Distribution<State> currentBeliefState, Vector<ACTION> agentActions);
  double getObservationProbability(Vector<ACTION> agentActions, State followState, Vector<OBSERVATION> agentObservations);

  double getValue();
  double getValue(Distribution<State> beliefSate);

  List<Vector<ACTION>> getActionCombinations();
  List<Vector<OBSERVATION>> getObservationCombinations();
}
