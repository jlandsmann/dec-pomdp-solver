package de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.Collection;
import java.util.Optional;

/**
 * This interface describes a library and data type independent class
 * which creates a linear optimization program from the given DecPOMDP.
 * This LP is used to determine a convex combination of dominating nodes
 * for a given node and a set of belief points.
 * It can be solved by an {@link LinearOptimizationSolver}.
 *
 * @param <LP> the linear program itself
 * @param <RESULT> the LP's result data type
 */
public interface CombinatorialNodePruningTransformer<LP, RESULT> {

  void setDecPOMDP(DecPOMDPWithStateController decPOMDP);
  void setAgent(AgentWithStateController agent);
  void setBeliefPoints(Collection<Distribution<State>> beliefPoints);

  LP getLinearProgramForNode(Node nodeToPrune);

  Optional<Distribution<Node>> getDominatingNodeDistributionFromResult(RESULT result);
}
