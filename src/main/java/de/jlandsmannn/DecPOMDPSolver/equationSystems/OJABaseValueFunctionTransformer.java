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

import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

public abstract class OJABaseValueFunctionTransformer<U extends IDecPOMDPWithStateController<?>> implements ValueFunctionTransformer<U, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJABaseValueFunctionTransformer.class);

  protected U decPOMDP;
  protected long stateCount;
  protected long nodeCombinationCount;

  @Override
  public void setDecPOMDP(U decPOMDP) {
    LOG.info("Initialized with DecPOMDP");
    this.decPOMDP = decPOMDP;
    this.stateCount = decPOMDP.getStates().size();
    this.nodeCombinationCount = decPOMDP.getNodeCombinations().size();
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

    decPOMDP.clearValueFunction();
    AtomicLong index = new AtomicLong();
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : decPOMDP.getNodeCombinations()) {
        var value = values.get(index.getAndIncrement(), 0);
        LOG.debug("Value for state {} and node vector {} is {}", state, nodeVector, value);
        decPOMDP.setValue(state, nodeVector, value);
      }
    }
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
    var stateIndex = decPOMDP.getStates().indexOf(state);
    var nodeVectorIndex = decPOMDP.getNodeCombinations().indexOf(nodeVector);
    return (stateIndex * nodeCombinationCount) + nodeVectorIndex;
  }

  protected State getStateByIndex(long index) {
    var realIndex = Math.floorDiv(index, nodeCombinationCount);
    return decPOMDP.getStates().get(Math.toIntExact(realIndex));
  }

  protected Vector<Node> getNodeVectorByIndex(long index) {
    var realIndex = index % nodeCombinationCount;
    return decPOMDP.getNodeCombinations().get(Math.toIntExact(realIndex));
  }

  protected abstract double getCoefficient(State state, Vector<Node> nodeVector, State newState, Vector<Node> newNodeVector);

  protected abstract double calculateAllRewardsForStateAndNodes(State state, Vector<Node> nodeVector);
}
