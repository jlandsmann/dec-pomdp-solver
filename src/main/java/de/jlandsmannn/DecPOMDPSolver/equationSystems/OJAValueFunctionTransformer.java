package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.EquationSystemSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.PhysicalStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.matrix.store.SparseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class implements the {@link ValueFunctionTransformer}
 * and transforms a {@link DecPOMDPWithStateController}
 * into a matrix and vector, that work with the OjAlgo library.
 */
@Service
public class OJAValueFunctionTransformer implements ValueFunctionTransformer<DecPOMDPWithStateController, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJAValueFunctionTransformer.class);

  private DecPOMDPWithStateController decPOMDP;
  private long stateCount;
  private long nodeCombinationCount;
  private List<Vector<Node>> nodeCombinations;
  private List<Vector<Action>> actionCombinations;
  private List<Vector<Observation>> observationsCombinations;

  @Override
  public void setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    LOG.info("Initialized with DecPOMDP");
    this.decPOMDP = decPOMDP;
    var nodeCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getControllerNodes).toList();
    this.nodeCombinations = VectorCombinationBuilder.listOf(nodeCombinations);
    var actionCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getActions).toList();
    this.actionCombinations = VectorCombinationBuilder.listOf(actionCombinations);
    var observationsCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getObservations).toList();
    this.observationsCombinations = VectorCombinationBuilder.listOf(observationsCombinations);

    this.stateCount = decPOMDP.getStates().size();
    this.nodeCombinationCount = this.nodeCombinations.size();
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
    LOG.info("Retrieving transition matrix from DecPOMDP");
    var matrixBuilder = SparseStore.R032.make(
      stateCount * nodeCombinationCount,
      stateCount * nodeCombinationCount
    );

    AtomicLong row = new AtomicLong(0L);
    decPOMDP.getStates().stream().parallel().forEach(state -> {
      nodeCombinations.stream().parallel().forEach(nodeVector -> {
        calculateMatrixRow(matrixBuilder, state, nodeVector, row.getAndIncrement());
        if (row.get() % 100 == 0) {
          LOG.info("Calculated {} / {} rows for transition matrix", row.get(), getNumberOfEquations());
        }
      });
    });
    return matrixBuilder;
  }

  @Override
  public MatrixStore<Double> getVectorFromDecPOMDP() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to get vector");
    LOG.info("Retrieving reward vector from DecPOMDP");
    var matrixBuilder = SparseStore.R032.make(stateCount * nodeCombinationCount,1);
    AtomicLong index = new AtomicLong();
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : nodeCombinations) {
        var reward = calculateAllRewardsForStateAndNodes(state, nodeVector);
        matrixBuilder.set(index.getAndAdd(1), 0, -reward);
      }
    }
    return matrixBuilder;
  }

  @Override
  public void applyValuesToDecPOMDP(MatrixStore<Double> values) {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to apply values");
    LOG.info("Applying values to DecPOMDP");

    AtomicLong index = new AtomicLong();
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : nodeCombinations) {
        var value = values.get(index.getAndIncrement(), 0);
        LOG.debug("Value for state {} and node vector {} is {}", state, nodeVector, value);
        decPOMDP.setValue(state, nodeVector, value);
      }
    }
  }

  private void calculateMatrixRow(SparseStore<Double> matrixBuilder, State state, Vector<Node> nodeVector, long row) {
    AtomicLong col = new AtomicLong(0);
    for (var newState : decPOMDP.getStates()) {
      for (var newNodeVector : nodeCombinations) {
        var coefficient = getCoefficient(state, nodeVector, newState, newNodeVector);
        matrixBuilder.set(row, col.get(), coefficient);
        col.getAndIncrement();
      }
    }
  }

  private double getCoefficient(State state, Vector<Node> nodeVector, State newState, Vector<Node> newNodeVector) {
    var coefficient = 0D;

    for (var actionVector : actionCombinations) {
      for (var observationVector : observationsCombinations) {
        var action = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
        var stateTransition = decPOMDP.getTransitionProbability(state, actionVector, observationVector, newState);
        var nodeTransition = decPOMDP.getNodeTransitionProbability(nodeVector, actionVector, observationVector, newNodeVector);
        coefficient += action * stateTransition * nodeTransition;
      }
    }

    coefficient *= decPOMDP.getDiscountFactor();

    if (state.equals(newState) && nodeVector.equals(newNodeVector)) {
      coefficient -= 1D;
    }

    return coefficient;
  }

  private double calculateAllRewardsForStateAndNodes(State state, Vector<Node> nodeVector) {
    return actionCombinations.stream()
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
