package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import com.google.common.util.concurrent.AtomicDouble;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.ILiftedAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.*;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.tuple.Tuples;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.SparseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * This class implements the {@link ValueFunctionTransformer}
 * and transforms a {@link DecPOMDPWithStateController}
 * into a matrix and vector, that work with the OjAlgo library.
 */
@Service
public class OJAIsomorphicValueFunctionTransformer extends OJABaseValueFunctionTransformer<IsomorphicDecPOMDPWithStateController> implements ValueFunctionTransformer<IsomorphicDecPOMDPWithStateController, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJAIsomorphicValueFunctionTransformer.class);

  @Override
  protected void calculateMatrixRow(SparseStore<Double> matrixBuilder, State state, Vector<Node> nodeVector, long rowIndex) {
    matrixBuilder.add(rowIndex, rowIndex, -1);
    decPOMDP.getStates().stream().parallel().forEach(newState -> {
      decPOMDP.getNodeCombinations(nodeVector).stream().parallel().forEach(newNodeVector -> {
        var coefficient = getCoefficient(state, nodeVector, newState, newNodeVector);
        var normalizedNewNodeVector = normalizeVector(newNodeVector);
        var columnIndex = indexOfStateAndNodeVector(newState, normalizedNewNodeVector);
        matrixBuilder.add(rowIndex, columnIndex, coefficient);
      });
    });
  }

  protected long indexOfStateAndNodeVector(State state, Vector<Node> nodeVector) {
    var stateIndex = decPOMDP.getStates().indexOf(state);
    var nodeVectorIndex = decPOMDP.getNodeCombinations().indexOf(nodeVector);
    return (stateIndex * nodeCombinationCount) + nodeVectorIndex;
  }

  protected <U> Vector<U> normalizeVector(Vector<U> vector) {
    return Vector.of(Histogram.from(vector).toList());
  }

  protected double getCoefficient(State state, Vector<Node> nodeVector, State newState, Vector<Node> newNodeVector) {
    var discountFactor = decPOMDP.getDiscountFactor();
    return decPOMDP.getActionCombinations(nodeVector).stream()
      .map(actionVector ->
        decPOMDP.getObservationCombinations().stream()
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
    return getActionCombinations(nodeVector).stream()
      .map(actionVector -> calculateRewardForStateAndNodesAndActions(state, nodeVector, actionVector))
      .reduce(Double::sum)
      .orElse(0D);
  }

  private double calculateRewardForStateAndNodesAndActions(State state, Vector<Node> nodeVector, Vector<Action> actionVector) {
    var actionProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
    var reward = decPOMDP.getReward(state, actionVector);
    return actionProbability * reward;
  }

  private <U> Vector<Histogram<U>> getHistogramFromVector(Vector<U> vector) {
    if (vector.size() != decPOMDP.getTotalAgentCount()) {
      throw new IllegalArgumentException("Vector size does not match number of agents");
    }
    var rawVector = new ArrayList<Histogram<U>>();
    var i = 0;
    for (int k = 0; k < decPOMDP.getAgentCount(); k++) {
      var agent = decPOMDP.getAgents().get(k);
      var elementsOfPartition = vector.toList().subList(i, i + agent.getPartitionSize());
      var histogramOfPartition = Histogram.from(elementsOfPartition);
      rawVector.add(histogramOfPartition);
      i += agent.getPartitionSize();
    }
    return Vector.of(rawVector);
  }

  private List<Vector<Action>> getActionCombinations(Vector<Node> nodes) {
    var nodeHistogramVector = getHistogramFromVector(nodes);
    return IntStream.range(0, decPOMDP.getAgentCount())
      .mapToObj(idx -> {
        var agent = decPOMDP.getAgents().get(idx);
        var nodeHistogram = nodeHistogramVector.get(idx);
        return getActionCombinationsForPartition(agent, nodeHistogram);
      })
      .collect(CombinationCollectors.toCombinationLists())
      .map(c -> c.stream().flatMap(Vector::stream).toList())
      .map(Vector::of)
      .toList();
  }

  private List<Vector<Action>> getActionCombinationsForPartition(ILiftedAgent agent, Histogram<Node> nodeHistogram) {
    return nodeHistogram.keySet().stream()
      .map(node -> {
        var count = nodeHistogram.get(node);
        return agent.getActions().stream()
          .map(action -> IntStream.range(0, count).mapToObj((i) -> action).toList())
          .toList();
      })
      .collect(CombinationCollectors.toCombinationLists())
      .map(listOfLists -> listOfLists.stream().flatMap(Collection::stream).toList())
      .map(Vector::of)
      .toList();
  }
}
