package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExhaustiveBackupPerformer {
  private static final Logger LOG = LoggerFactory.getLogger(ExhaustiveBackupPerformer.class);
  private DecPOMDPWithStateController decPOMDP;

  public ExhaustiveBackupPerformer setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    LOG.debug("Retrieving DecPOMDP: {}", decPOMDP);
    this.decPOMDP = decPOMDP;
    return this;
  }

  public void performExhaustiveBackup(DecPOMDPWithStateController decPOMDP) {
    LOG.info("Performing global exhaustive backup");
    for (var agent : decPOMDP.getAgents()) {
      performExhaustiveBackupForAgent(agent);
    }

    var rawNodeCombinations = decPOMDP.getAgents().stream().map(a -> a.getControllerNodes()).toList();
    var nodeCombinations = VectorStreamBuilder.forEachCombination(rawNodeCombinations).toList();
    for (var state : decPOMDP.getStates()) {
      for (var nodeCombination : nodeCombinations) {
        if (decPOMDP.getValue(state, nodeCombination) != 0D) continue;
        var value = calculateValue(decPOMDP, state, nodeCombination);
        decPOMDP.setValue(state, nodeCombination, value);
      }
    }
  }

  private double calculateValue(DecPOMDPWithStateController decPOMDP, State state, Vector<Node> nodeVector) {
    var rawNodeCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getControllerNodes).toList();
    var nodeCombinations = VectorStreamBuilder.forEachCombination(rawNodeCombinations).toList();
    var rawActionCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getActions).toList();
    var actionCombinations = VectorStreamBuilder.forEachCombination(rawActionCombinations).toList();
    var rawObservationsCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getObservations).toList();
    var observationsCombinations = VectorStreamBuilder.forEachCombination(rawObservationsCombinations).toList();

    var value = 0D;
    for (var actionVector : actionCombinations) {
      value += decPOMDP.getActionVectorProbability(nodeVector, actionVector);
      for (var observationVector : observationsCombinations) {
        for (var newNodeVector : nodeCombinations) {
          for (var newState : decPOMDP.getStates()) {
            var discount = decPOMDP.getDiscountFactor();
            var action = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
            var stateTransition = decPOMDP.getStateTransitionProbability(state, actionVector, observationVector, newState);
            var nodeTransition = decPOMDP.getNodeTransitionProbability(nodeVector, actionVector, observationVector, newNodeVector);
            var followValue = decPOMDP.getValue(newState, newNodeVector);
            value += discount * action * stateTransition * nodeTransition * followValue;
          }
        }
      }
    }
    return value;
  }

  protected void performExhaustiveBackupForAgent(AgentWithStateController agent) {
    long nodesAdded = 0;
    var nodes = List.copyOf(agent.getControllerNodes());
    for (var action : agent.getActions()) {
      var rawObservationNodeCombinations = agent.getObservations().stream().map(o -> nodes).toList();
      var observationNodeCombinations = VectorStreamBuilder.forEachCombination(rawObservationNodeCombinations).toList();
      for (var observationNodeCombination : observationNodeCombinations) {
        var node = Node.of("Q" + agent.getControllerNodeIndex());
        agent.addNode(node, Distribution.createSingleEntryDistribution(action));
        nodesAdded++;
        var observationIndex = 0;
        for (var observation : agent.getObservations()) {
          var followNode = observationNodeCombination.get(observationIndex++);
          agent.addTransition(node, action, observation, Distribution.createSingleEntryDistribution(followNode));
        }
      }
    }
    LOG.info("Added {} nodes to Agent {}.", nodesAdded, agent);
  }
}
