package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IAgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * This is an implementation of the {@link CombinatorialNodePruningTransformer}
 * and creates a linear program for an agent's node of a {@link DecPOMDPWithStateController}
 * to find a dominating combination of nodes.
 * In addition to that, it creates such a combination of nodes
 * as a {@link Distribution} from the results of the linear program.
 */
@Service
public class ORCombinatorialNodePruningTransformer implements CombinatorialNodePruningTransformer<IDecPOMDPWithStateController<?>, MPSolver, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(ORCombinatorialNodePruningTransformer.class);

  private IDecPOMDPWithStateController<?> decPOMDP;
  private IAgentWithStateController agent;
  private Collection<Distribution<State>> beliefPoints;

  @Override
  public void setDecPOMDP(IDecPOMDPWithStateController<?> decPOMDP) {
    this.decPOMDP = decPOMDP;
  }

  @Override
  public void setAgent(IAgentWithStateController agent) {
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
  public MPSolver getLinearProgramForNode(Node nodeToCheck) {
    validateDependencies(nodeToCheck);
    var linearProgram = MPSolver.createSolver("GLOP");
    var agentIndex = decPOMDP.getAgents().indexOf(agent);
    var epsilon = linearProgram.makeNumVar(0, MPSolver.infinity(), "epsilon");
    var constant = linearProgram.makeNumVar(1, 1,"constant=1");
    var nodeDistribution = linearProgram.makeConstraint(1,1, "x(q)");

    var nodeVariables = new HashMap<Node, MPVariable>();
    for (var node : agent.getInitialControllerNodes()) {
      var nodeVariable = linearProgram.makeNumVar(0, 1, node.name());
      nodeDistribution.setCoefficient(nodeVariable, 1);
      nodeVariables.put(node, nodeVariable);
    }

    var rawNodeCombinations = decPOMDP.getAgents().stream()
      .filter(a -> !a.equals(agent))
      .map(IAgentWithStateController::getInitialControllerNodes)
      .map(List::copyOf)
      .toList();
    var nodeCombinations = VectorCombinationBuilder.listOf(rawNodeCombinations);


    var beliefStateIndex = 0;
    for (var beliefState : beliefPoints) {
      var nodeVectorIndex = 0;
      for (var nodeVector : nodeCombinations) {
        var expression = linearProgram.makeConstraint(0, MPSolver.infinity(), "b: " + beliefStateIndex + ", q-i: " + nodeVectorIndex);
        expression.setCoefficient(epsilon, -1);
        var nodeToCheckVector = Vector.addEntry(nodeVector, agentIndex, nodeToCheck);
        var nodeToCheckValue = decPOMDP.getValue(beliefState, nodeToCheckVector);
        expression.setCoefficient(constant, -nodeToCheckValue);

        for (var node : agent.getInitialControllerNodes()) {
          var nodeVariable = nodeVariables.get(node);
          var vector = Vector.addEntry(nodeVector, agentIndex, node);
          var value = decPOMDP.getValue(beliefState, vector);
          expression.setCoefficient(nodeVariable, value);
        }
        nodeVectorIndex++;
      }
      beliefStateIndex++;
    }
    linearProgram.objective().setCoefficient(epsilon, 1);
    LOG.debug("Created linear program with {} variables and {} constraints.", linearProgram.numVariables(), linearProgram.numConstraints());
    return linearProgram;
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
