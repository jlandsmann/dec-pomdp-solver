package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionEvaluater;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruner;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class HeuristicPolicyIterationSolver extends DecPOMDPSolver<DecPOMDPWithStateController> {
  private static final Logger LOG = LoggerFactory.getLogger(HeuristicPolicyIterationSolver.class);
  private static final double VALUE_CHANGE_THRESHOLD = 1e-6;

  protected int numberOfBeliefPoints;
  protected int maxIterations = 0;

  protected Map<AgentWithStateController, Set<Distribution<State>>> beliefPoints = new HashMap<>();
  protected double controllerState = 0;
  protected int currentIteration = 0;

  protected final BeliefPointGenerator beliefPointGenerator;
  protected final ValueFunctionEvaluater<DecPOMDPWithStateController, ?> valueFunctionEvaluater;
  protected final ExhaustiveBackupPerformer exhaustiveBackupPerformer;
  protected final DominatingNodesRetainer dominatingNodesRetainer;
  protected final CombinatorialNodePruner<?, ?> combinatorialNodePruner;

  @Autowired
  public HeuristicPolicyIterationSolver(BeliefPointGenerator beliefPointGenerator,
                                        ValueFunctionEvaluater<DecPOMDPWithStateController, ?> valueFunctionEvaluater,
                                        ExhaustiveBackupPerformer exhaustiveBackupPerformer,
                                        DominatingNodesRetainer dominatingNodesRetainer,
                                        CombinatorialNodePruner<?, ?> combinatorialNodePruner) {
    this.beliefPointGenerator = beliefPointGenerator;
    this.valueFunctionEvaluater = valueFunctionEvaluater;
    this.exhaustiveBackupPerformer = exhaustiveBackupPerformer;
    this.dominatingNodesRetainer = dominatingNodesRetainer;
    this.combinatorialNodePruner = combinatorialNodePruner;
  }

  public HeuristicPolicyIterationSolver setNumberOfBeliefPoints(int numberOfBeliefPoints) {
    this.numberOfBeliefPoints = numberOfBeliefPoints;
    return this;
  }

  public HeuristicPolicyIterationSolver setMaxIterations(int maxIterations) {
    this.maxIterations = maxIterations;
    return this;
  }

  @Override
  public double solve() {
    LOG.info("Start solving DecPOMDP with heuristic policy iteration.");
    currentIteration = 0;
    generateBeliefPoints();
    do {
      LOG.info("Starting iteration #{}. Current value: {}", ++currentIteration, getValueOfDecPOMDP());
      saveControllerState();
      evaluateValueFunction();
      performExhaustiveBackup();
      retainDominatingNodes();
      pruneCombinatorialDominatedNodes();
    } while (hasControllerStateChanged() && !isIterationLimitReached());
    return getValueOfDecPOMDP();
  }

  protected void generateBeliefPoints() {
    LOG.info("Generating {} belief points for each agent to guide the pruning.", numberOfBeliefPoints);
    beliefPointGenerator
      .setDecPOMDP(decPOMDP)
      .setInitialBeliefState(initialBeliefState)
      .setDesiredNumberOfBeliefPoints(numberOfBeliefPoints)
      .generateRandomPolicies();

    for (var agent : decPOMDP.getAgents()) {
      LOG.debug("Generating {} belief points for {} to guide the pruning.", numberOfBeliefPoints, agent);
      var generateBeliefPointsForAgent = beliefPointGenerator.generateBeliefPointsForAgent(agent);
      beliefPoints.put(agent, generateBeliefPointsForAgent);
    }
  }

  protected void saveControllerState() {
    this.controllerState = getValueOfDecPOMDP();
    LOG.debug("Saving controller state: {} ", controllerState);
  }

  protected void evaluateValueFunction() {
    LOG.info("Evaluating the value function.");
    valueFunctionEvaluater
      .evaluateValueFunction(decPOMDP);
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
    for (var agent : decPOMDP.getAgents()) {
      var agentBeliefPoints = beliefPoints.get(agent);
      LOG.debug("Pruning combinatorial dominated nodes for Agent {}.", agent);
      combinatorialNodePruner.pruneNodesIfCombinatorialDominated(decPOMDP, agent, agentBeliefPoints);
    }
  }

  protected boolean hasControllerStateChanged() {
    var valueChange = Math.abs(controllerState - getValueOfDecPOMDP());
    boolean controllerStateChanged = valueChange >= VALUE_CHANGE_THRESHOLD;
    LOG.debug("Controller state changed: {}", controllerStateChanged);
    return controllerStateChanged;
  }

  protected boolean isIterationLimitReached() {
    boolean hasIterationLimit = maxIterations != 0;
    boolean isIterationLimitReached = maxIterations <= currentIteration;
    LOG.debug("Iteration limit enabled: {} and reached: {}", hasIterationLimit, isIterationLimitReached);
    return hasIterationLimit && isIterationLimitReached;
  }
}
