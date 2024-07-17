package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IAgentWithStateController;

import java.util.*;

/**
 * This is an extension of the base agent,
 * which uses {@link FiniteStateController} to represent its own policy.
 * This agent is used by {@link DecPOMDPWithStateController}.
 */
public class IsomorphicAgentWithStateController extends AgentWithStateController implements IAgentWithStateController, ILiftedAgent {

  private final int partitionSize;

  /**
   * Default constructor with name, actions, observations and controller.
   *
   * @param name           The name of this agent
   * @param actions        The actions of this agent
   * @param observations   The observations of this agent
   * @param controller     The controller of this agent
   * @param partitionSize The number of agents of this agent
   */
  public IsomorphicAgentWithStateController(String name,
                                            List<Action> actions,
                                            List<Observation> observations,
                                            FiniteStateController controller,
                                            int partitionSize) {
    super(name, actions, observations, controller);
    this.partitionSize = partitionSize;
  }

  @Override
  public int getPartitionSize() {
    return partitionSize;
  }
}
