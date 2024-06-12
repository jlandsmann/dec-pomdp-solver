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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * This abstract class describes an interface for pruning nodes
 * and replacing them with a dominating combination of initial nodes.
 * This means that it finds a convex combination of nodes from the same agent.
 * SO that combination has for all possible combinations of the other agent's nodes,
 * and for all belief points a value at least as high as the given node.
 * This class is independent of the concrete implementation or data types.
 *
 * @param <LP> the data type of the linear program
 * @param <RESULT> the data type of the linear program's result
 */
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
    if (agent.getControllerNodes().size() <= 2) {
      LOG.info("No combinatorial pruning possible, since {} has only {} node(s).", agent, agent.getControllerNodes().size());
      return;
    }
    var originalNodeCount = agent.getControllerNodes().size();
    var nodesToPrune = new ArrayList<>(agent.getControllerNodes());
    nodesToPrune.removeAll(agent.getInitialControllerNodes());
    LOG.info("Iterating over all {} non-initial nodes of {} for combinatorial pruning", nodesToPrune.size(), agent);
    for (var node : nodesToPrune) {
      pruneNodeIfCombinatorialDominated(node);
    }
    var newNodeCount = agent.getControllerNodes().size();
    var nodesPruned = originalNodeCount - newNodeCount;
    LOG.info("Pruned {} nodes of {}", nodesPruned, agent);
  }

  public void pruneNodeIfCombinatorialDominated(Node nodeToCheck) {
    LOG.debug("Checking {} of {} for pruning", nodeToCheck, agent);
    applyLinearProgram(nodeToCheck);
    var dominatingNodes = getDominatingNodeDistribution(nodeToCheck);
    if (dominatingNodes.isEmpty()) {
      LOG.debug("No combination of dominating nodes exist, can't prune {} of {}", nodeToCheck, agent);
      return;
    }
    LOG.debug("Replacing {} with {}", nodeToCheck, dominatingNodes.get());
    agent.pruneNode(nodeToCheck, dominatingNodes.get());
  }

  private void applyLinearProgram(Node nodeToCheck) {
    var lp = transformer.getLinearProgramForNode(nodeToCheck);
    solver.setLinearProgram(lp);
  }

  private Optional<Distribution<Node>> getDominatingNodeDistribution(Node nodeToCheck) {
    return solver.maximise()
      .flatMap(result -> transformer.getDominatingNodeDistributionFromResult(result));
  }
}
