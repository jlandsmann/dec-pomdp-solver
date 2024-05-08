package de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.Collection;
import java.util.Optional;

public interface CombinatorialNodePruningTransformer<LP, RESULT> {

  void setDecPOMDP(DecPOMDPWithStateController decPOMDP);
  void setAgent(AgentWithStateController agent);
  void setBeliefPoints(Collection<Distribution<State>> beliefPoints);

  LP getLinearProgramForNode(Node nodeToPrune);

  Optional<Distribution<Node>> getDominatingNodeDistributionFromResult(RESULT result);
}
