package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

/**
 * This class is used to build an agent.
 * It ensures an agent has unique actions and observations
 * and creates needed objects, like an arbitrary finite state controller.
 */
public class BasicAgentBuilder extends AgentBuilder<Agent, BasicAgentBuilder> {

  public Agent createAgent() {
    return new Agent(name, actions, observations);
  }
}