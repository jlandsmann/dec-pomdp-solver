package de.jlandsmannn.DecPOMDPSolver.policyIteration;

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

  public void performExhaustiveBackup() {
    LOG.info("Performing global exhaustive backup");
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to perform exhaustive backup.");
    for (var agent : decPOMDP.getAgents()) {
      performExhaustiveBackupForAgent(agent);
    }
    updateValueFunction();
  }

  protected void performExhaustiveBackupForAgent(AgentWithStateController agent) {
    LOG.info("Performing local exhaustive backup for Agent {}", agent);
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to perform exhaustive backup.");
    long nodesAdded = 0;
    var originalNodes = List.copyOf(agent.getControllerNodes());
    LOG.info("Starting with {} nodes for Agent {}", originalNodes.size(), agent);
    for (var action : agent.getActions()) {
      var rawObservationNodeCombinations = agent.getObservations().stream().map(o -> originalNodes).toList();
      var observationNodeCombinations = VectorStreamBuilder.forEachCombination(rawObservationNodeCombinations).toList();
      for (var observationNodeCombination : observationNodeCombinations) {
        var node = Node.from(agent.getName() + "-Q" + agent.getControllerNodeIndex());
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

  protected void updateValueFunction() {
    LOG.info("Calculating missing values of value function");
    var rawNodeCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getControllerNodes).toList();
    var nodeCombinations = VectorStreamBuilder.forEachCombination(rawNodeCombinations).toList();
    var updatedCombinations = 0;
    for (var state : decPOMDP.getStates()) {
      for (var nodeCombination : nodeCombinations) {
        if (decPOMDP.getOptionalValue(state, nodeCombination).isPresent()) continue;
        var value = calculateValue(state, nodeCombination);
        decPOMDP.setValue(state, nodeCombination, value);
        updatedCombinations++;
      }
    }
    LOG.info("Calculated {} missing values of value function", updatedCombinations);
  }

  protected double calculateValue(State state, Vector<Node> nodeVector) {
    var rawNodeCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getControllerNodes).toList();
    var nodeCombinations = VectorStreamBuilder.forEachCombination(rawNodeCombinations).toList();
    var rawActionCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getActions).toList();
    var actionCombinations = VectorStreamBuilder.forEachCombination(rawActionCombinations).toList();
    var rawObservationsCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getObservations).toList();
    var observationsCombinations = VectorStreamBuilder.forEachCombination(rawObservationsCombinations).toList();

    var value = 0D;
    for (var actionVector : actionCombinations) {
      var actionVectorProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
      var reward = decPOMDP.getReward(state, actionVector);
      value += actionVectorProbability * reward;

      for (var observationVector : observationsCombinations) {
        for (var newNodeVector : nodeCombinations) {
          for (var newState : decPOMDP.getStates()) {
            var discount = decPOMDP.getDiscountFactor();
            var stateTransitionProbability = decPOMDP.getStateTransitionProbability(state, actionVector, observationVector, newState);
            var nodeTransitionProbability = decPOMDP.getNodeTransitionProbability(nodeVector, actionVector, observationVector, newNodeVector);
            var followValue = decPOMDP.getValue(newState, newNodeVector);
            value += discount * actionVectorProbability * stateTransitionProbability * nodeTransitionProbability * followValue;
          }
        }
      }
    }
    return value;
  }
}
