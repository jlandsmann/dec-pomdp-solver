package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionEvaluater;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruner;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.linearPrograms.OJALinearProgramSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class HeuristicPolicyIterationSolver extends DecPOMDPSolver<DecPOMDPWithStateController> {
  private static final Logger LOG = LoggerFactory.getLogger(HeuristicPolicyIterationSolver.class);

  protected int numberOfBeliefPoints;
  protected int maxIterations = 0;

  protected Map<Agent, Set<Distribution<State>>> beliefPoints = new HashMap<>();
  protected int controllerHash = 0;
  protected int currentIteration = 0;

  protected final ValueFunctionEvaluater<DecPOMDPWithStateController, ?> valueFunctionEvaluater;
  protected final CombinatorialNodePruner<?, ?> combinatorialNodePruner;

  @Autowired
  public HeuristicPolicyIterationSolver(ValueFunctionEvaluater<DecPOMDPWithStateController, ?> valueFunctionEvaluater, CombinatorialNodePruner<?, ?> combinatorialNodePruner) {
    this.valueFunctionEvaluater = valueFunctionEvaluater;
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
    generateBeliefPoints();
    do {
      LOG.info("Starting iteration #{}", ++currentIteration);
      saveControllerHash();
      evaluateValueFunction();
      performExhaustiveBackup();
      retainDominatingNodes();
      pruneCombinatorialDominatedNodes();
    } while (hasControllerHashChanged() && !isIterationLimitReached());
    return getValueOfDecPOMDP();
  }

  protected void generateBeliefPoints() {
    LOG.info("Generating {} belief points for guiding the pruning.", numberOfBeliefPoints);
    for (var agent : decPOMDP.getAgents()) {
      var collection = new HashSet<Distribution<State>>();
      collection.add(initialBeliefState);
      beliefPoints.put(agent, collection);
    }
  }

  protected void saveControllerHash() {
    this.controllerHash = decPOMDP.hashCode();
  }

  protected void evaluateValueFunction() {
    LOG.info("Evaluating the value function.");
    valueFunctionEvaluater.evaluateValueFunction(decPOMDP);
  }

  protected void performExhaustiveBackup() {
    LOG.info("Performing exhaustive backup.");
  }

  protected void retainDominatingNodes() {
    LOG.info("Retaining dominating nodes.");
  }

  protected void pruneCombinatorialDominatedNodes() {
    LOG.info("Pruning combinatorial dominated nodes.");
    var agent = decPOMDP.getAgents().get(0);
    var agentBeliefPoints = beliefPoints.get(agent);
    combinatorialNodePruner.pruneNodesIfCombinatorialDominated(decPOMDP, agent, agentBeliefPoints);
  }

  protected boolean hasControllerHashChanged() {
    boolean controllerHashChanged = controllerHash != decPOMDP.hashCode();
    LOG.debug("Controller hash changed: {}", controllerHashChanged);
    return controllerHashChanged;
  }

  protected boolean isIterationLimitReached() {
    boolean hasIterationLimit = maxIterations != 0;
    boolean isIterationLimitReached = maxIterations <= currentIteration;
    LOG.debug("Iteration limit enabled: {}", hasIterationLimit);
    LOG.debug("Iteration limit reached: {}", isIterationLimitReached);
    return hasIterationLimit && isIterationLimitReached;
  }
}
