package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public abstract class CombinatorialNodePruner<LP, RESULT> {
  private static final Logger LOG = LoggerFactory.getLogger(CombinatorialNodePruner.class);

  protected CombinatorialNodePruningTransformer<LP, RESULT> transformer;
  protected LinearOptimizationSolver<LP, RESULT> solver;
  protected AgentWithStateController agent;

  public CombinatorialNodePruner(CombinatorialNodePruningTransformer<LP, RESULT> transformer,
                                 LinearOptimizationSolver<LP, RESULT> solver) {
    this.transformer = transformer;
    this.solver = solver;
  }

  public CombinatorialNodePruner<LP, RESULT> setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    transformer.setDecPOMDP(decPOMDP);
    return this;
  }

  public CombinatorialNodePruner<LP, RESULT> setAgent(AgentWithStateController agent) {
    this.agent = agent;
    transformer.setAgent(agent);
    return this;
  }

  public CombinatorialNodePruner<LP, RESULT> setBeliefPoints(Collection<Distribution<State>> beliefPoints) {
    transformer.setBeliefPoints(beliefPoints);
    return this;
  }

  public void pruneNodesIfCombinatorialDominated() {
    if (agent.getControllerNodes().size() <= 1) {
      LOG.info("No combinatorial pruning possible, since {} has only {} node(s).", agent, agent.getControllerNodes().size());
      return;
    }
    LOG.info("Iterating over all {} nodes of {} for combinatorial pruning", agent.getControllerNodes().size(), agent);
    for (var node : List.copyOf(agent.getControllerNodes())) {
      pruneNodeIfCombinatorialDominated(node);
    }
  }

  public void pruneNodeIfCombinatorialDominated(Node nodeToCheck) {
    LOG.debug("Checking {} of {} for pruning", nodeToCheck, agent);
    var lp = transformer.getLinearProgramForNode(nodeToCheck);
    solver.setLinearProgram(lp);
    var results = solver.maximise();
    if (results.isEmpty()) {
      LOG.debug("Linear program has no results, no pruning required.");
      return;
    }
    LOG.debug("Retrieving pruning nodes distribution for {}", nodeToCheck);
    var dominatingNodes = transformer.getDominatingNodeDistributionFromResult(results.get());
    if (dominatingNodes.isEmpty()) {
      LOG.debug("No combination of dominating nodes exist, can't prune {} of {}", nodeToCheck, agent);
      return;
    }
    LOG.debug("Replacing {} with {}", nodeToCheck, dominatingNodes.get());
    agent.pruneNode(nodeToCheck, dominatingNodes.get());
  }
}
