package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
    var epsilon = linearProgram.newVariable("epsilon").lower(0);
    var constant = linearProgram.newVariable("constant").level(1);
    var nodeDistribution = linearProgram.newExpression("x(q)").level(1);

    for (var node : agent.getControllerNodes()) {
      if (node.equals(nodeToCheck)) continue;
      var nodeVariable = linearProgram.newVariable(node.name()).lower(0).upper(1);
      nodeDistribution.add(nodeVariable, 1);
    }

    var rawNodeCombinations = decPOMDP.getAgents().stream().filter(a -> !a.equals(agent)).map(a -> a.getControllerNodes()).toList();
    var nodeCombinations = VectorStreamBuilder.forEachCombination(rawNodeCombinations).toList();


    for (var beliefState : beliefPoints) {
      for (var nodeVector : nodeCombinations) {
        var expression = linearProgram.newExpression("b: " + beliefState.hashCode() + ", q-i: " + nodeVector.hashCode());
        var nodeToCheckVector = Vector.addEntry(nodeVector, agentIndex, nodeToCheck);
        expression.lower(epsilon);

        for (var state : decPOMDP.getStates()) {
          var stateProbability = beliefState.getProbability(state);
          var nodeToCheckValue = decPOMDP.getValue(state, nodeToCheckVector);
          expression.add(constant, stateProbability * -nodeToCheckValue);

          for (var node : agent.getControllerNodes()) {
            if (node.equals(nodeToCheck)) continue;
            var nodeVariable = linearProgram.getVariables().stream().filter(v -> v.getName().equals(node.name())).findFirst();
            var vector = Vector.addEntry(nodeVector, agentIndex, node);
            var value = decPOMDP.getValue(state, vector);
            expression.add(nodeVariable.get(), stateProbability * value);
          }
        }
      }
    }

    LOG.debug("Created linear program.");
    return linearProgram;
  }

  @Override
  public Optional<Distribution<Node>> getDominatingNodeDistributionFromResult(Map<String, Double> result) {
    if (result.get("epsilon") < 0) {
      LOG.debug("Epsilon is negative, no dominating combination exists.");
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
