package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.ILiftedAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.RepresentativeDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.SparseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

/**
 * This class implements the {@link ValueFunctionTransformer}
 * and transforms a {@link DecPOMDPWithStateController}
 * into a matrix and vector, that work with the OjAlgo library.
 */
@Service
public class OJARepresentativeObservationsValueFunctionTransformer extends OJABaseValueFunctionTransformer<RepresentativeDecPOMDPWithStateController> implements ValueFunctionTransformer<RepresentativeDecPOMDPWithStateController, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJARepresentativeObservationsValueFunctionTransformer.class);

  private long groundingConstant;

  protected double getCoefficient(State state, Vector<Node> nodeVector, State newState, Vector<Node> newNodeVector) {
    var discountFactor = decPOMDP.getDiscountFactor();
    var coefficientModification = 0;
    if (state.equals(newState) && nodeVector.equals(newNodeVector)) {
      coefficientModification = -1;
    }
    return decPOMDP.getActionCombinations().stream()
      .flatMapToDouble(actionVector ->
        decPOMDP.getObservationCombinations().stream()
          .parallel()
          .mapToDouble(observationVector -> {
            var actionProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
            var stateTransitionProbability = Math.pow(decPOMDP.getTransitionProbability(state, actionVector, newState), groundingConstant);
            var observationProbability = Math.pow(decPOMDP.getObservationProbability(actionVector, newState, observationVector), groundingConstant);
            var nodeTransitionProbability = groundingConstant * decPOMDP.getNodeTransitionProbability(nodeVector, actionVector, observationVector, newNodeVector);
            return discountFactor * actionProbability * stateTransitionProbability * observationProbability * nodeTransitionProbability;
          })
      )
      .reduce(coefficientModification, Double::sum)
      ;
  }

  protected double calculateAllRewardsForStateAndNodes(State state, Vector<Node> nodeVector) {
    return decPOMDP.getActionCombinations().stream()
      .map(actionVector -> calculateRewardForStateAndNodesAndActions(state, nodeVector, actionVector))
      .reduce(Double::sum)
      .map(r -> r * groundingConstant * groundingConstant)
      .orElse(0D);
  }

  private double calculateRewardForStateAndNodesAndActions(State state, Vector<Node> nodeVector, Vector<Action> actionVector) {
    var actionProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
    var reward = decPOMDP.getReward(state, actionVector);
    return actionProbability * reward;
  }
}
