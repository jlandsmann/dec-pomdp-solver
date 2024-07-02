package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IAgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This is an extension of the base agent,
 * which uses {@link FiniteStateController} to represent its own policy.
 * This agent is used by {@link DecPOMDPWithStateController}.
 */
public class LiftedAgentWithStateController extends AgentWithStateController implements IAgentWithStateController, ILiftedAgent {

  private final int numberOfAgents;

  /**
   * Default constructor with name, actions, observations and controller.
   *
   * @param name         The name of this agent
   * @param actions      The actions of this agent
   * @param observations The observations of this agent
   * @param numberOfAgents The number of agents of this agent
   * @param controller   The controller of this agent
   */
  public LiftedAgentWithStateController(String name, List<Action> actions, List<Observation> observations, int numberOfAgents, FiniteStateController controller) {
    super(name, actions, observations, controller);
    this.numberOfAgents = numberOfAgents;
  }

  @Override
  public int getNumberOfAgents() {
    return numberOfAgents;
  }
}
