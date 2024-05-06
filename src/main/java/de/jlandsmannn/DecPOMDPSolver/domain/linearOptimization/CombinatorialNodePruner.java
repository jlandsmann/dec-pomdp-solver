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
    LOG.info("Iterating over all nodes of {} for pruning", agent);
    for (var node : agent.getControllerNodes()) {
      pruneNodeIfCombinatorialDominated(decPOMDP, agent, beliefPoints, node);
    }
  }

  public void tryPruneNodeIfCombinatorialDominated(DecPOMDPWithStateController decPOMDP,
                                                   AgentWithStateController agent,
                                                   Collection<Distribution<State>> beliefPoints,
                                                   Node nodeToCheck) {
    pruneNodeIfCombinatorialDominated(decPOMDP, agent, beliefPoints, nodeToCheck);
  }

  public void pruneNodeIfCombinatorialDominated(DecPOMDPWithStateController decPOMDP,
                                    AgentWithStateController agent,
                                    Collection<Distribution<State>> beliefPoints,
                                    Node nodeToCheck) {
    LOG.info("Checking {} of {} for pruning", nodeToCheck, agent);
    transformer.setDecPOMDP(decPOMDP);
    transformer.setAgent(agent);
    transformer.setBeliefPoints(beliefPoints);
    var lp = transformer.forNode(nodeToCheck);
    solver.setLinearProgram(lp);
    var results = solver.maximise();
    if (results.isEmpty()) {
      LOG.info("Linear program has no results, no pruning required.");
      return;
    }
    LOG.info("Retrieving pruning nodes distribution for {}", nodeToCheck);
    var pruningNodes = transformer.getPruningNodes(results.get());
    LOG.info("Pruning nodes of {} for {}", agent, nodeToCheck);
    agent.pruneNode(nodeToCheck, pruningNodes.get());
  }
}
