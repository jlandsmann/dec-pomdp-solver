package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OJATransformer implements ValueFunctionTransformer<DecPOMDPWithStateController, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJATransformer.class);
  private final LocalValidatorFactoryBean defaultValidator;

  private DecPOMDPWithStateController decPOMDP;
  private long stateCount;
  private long nodeCombinationCount;
  private List<Vector<Node>> nodeCombinations;
  private List<Vector<Action>> actionCombinations;
  private List<Vector<Observation>> observationsCombinations;

  public OJATransformer(LocalValidatorFactoryBean defaultValidator) {
    this.defaultValidator = defaultValidator;
  }

  @Override
  public void setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    LOG.info("Initialized with DecPOMDP");
    this.decPOMDP = decPOMDP;
    var nodeCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getControllerNodes).toList();
    this.nodeCombinations = VectorStreamBuilder.forEachCombination(nodeCombinations).toList();
    var actionCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getActions).toList();
    this.actionCombinations = VectorStreamBuilder.forEachCombination(actionCombinations).toList();
    var observationsCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getObservations).toList();
    this.observationsCombinations = VectorStreamBuilder.forEachCombination(observationsCombinations).toList();

    this.stateCount = this.decPOMDP.getStates().size();
    this.nodeCombinationCount = VectorStreamBuilder.forEachCombination(nodeCombinations).count();
  }

  @Override
  public MatrixStore<Double> getMatrixFromDecPOMDP() {
    LOG.info("Retrieving transition matrix from DecPOMDP");
    var matrixBuilder = Primitive64Store.FACTORY.make(
      stateCount * nodeCombinationCount,
      stateCount * nodeCombinationCount
    );

    AtomicLong row = new AtomicLong(0);
    AtomicLong col = new AtomicLong(0);
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : nodeCombinations) {
        col.set(0);
        for (var newState : decPOMDP.getStates()) {
          for (var newNodeVector : nodeCombinations) {
            var coefficient = getCoefficient(state, nodeVector, newState, newNodeVector);
            matrixBuilder.set(row.get(), col.get(), coefficient);
            col.getAndAdd(1);
          }
        }
        row.getAndAdd(1);
      }
    }

    LOG.debug("Retrieved matrix from DecPOMDP: " + matrixBuilder.get());
    return matrixBuilder.get();
  }

  @Override
  public MatrixStore<Double> getVectorFromDecPOMDP() {
    LOG.info("Retrieving reward vector from DecPOMDP");
    var matrixBuilder = Primitive64Store.FACTORY.make(stateCount * nodeCombinationCount,1);
    AtomicLong index = new AtomicLong();
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : nodeCombinations) {
        var reward = calculateAllRewardsForStateAndNodes(state, nodeVector);
        matrixBuilder.set(index.getAndAdd(1), 0, -reward);
      }
    }
    LOG.debug("Retrieved reward vector from DecPOMDP: " + matrixBuilder.get());
    return matrixBuilder.get();
  }

  @Override
  public void applyValuesToDecPOMDP(MatrixStore<Double> values) {
    LOG.info("Applying values to DecPOMDP");

    AtomicLong index = new AtomicLong();
    for (var state : decPOMDP.getStates()) {
      for (var nodeVector : nodeCombinations) {
        var value = values.get(index.getAndAdd(1), 0);
        LOG.info("Value for state {} and node vector {} is {}", state, nodeVector.hashCode(), value);
        decPOMDP.setValue(state, nodeVector, value);
      }
    }
  }

  private double getCoefficient(State state, Vector<Node> nodeVector, State newState, Vector<Node> newNodeVector) {
    var coefficient = 0D;

    for (var actionVector : actionCombinations) {
      for (var observationVector : observationsCombinations) {
        var action = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
        var stateTransition = decPOMDP.getStateTransitionProbability(state, actionVector, observationVector, newState);
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
