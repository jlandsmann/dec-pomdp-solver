package de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public abstract class CombinatorialNodePruner<LP, RESULT> {
  private static final Logger LOG = LoggerFactory.getLogger(CombinatorialNodePruner.class);

  CombinatorialNodePruningTransformer<LP, RESULT> transformer;
  LinearOptimizationSolver<LP, RESULT> solver;

  public CombinatorialNodePruner(CombinatorialNodePruningTransformer<LP, RESULT> transformer,
                                 LinearOptimizationSolver<LP, RESULT> solver) {
    this.transformer = transformer;
    this.solver = solver;
  }

  public void pruneNodesIfCombinatorialDominated(DecPOMDPWithStateController decPOMDP,
                                     AgentWithStateController agent,
                                     Collection<Distribution<State>> beliefPoints) {
    LOG.info("Iterating over all {} nodes of {} for combinatorial pruning", agent.getControllerNodes().size(), agent);
    for (var node : agent.getControllerNodes()) {
      pruneNodeIfCombinatorialDominated(decPOMDP, agent, beliefPoints, node);
    }
  }

  public void pruneNodeIfCombinatorialDominated(DecPOMDPWithStateController decPOMDP,
                                    AgentWithStateController agent,
                                    Collection<Distribution<State>> beliefPoints,
                                    Node nodeToCheck) {
    LOG.debug("Checking {} of {} for pruning", nodeToCheck, agent);
    transformer.setDecPOMDP(decPOMDP);
    transformer.setAgent(agent);
    transformer.setBeliefPoints(beliefPoints);
    var lp = transformer.forNode(nodeToCheck);
    solver.setLinearProgram(lp);
    var results = solver.maximise();
    if (results.isEmpty()) {
      LOG.debug("Linear program has no results, no pruning required.");
      return;
    }
    LOG.debug("Retrieving pruning nodes distribution for {}", nodeToCheck);
    var dominatingNodes = transformer.getDominatingNodeDistribution(results.get());
    if (dominatingNodes.isEmpty()) {
      LOG.debug("No combination of dominating nodes exist, can't prune {} of {}", nodeToCheck, agent);
      return;
    }
    LOG.debug("Replacing {} with {} nodes of {}", nodeToCheck, dominatingNodes.get().size(), agent);
    agent.pruneNode(nodeToCheck, dominatingNodes.get());
  }
}
