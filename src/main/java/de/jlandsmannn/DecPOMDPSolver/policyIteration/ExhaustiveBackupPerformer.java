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
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ExhaustiveBackupPerformer {
  private static final Logger LOG = LoggerFactory.getLogger(ExhaustiveBackupPerformer.class);
  private DecPOMDPWithStateController decPOMDP;

  private List<Vector<Node>> originalNodeCombinations = List.of();

  public ExhaustiveBackupPerformer setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    LOG.debug("Retrieving DecPOMDP: {}", decPOMDP);
    this.decPOMDP = decPOMDP;
    return this;
  }

  public void performExhaustiveBackup() {
    LOG.info("Performing global exhaustive backup");
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to perform exhaustive backup.");
    var originalNodes = decPOMDP.getAgents().stream().map(AgentWithStateController::getControllerNodes).toList();
    originalNodeCombinations = VectorStreamBuilder.forEachCombination(originalNodes).toList();
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
    var rawObservationNodeCombinations = agent.getObservations().stream().map(o -> originalNodes).toList();
    var observationNodeCombinations = VectorStreamBuilder.forEachCombination(rawObservationNodeCombinations).toList();
    LOG.info("Starting with {} nodes for Agent {}", originalNodes.size(), agent);
    for (var action : agent.getActions()) {
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
    AtomicInteger updatedCombinations = new AtomicInteger();
    decPOMDP.getStates().stream()
      .parallel()
      .forEach(state -> {
        nodeCombinations.stream()
          .parallel()
          .filter(nodeVector -> decPOMDP.getOptionalValue(state, nodeVector).isEmpty())
          .forEach(nodeCombination -> {
            var value = calculateValue(state, nodeCombination);
            decPOMDP.setValue(state, nodeCombination, value);
            updatedCombinations.getAndIncrement();
            if (updatedCombinations.get() % 5000 == 0) {
              LOG.info("Calculated first {} missing values of value function", updatedCombinations.get());
            }
          });
      });
    LOG.info("Calculated all {} missing values of value function", updatedCombinations);
  }

  protected double calculateValue(State state, Vector<Node> nodeVector) {
    LOG.debug("Calculating missing value of value function for {} and {}", state, nodeVector);
    var rawActionCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getActions).toList();
    var actionCombinations = VectorStreamBuilder.forEachCombination(rawActionCombinations).toList();
    var rawObservationsCombinations = decPOMDP.getAgents().stream().map(AgentWithStateController::getObservations).toList();
    var observationsCombinations = VectorStreamBuilder.forEachCombination(rawObservationsCombinations).toList();

    var value = 0D;
    var discount = decPOMDP.getDiscountFactor();
    for (var actionVector : actionCombinations) {
      var actionVectorProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
      if (actionVectorProbability == 0) continue;
      var reward = decPOMDP.getReward(state, actionVector);
      value += actionVectorProbability * reward;

      for (var observationVector : observationsCombinations) {
        for (var followNodeVector : originalNodeCombinations) {
          var nodeTransitionProbability = decPOMDP.getNodeTransitionProbability(nodeVector, actionVector, observationVector, followNodeVector);
          if (nodeTransitionProbability == 0) continue;
          for (var followState : decPOMDP.getStates()) {
            var stateTransitionProbability = decPOMDP.getTransitionProbability(state, actionVector, observationVector, followState);
            if (stateTransitionProbability == 0) continue;
            var followValue = decPOMDP.getValue(followState, followNodeVector);
            value += discount * actionVectorProbability * stateTransitionProbability * nodeTransitionProbability * followValue;
          }
        }
      }
    }
    return value;
  }
}
