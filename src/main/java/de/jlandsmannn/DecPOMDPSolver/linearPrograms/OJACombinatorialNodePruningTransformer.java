package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.GenericSolver;
import org.ojalgo.optimisation.Optimisation;
import org.ojalgo.optimisation.Variable;
import org.ojalgo.optimisation.linear.LinearSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OJACombinatorialNodePruningTransformer implements CombinatorialNodePruningTransformer<ExpressionsBasedModel, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJACombinatorialNodePruningTransformer.class);

  private DecPOMDPWithStateController decPOMDP;
  private AgentWithStateController agent;
  private Collection<Distribution<State>> beliefPoints;

  @Override
  public void setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    this.decPOMDP = decPOMDP;
  }

  @Override
  public void setAgent(AgentWithStateController agent) {
    if (decPOMDP == null) {
      throw new IllegalStateException("DecPOMDP must be set to select agent");
    } else if (!decPOMDP.getAgents().contains(agent)) {
      throw new IllegalArgumentException("DecPOMDP does not contain given agent");
    }
    this.agent = agent;
  }

  @Override
  public void setBeliefPoints(Collection<Distribution<State>> beliefPoints) {
    if (beliefPoints.isEmpty()) {
      throw new IllegalArgumentException("Belief points must not be empty");
    }
    this.beliefPoints = beliefPoints;
  }

  @Override
  public ExpressionsBasedModel getLinearProgramForNode(Node nodeToCheck) {
    validateDependencies(nodeToCheck);
    var linearProgram = new ExpressionsBasedModel();
    var agentIndex = decPOMDP.getAgents().indexOf(agent);
    var epsilon = linearProgram.newVariable("epsilon").lower(0).weight(1);
    var constant = linearProgram.newVariable("constant=1").level(1);
    var nodeDistribution = linearProgram.newExpression("x(q)").level(1);

    var nodeVariables = new HashMap<Node, Variable>();
    for (var node : agent.getInitialControllerNodes()) {
      var nodeVariable = linearProgram.newVariable(node.name()).lower(0).upper(1);
      nodeDistribution.add(nodeVariable, 1);
      nodeVariables.put(node, nodeVariable);
    }

    var rawNodeCombinations = decPOMDP.getAgents().stream()
      .filter(a -> !a.equals(agent))
      .map(AgentWithStateController::getInitialControllerNodes)
      .map(List::copyOf)
      .toList();
    var nodeCombinations = VectorCombinationBuilder.listOf(rawNodeCombinations);


    var beliefStateIndex = 0;
    for (var beliefState : beliefPoints) {
      var nodeVectorIndex = 0;
      for (var nodeVector : nodeCombinations) {
        var expression = linearProgram.newExpression("b: " + beliefStateIndex + ", q-i: " + nodeVectorIndex).lower(0);
        expression.add(epsilon, -1);
        var nodeToCheckVector = Vector.addEntry(nodeVector, agentIndex, nodeToCheck);
        var nodeToCheckValue = decPOMDP.getValue(beliefState, nodeToCheckVector);
        expression.add(constant, -nodeToCheckValue);

        for (var node : agent.getInitialControllerNodes()) {
          var nodeVariable = nodeVariables.get(node);
          var vector = Vector.addEntry(nodeVector, agentIndex, node);
          var value = decPOMDP.getValue(beliefState, vector);
          expression.add(nodeVariable, value);
        }
        nodeVectorIndex++;
      }
      beliefStateIndex++;
    }
    LOG.debug("Created linear program with {} variables and {} expressions.", linearProgram.countVariables(), linearProgram.countExpressions());
    return linearProgram.simplify();
  }

  @Override
  public Optional<Distribution<Node>> getDominatingNodeDistributionFromResult(Map<String, Double> result) {
    if (result.get("epsilon") <= 0) {
      LOG.debug("Epsilon is not positive, no dominating combination exists.");
      return Optional.empty();
    }
    Map<Node, Double> mappedResults = new HashMap<>();
    for (var node : agent.getControllerNodes()) {
      var probability = result.getOrDefault(node.name(), 0D);
      if (probability <= 0D) continue;
      mappedResults.put(node, probability);
    }
    LOG.debug("Dominating combination consists of {} nodes", mappedResults.keySet().size());
    var distribution = Distribution.of(mappedResults);
    return Optional.of(distribution);
  }

  private void validateDependencies(Node node) {
    if (decPOMDP == null || agent == null || beliefPoints == null) {
      throw new IllegalStateException("DecPOMDP, agent and beliefPoints must be set to create linear program");
    } else if (!agent.getControllerNodes().contains(node)) {
      throw new IllegalArgumentException("NodeToCheck must be part of " + agent);
    }
  }
}
