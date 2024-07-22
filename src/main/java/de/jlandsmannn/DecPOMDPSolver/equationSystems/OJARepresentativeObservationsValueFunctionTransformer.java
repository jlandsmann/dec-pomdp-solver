package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.ILiftedAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
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
public class OJARepresentativeObservationsValueFunctionTransformer implements ValueFunctionTransformer<IsomorphicDecPOMDPWithStateController, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJARepresentativeObservationsValueFunctionTransformer.class);

  private IsomorphicDecPOMDPWithStateController decPOMDP;
  private long stateCount;
  private long nodeCombinationCount;
  private long groundingConstant;

  @Override
  public void setDecPOMDP(IsomorphicDecPOMDPWithStateController decPOMDP) {
    LOG.info("Initialized with DecPOMDP");
    this.decPOMDP = decPOMDP;
    this.stateCount = decPOMDP.getStates().size();
    this.nodeCombinationCount = decPOMDP.getNodeCombinations().size();
    this.groundingConstant = decPOMDP.getAgents().stream()
      .mapToInt(ILiftedAgent::getPartitionSize)
      .reduce(1, Math::multiplyExact);
  }

  public long getNumberOfEquations() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to get number of equations");
    return stateCount * nodeCombinationCount;
  }

  public long getNumberOfVariables() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to get number of variables");
    return stateCount * nodeCombinationCount;
  }

  @Override
  public MatrixStore<Double> getMatrixFromDecPOMDP() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to get matrix");
    LOG.info("Retrieving {}x{} transition matrix from DecPOMDP", getNumberOfEquations(), getNumberOfVariables());
    var matrixBuilder = SparseStore.R032.make(
      getNumberOfEquations(),
      getNumberOfVariables()
    );
    var rowsCalculated = new AtomicLong(0);

    LongStream.range(0, getNumberOfEquations())
      .parallel()
      .forEach(rowIndex -> {
        var state = getStateByIndex(rowIndex);
        var nodeVector = getNodeVectorByIndex(rowIndex);
        calculateMatrixRow(matrixBuilder, state, nodeVector, rowIndex);
        if (rowsCalculated.incrementAndGet() % 100 == 0) {
          LOG.info("Calculated {} / {} rows for transition matrix", rowsCalculated.get(), getNumberOfEquations());
        }
      });
    LOG.info("Calculated all {} rows for transition matrix", rowsCalculated.get());
    return matrixBuilder;
  }

  @Override
  public MatrixStore<Double> getVectorFromDecPOMDP() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to get vector");
    LOG.info("Retrieving reward vector from DecPOMDP");
    var matrixBuilder = SparseStore.R032.make(getNumberOfEquations(), 1);
    LongStream.range(0, getNumberOfEquations())
      .parallel()
      .forEach(rowIndex -> {
        var state = getStateByIndex(rowIndex);
        var nodeVector = getNodeVectorByIndex(rowIndex);
        var reward = calculateAllRewardsForStateAndNodes(state, nodeVector);
        matrixBuilder.set(rowIndex, 0, -reward);
      });
    return matrixBuilder;
  }

  @Override
  public void applyValuesToDecPOMDP(MatrixStore<Double> values) {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to apply values");
    LOG.info("Applying values to DecPOMDP");

    AtomicLong index = new AtomicLong();
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : decPOMDP.getNodeCombinations()) {
        var value = values.get(index.getAndIncrement(), 0);
        LOG.debug("Value for state {} and node vector {} is {}", state, nodeVector, value);
        decPOMDP.setValue(state, nodeVector, value);
      }
    }
  }

  private State getStateByIndex(long index) {
    var realIndex = Math.floorDiv(index, nodeCombinationCount);
    return decPOMDP.getStates().get(Math.toIntExact(realIndex));
  }

  private Vector<Node> getNodeVectorByIndex(long index) {
    var realIndex = index % nodeCombinationCount;
    return decPOMDP.getNodeCombinations().get(Math.toIntExact(realIndex));
  }

  private void calculateMatrixRow(SparseStore<Double> matrixBuilder, State state, Vector<Node> nodeVector, long rowIndex) {
    LongStream.range(0, getNumberOfVariables())
      .parallel()
      .forEach(columnIndex -> {
        var newState = getStateByIndex(columnIndex);
        var newNodeVector = getNodeVectorByIndex(columnIndex);
        var coefficient = getCoefficient(state, nodeVector, newState, newNodeVector);
        matrixBuilder.set(rowIndex, columnIndex, coefficient);
      });
  }

  private double getCoefficient(State state, Vector<Node> nodeVector, State newState, Vector<Node> newNodeVector) {
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

  private double calculateAllRewardsForStateAndNodes(State state, Vector<Node> nodeVector) {
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
