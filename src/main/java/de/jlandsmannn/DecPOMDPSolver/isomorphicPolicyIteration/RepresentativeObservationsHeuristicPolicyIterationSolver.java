package de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.RepresentativeObservationsDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.solving.BaseDecPOMDPSolverWithConfig;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationSolver;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.ValueFunctionEvaluater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class implements the heuristic policy iteration algorithm.
 * It starts with a {@link DecPOMDPWithStateController} with (usually) arbitrary controllers.
 * Since it is a heuristic algorithm, the results are not deterministic,
 * and depend on the initialization and on the belief point generation.
 */
@Service
public class RepresentativeObservationsHeuristicPolicyIterationSolver
  extends BaseDecPOMDPSolverWithConfig<RepresentativeObservationsDecPOMDPWithStateController, IsomorphicHeuristicPolicyIterationConfig, RepresentativeObservationsHeuristicPolicyIterationSolver> {
  private static final Logger LOG = LoggerFactory.getLogger(RepresentativeObservationsHeuristicPolicyIterationSolver.class);

  protected final HeuristicPolicyIterationSolver solver;
  protected final ValueFunctionEvaluater<IDecPOMDPWithStateController<?>, ?> valueFunctionEvaluater;

  protected DecPOMDPWithStateController representativeDecPOMDP;

  @Autowired
  public RepresentativeObservationsHeuristicPolicyIterationSolver(IsomorphicHeuristicPolicyIterationConfig config,
                                                                  HeuristicPolicyIterationSolver solver,
                                                                  ValueFunctionEvaluater<IDecPOMDPWithStateController<?>, ?> valueFunctionEvaluater) {
    super();
    this.config = config;
    this.solver = solver;
    this.valueFunctionEvaluater = valueFunctionEvaluater;
  }

  @Override
  public double solve() {
    LOG.info("Start solving DecPOMDP with isomorphic heuristic policy iteration with representative observations.");
    LOG.info("Isomorphic DecPOMDP has {} states and {} agents", decPOMDP.getStates().size(), decPOMDP.getTotalAgentCount());
    createRepresentativeDecPOMDP();
    solveRepresentativeDecPOMDP();
    transferController();
    evaluateValueFunction();
    return decPOMDP.getValue();
  }

  protected void createRepresentativeDecPOMDP() {
    LOG.info("Creating representative DecPOMDP from isomorphic DecPOMDP.");
    List<AgentWithStateController> agents = decPOMDP.getAgents().stream().map(AgentWithStateController::new).toList();
    representativeDecPOMDP = new DecPOMDPWithStateController(
      agents,
      decPOMDP.getStates(),
      decPOMDP.getDiscountFactor(),
      decPOMDP.getInitialBeliefState(),
      decPOMDP.getTransitionFunction(),
      decPOMDP.getRewardFunction(),
      decPOMDP.getObservationFunction()
    );
  }

  protected void solveRepresentativeDecPOMDP() {
    LOG.info("Solving representative DecPOMDP.");
    solver
      .setConfig(config.policyIterationConfig())
      .setDecPOMDP(representativeDecPOMDP)
      .solve();
    LOG.info("Solving representative DecPOMDP resulted in value of {}.", representativeDecPOMDP.getValue());
  }

  protected void transferController() {
    LOG.info("Transferring local controller from representative DecPOMDP to isomorphic DecPOMDP.");
    for (int idx = 0; idx < decPOMDP.getAgentCount(); idx++) {
      var agent = decPOMDP.getAgents().get(idx);
      var otherAgent = representativeDecPOMDP.getAgents().get(idx);
      agent.setController(otherAgent.getController());
    }
  }

  protected void evaluateValueFunction() {
    LOG.info("Evaluating the value function for isomorphic DecPOMDP.");
    valueFunctionEvaluater
      .setDecPOMDP(decPOMDP)
      .evaluateValueFunction();
  }
}
