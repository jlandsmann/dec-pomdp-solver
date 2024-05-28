package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class HeuristicPolicyIterationSolver extends DecPOMDPSolver<DecPOMDPWithStateController, HeuristicPolicyIterationSolver> {
  private static final Logger LOG = LoggerFactory.getLogger(HeuristicPolicyIterationSolver.class);

  protected int numberOfBeliefPoints;
  protected int maxIterations = 0;
  protected Map<Agent, Map<State, Distribution<Action>>> initialPolicies;
  protected Set<Distribution<State>> beliefPoints = new HashSet<>();
  protected double controllerState = 0;
  protected int currentIteration = 0;

  protected final BeliefPointGenerator beliefPointGenerator;
  protected final ValueFunctionEvaluater<DecPOMDPWithStateController, ?> valueFunctionEvaluater;
  protected final ExhaustiveBackupPerformer exhaustiveBackupPerformer;
  protected final DominatingNodesRetainer dominatingNodesRetainer;
  protected final CombinatorialNodePruner<?, ?> combinatorialNodePruner;
  protected final double valueChangeThreshold;

  @Autowired
  public HeuristicPolicyIterationSolver(BeliefPointGenerator beliefPointGenerator,
                                        ValueFunctionEvaluater<DecPOMDPWithStateController, ?> valueFunctionEvaluater,
                                        ExhaustiveBackupPerformer exhaustiveBackupPerformer,
                                        DominatingNodesRetainer dominatingNodesRetainer,
                                        CombinatorialNodePruner<?, ?> combinatorialNodePruner,
                                        HeuristicPolicyIterationConfig config) {
    this.beliefPointGenerator = beliefPointGenerator;
    this.valueFunctionEvaluater = valueFunctionEvaluater;
    this.exhaustiveBackupPerformer = exhaustiveBackupPerformer;
    this.dominatingNodesRetainer = dominatingNodesRetainer;
    this.combinatorialNodePruner = combinatorialNodePruner;
    this.valueChangeThreshold = config.valueChangeThreshold();
  }

  public HeuristicPolicyIterationSolver setNumberOfBeliefPoints(int numberOfBeliefPoints) {
    this.numberOfBeliefPoints = numberOfBeliefPoints;
    return this;
  }

  public HeuristicPolicyIterationSolver setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
    return this;
  }

  public HeuristicPolicyIterationSolver setInitialPolicies(Map<Agent, Map<State, Distribution<Action>>> initialPolicies) {
    this.initialPolicies = initialPolicies;
    return this;
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
      LOG.info("Starting iteration #{}. Current value: {}", ++currentIteration, getValueOfDecPOMDP());
      saveControllerState();
      performExhaustiveBackup();
      retainDominatingNodes();
      pruneCombinatorialDominatedNodes();
      evaluateValueFunction();
    } while (hasControllerStateChanged() && !isIterationLimitReached());
    return getValueOfDecPOMDP();
  }

  protected void generateBeliefPoints() {
    LOG.info("Generating {} belief points to guide the pruning.", numberOfBeliefPoints);
    beliefPoints.clear();
    beliefPointGenerator
      .setDecPOMDP(decPOMDP)
      .setDesiredNumberOfBeliefPoints(numberOfBeliefPoints)
      .setPolicies(initialPolicies);
    var generateBeliefPoints = beliefPointGenerator.generateBeliefPoints();
    beliefPoints.addAll(generateBeliefPoints);
  }

  protected void saveControllerState() {
    this.controllerState = getValueOfDecPOMDP();
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
    for (var agent : decPOMDP.getAgents()) {
      LOG.debug("Pruning combinatorial dominated nodes for Agent {}.", agent);
      combinatorialNodePruner
        .setAgent(agent)
        .setBeliefPoints(beliefPoints)
        .pruneNodesIfCombinatorialDominated();
    }
  }

  protected boolean hasControllerStateChanged() {
    var valueChange = Math.abs(controllerState - getValueOfDecPOMDP());
    boolean controllerStateChanged = valueChange >= valueChangeThreshold;
    LOG.info("Controller state changed: {}", controllerStateChanged);
    return controllerStateChanged;
  }

  protected boolean isIterationLimitReached() {
    boolean hasIterationLimit = maxIterations != 0;
    boolean isIterationLimitReached = maxIterations <= currentIteration;
    LOG.info("Iteration limit enabled: {} and reached: {}", hasIterationLimit, isIterationLimitReached);
    return hasIterationLimit && isIterationLimitReached;
  }
}
