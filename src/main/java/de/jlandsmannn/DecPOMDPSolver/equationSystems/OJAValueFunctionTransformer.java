package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.SparseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

@Service
public class OJAValueFunctionTransformer<U extends IDecPOMDPWithStateController<?>> implements ValueFunctionTransformer<U, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJAValueFunctionTransformer.class);

  protected U decPOMDP;
  protected long stateCount;
  protected long nodeCombinationCount;
  protected List<State> states;
  protected List<Vector<Node>> nodeCombinations;

  @Override
  public void setDecPOMDP(U decPOMDP) {
    LOG.info("Initialized with DecPOMDP");
    this.decPOMDP = decPOMDP;
    this.states = List.copyOf(decPOMDP.getStates());
    this.stateCount = states.size();
    this.nodeCombinations = List.copyOf(decPOMDP.getNodeCombinations());
    this.nodeCombinationCount = nodeCombinations.size();
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
    var matrixBuilder = SparseStore.R064.make(
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
    var matrixBuilder = SparseStore.R064.make(getNumberOfEquations(), 1);
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

    decPOMDP.clearValueFunction();
    LongStream.range(0, getNumberOfVariables())
      .parallel()
      .forEach(index -> {
        var state = getStateByIndex(index);
        var nodeVector = getNodeVectorByIndex(index);
        var value = values.get(index, 0);
        decPOMDP.setValue(state, nodeVector, value);
      });
  }

  protected void calculateMatrixRow(SparseStore<Double> matrixBuilder, State state, Vector<Node> nodeVector, long rowIndex) {
    matrixBuilder.add(rowIndex, rowIndex, -1);
    decPOMDP.getStates().stream().parallel().forEach(newState -> {
      decPOMDP.getNodeCombinations(nodeVector).stream().parallel().forEach(newNodeVector -> {
        var columnIndex = indexOfStateAndNodeVector(newState, newNodeVector);
        var coefficient = getCoefficient(state, nodeVector, newState, newNodeVector);
        matrixBuilder.add(rowIndex, columnIndex, coefficient);
      });
    });
  }

  protected long indexOfStateAndNodeVector(State state, Vector<Node> nodeVector) {
    var stateIndex = states.indexOf(state);
    var nodeVectorIndex = nodeCombinations.indexOf(nodeVector);
    if (stateIndex == -1) {
      throw new IllegalStateException("Unknown state: " + state);
    } else if (nodeVectorIndex == -1) {
      throw new IllegalStateException("Unknown nodeVector: " + nodeVector);
    }
    return (stateIndex * nodeCombinationCount) + nodeVectorIndex;
  }

  protected State getStateByIndex(long index) {
    var realIndex = Math.floorDiv(index, nodeCombinationCount);
    return states.get(Math.toIntExact(realIndex));
  }

  protected Vector<Node> getNodeVectorByIndex(long index) {
    var realIndex = Math.floorMod(index, nodeCombinationCount);
    return nodeCombinations.get(Math.toIntExact(realIndex));
  }

  protected double getCoefficient(State state, Vector<Node> nodeVector, State newState, Vector<Node> newNodeVector) {
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
      .map(c -> c * decPOMDP.getDiscountFactor())
      .orElse(0D)
      ;
  }

  protected double calculateAllRewardsForStateAndNodes(State state, Vector<Node> nodeVector) {
    return decPOMDP.getActionCombinations(nodeVector)
      .stream()
      .map(actionVector -> {
        var actionProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
        var reward = decPOMDP.getReward(state, actionVector);
        return actionProbability * reward;
      })
      .reduce(Double::sum)
      .orElse(0D);
  }
}
