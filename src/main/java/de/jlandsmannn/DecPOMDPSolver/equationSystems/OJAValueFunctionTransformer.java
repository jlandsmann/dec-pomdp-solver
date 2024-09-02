package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.ojalgo.matrix.store.MatrixStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This class implements the {@link ValueFunctionTransformer}
 * and transforms a {@link DecPOMDPWithStateController}
 * into a matrix and vector, that work with the OjAlgo library.
 */
@Service
public class OJAValueFunctionTransformer extends OJABaseValueFunctionTransformer<IDecPOMDPWithStateController<?>> implements ValueFunctionTransformer<IDecPOMDPWithStateController<?>, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJAValueFunctionTransformer.class);

  protected double getCoefficient(State state, Vector<Node> nodeVector, State newState, Vector<Node> newNodeVector) {
    var discountFactor = decPOMDP.getDiscountFactor();
    return decPOMDP.getActionCombinations(nodeVector).stream()
      .map(actionVector ->
        decPOMDP.getObservationVectors().stream()
          .parallel()
          .map(observationVector -> {
            var observationProbability = decPOMDP.getObservationProbability(actionVector, newState, observationVector);
            var nodeTransitionProbability = decPOMDP.getNodeTransitionProbability(nodeVector, actionVector, observationVector, newNodeVector);
            return observationProbability * nodeTransitionProbability;
          })
          .reduce(Double::sum)
          .map(c -> {
            var actionVectorProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
            return c * actionVectorProbability;
          })
          .map(c -> {
            var transitionProbability = decPOMDP.getTransitionProbability(state, actionVector, newState);
            return c * transitionProbability;
          })
          .orElse(0D)
      )
      .reduce(Double::sum)
      .map(c -> c * discountFactor)
      .orElse(0D)
      ;
  }

  protected double calculateAllRewardsForStateAndNodes(State state, Vector<Node> nodeVector) {
    return decPOMDP.getActionCombinations(nodeVector)
      .stream()
      .map(actionVector -> calculateRewardForStateAndNodesAndActions(state, nodeVector, actionVector))
      .reduce(Double::sum)
      .orElse(0D);
  }

  private double calculateRewardForStateAndNodesAndActions(State state, Vector<Node> nodeVector, Vector<Action> actionVector) {
    var actionProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
    var reward = decPOMDP.getReward(state, actionVector);
    return actionProbability * reward;
  }
}
