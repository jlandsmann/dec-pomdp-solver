package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

import java.util.List;

public interface IAgent {
  /**
   * Getter for the name of the agent
   * @return the name of the agent
   */
  String getName();

  /**
   * Getter for the actions of the agent
   * @return a list of actions
   */
  List<Action> getActions();

  /**
   * Getter for the observations of the agent
   * @return a list of observations
   */
  List<Observation> getObservations();
}
