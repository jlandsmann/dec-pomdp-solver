package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IAgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.solving.BaseDecPOMDPSolverWithConfig;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class implements the heuristic policy iteration algorithm.
 * It starts with a {@link DecPOMDPWithStateController} with (usually) arbitrary controllers.
 * Since it is a heuristic algorithm, the results are not deterministic,
 * and depend on the initialization and on the belief point generation.
 */
@Service
public class HeuristicPolicyIterationSolver
  extends BaseDecPOMDPSolverWithConfig<IDecPOMDPWithStateController<?>, HeuristicPolicyIterationConfig, HeuristicPolicyIterationSolver> {
  private static final Logger LOG = LoggerFactory.getLogger(HeuristicPolicyIterationSolver.class);

  protected final BeliefPointGenerator beliefPointGenerator;
  protected final ValueFunctionEvaluater<IDecPOMDPWithStateController<?>, ?> valueFunctionEvaluater;
  protected final ExhaustiveBackupPerformer exhaustiveBackupPerformer;
  protected final DominatingNodesRetainer dominatingNodesRetainer;
  protected final CombinatorialNodePruner<IDecPOMDPWithStateController<?>, ?, ?> combinatorialNodePruner;


  protected Map<IAgent, Set<Distribution<State>>> beliefPoints;
  protected double controllerState = 0;
  protected int currentIteration = 0;

  @Autowired
  public HeuristicPolicyIterationSolver(BeliefPointGenerator beliefPointGenerator,
                                        ValueFunctionEvaluater<IDecPOMDPWithStateController<?>, ?> valueFunctionEvaluater,
                                        ExhaustiveBackupPerformer exhaustiveBackupPerformer,
                                        DominatingNodesRetainer dominatingNodesRetainer,
                                        CombinatorialNodePruner<IDecPOMDPWithStateController<?>, ?, ?> combinatorialNodePruner) {
    super();
    this.beliefPointGenerator = beliefPointGenerator;
    this.valueFunctionEvaluater = valueFunctionEvaluater;
    this.exhaustiveBackupPerformer = exhaustiveBackupPerformer;
    this.dominatingNodesRetainer = dominatingNodesRetainer;
    this.combinatorialNodePruner = combinatorialNodePruner;
  }

  @Override
  public double solve() {
    LOG.info("Start solving DecPOMDP with heuristic policy iteration.");
    if (decPOMDP.getDiscountFactor() == 1) {
      throw new IllegalStateException("This algorithm does not support discount factor of 1.");
    }
    currentIteration = 0;
    controllerState = 0;
    generateBeliefPoints();
    evaluateValueFunction();
    do {
      LOG.info("Starting iteration #{}. Current value: {}", ++currentIteration, getValue());
      saveControllerState();
      performExhaustiveBackup();
      retainDominatingNodes();
      pruneCombinatorialDominatedNodes();
      evaluateValueFunction();
    } while (hasControllerStateChanged() && !isIterationLimitReached());
    return getValue();
  }

  protected void generateBeliefPoints() {
    LOG.info("Generating {} belief points to guide the pruning.", config.beliefPointDesiredNumber());
    beliefPoints = new HashMap<>();
    beliefPointGenerator
      .setDecPOMDP(decPOMDP)
      .setDesiredNumberOfBeliefPoints(config.beliefPointDesiredNumber())
      .setPolicies(config.initialPolicies());
    var generatedBeliefPoints = beliefPointGenerator.generateBeliefPoints();
    beliefPoints.putAll(generatedBeliefPoints);
  }

  protected void saveControllerState() {
    this.controllerState = getValue();
    LOG.debug("Saving controller state: {} ", controllerState);
  }

  protected void evaluateValueFunction() {
    LOG.info("Evaluating the value function.");
    valueFunctionEvaluater
      .setDecPOMDP(decPOMDP)
      .evaluateValueFunction();
  }

  protected void performExhaustiveBackup() {
    LOG.info("Performing exhaustive backup.");
    exhaustiveBackupPerformer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .performExhaustiveBackup();
  }

  protected void retainDominatingNodes() {
    LOG.info("Retaining dominating nodes.");
    dominatingNodesRetainer
      .setDecPOMDP(decPOMDP)
      .setBeliefPoints(beliefPoints)
      .retainDominatingNodes();
  }

  protected void pruneCombinatorialDominatedNodes() {
    LOG.info("Pruning combinatorial dominated nodes.");
    combinatorialNodePruner.setDecPOMDP(decPOMDP);
    for (IAgentWithStateController agent : decPOMDP.getAgents()) {
      LOG.debug("Pruning combinatorial dominated nodes for Agent {}.", agent);
      combinatorialNodePruner
        .setAgent(agent)
        .setBeliefPoints(beliefPoints.get(agent))
        .pruneNodesIfCombinatorialDominated();
    }
  }

  protected boolean hasControllerStateChanged() {
    var valueChange = Math.abs(controllerState - getValue());
    boolean controllerStateChanged = valueChange >= config.valueChangeThreshold();
    LOG.info("Controller state changed: {}", controllerStateChanged);
    return true;
  }

  protected boolean isIterationLimitReached() {
    boolean hasIterationLimit = config.maxIterations() != 0;
    boolean isIterationLimitReached = config.maxIterations() <= currentIteration;
    LOG.info("Iteration limit enabled: {} and reached: {}", hasIterationLimit, isIterationLimitReached);
    return hasIterationLimit && isIterationLimitReached;
  }
}
