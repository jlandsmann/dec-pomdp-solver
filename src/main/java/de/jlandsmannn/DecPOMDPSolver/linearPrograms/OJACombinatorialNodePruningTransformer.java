package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
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
    this.agent = agent;
  }

  @Override
  public void setBeliefPoints(Collection<Distribution<State>> beliefPoints) {
    this.beliefPoints = beliefPoints;
  }

  @Override
  public ExpressionsBasedModel forNode(Node nodeToCheck) {
    var linearProgram = new ExpressionsBasedModel();
    var agentIndex = decPOMDP.getAgents().indexOf(agent);
    if (agentIndex < 0) throw new IllegalArgumentException("Agent is not part of DecPOMDP");

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
  public Optional<Distribution<Node>> getPruningNodes(Map<String, Double> result) {
    if (result.get("epsilon") <= 0) {
      LOG.info("Epsilon is not positive, no dominating combination exists.");
    }
    Map<Node, Double> mappedResults = new HashMap<>();
    for (var node : agent.getControllerNodes()) {
      mappedResults.put(node, result.getOrDefault(node.name(), 0D));
    }
    var distribution = Distribution.of(mappedResults);
    return Optional.of(distribution);
  }
}
