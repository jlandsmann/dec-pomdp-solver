package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class HeuristicPolicyIterationSolver extends DecPOMDPSolver<DecPOMDPWithStateController> {
  private static final Logger LOG = LoggerFactory.getLogger(HeuristicPolicyIterationSolver.class);

  protected int numberOfBeliefPoints;
  protected int maxIterations = 0;

  protected Set<Distribution<State>> beliefPoints;
  protected int controllerHash = 0;
  protected int currentIteration = 0;

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
  }

  protected void saveControllerHash() {
    this.controllerHash = decPOMDP.hashCode();
  }

  protected void evaluateValueFunction() {
    LOG.info("Evaluating the value function.");
  }

  protected void performExhaustiveBackup() {
    LOG.info("Performing exhaustive backup.");
  }

  protected void retainDominatingNodes() {
    LOG.info("Retaining dominating nodes.");
  }

  protected void pruneCombinatorialDominatedNodes() {
    LOG.info("Pruning combinatorial dominated nodes.");
  }

  protected boolean hasControllerHashChanged() {
    boolean controllerHashChanged = controllerHash != decPOMDP.hashCode();
    LOG.info("Controller hash changed: {}", controllerHashChanged);
    return controllerHashChanged;
  }

  protected boolean isIterationLimitReached() {
    boolean hasIterationLimit = maxIterations != 0;
    boolean isIterationLimitReached = maxIterations >= currentIteration;
    return hasIterationLimit && isIterationLimitReached;
  }
}
