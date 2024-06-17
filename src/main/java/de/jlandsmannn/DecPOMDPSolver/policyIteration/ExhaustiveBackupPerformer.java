package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class performs exhaustive backups on an agent's state controller.
 * An exhaustive backup is useful for exploration of the policy space.
 * It adds all possible one-step policies for each already existing node
 * and calculates the values of those newly created nodes.
 */
@Service
public class ExhaustiveBackupPerformer {
  private static final Logger LOG = LoggerFactory.getLogger(ExhaustiveBackupPerformer.class);

  private DecPOMDPWithStateController decPOMDP;
  private List<Vector<Node>> originalNodeCombinations = List.of();
  private Set<Distribution<State>> beliefPoints;

  public ExhaustiveBackupPerformer setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    LOG.debug("Retrieving DecPOMDP: {}", decPOMDP);
    this.decPOMDP = decPOMDP;
    this.originalNodeCombinations = decPOMDP.getNodeCombinations();
    return this;
  }

  public ExhaustiveBackupPerformer setBeliefPoints(Set<Distribution<State>> beliefPoints) {
    LOG.debug("Retrieving belief points: {}", beliefPoints);
    validateBeliefPoints(beliefPoints);
    this.beliefPoints = beliefPoints;
    return this;
  }

  public void performExhaustiveBackup() {
    LOG.info("Performing global exhaustive backup");
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to perform exhaustive backup.");
    else if (beliefPoints == null)
      throw new IllegalStateException("Belief points must be set to perform exhaustive backup.");
    for (var agent : decPOMDP.getAgents()) {
      performExhaustiveBackupForAgent(agent);
    }
    updateValueFunction();
  }

  protected void performExhaustiveBackupForAgent(AgentWithStateController agent) {
    LOG.info("Performing local exhaustive backup for Agent {}", agent);
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set to perform exhaustive backup.");
    else if (beliefPoints == null)
      throw new IllegalStateException("Belief points must be set to perform exhaustive backup.");

    var originalNodes = List.copyOf(agent.getControllerNodes());
    var rawObservationNodeCombinations = agent.getObservations().stream().map(o -> originalNodes).toList();
    var observationNodeCombinations = VectorCombinationBuilder.listOf(rawObservationNodeCombinations);
    LOG.info("Starting with {} nodes for Agent {}", originalNodes.size(), agent);

    AtomicLong nodesAdded = new AtomicLong();
    agent.getActions().stream().parallel().forEach(action -> {
      observationNodeCombinations.stream().parallel().forEach(observationNodeCombination -> {

        var node = Node.from(agent.getName() + "-Q" + agent.getControllerNodeIndex());
        agent.addNode(node, action);
        nodesAdded.getAndIncrement();

        var observationIndex = 0;
        for (var observation : agent.getObservations()) {
          var followNode = observationNodeCombination.get(observationIndex++);
          agent.addTransition(node, action, observation, followNode);
        }
      });
    });

    LOG.info("Added {} nodes to Agent {}.", nodesAdded, agent);
  }

  protected void updateValueFunction() {
    LOG.info("Calculating missing values of value function");
    var nodeCombinations = decPOMDP.getNodeCombinations();
    var beliefPointStatesStream = beliefPoints.stream().map(Distribution::keySet).flatMap(Set::stream).distinct();
    AtomicInteger updatedCombinations = new AtomicInteger();
    beliefPointStatesStream
      .parallel()
      .forEach(state -> {
        nodeCombinations.stream()
          .parallel()
          .filter(nodeVector -> !decPOMDP.hasValue(state, nodeVector))
          .forEach(nodeVector -> {
            var value = calculateValue(state, nodeVector);
            decPOMDP.setValue(state, nodeVector, value);
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
    var actionCombinations = decPOMDP.getActionCombinations();
    var observationsCombinations = decPOMDP.getObservationCombinations();

    var value = 0D;
    var discount = decPOMDP.getDiscountFactor();
    for (var actionVector : actionCombinations) {
      var actionVectorProbability = decPOMDP.getActionVectorProbability(nodeVector, actionVector);
      if (actionVectorProbability == 0) continue;
      var reward = decPOMDP.getReward(state, actionVector);
      value += actionVectorProbability * reward;
      if (discount == 0) continue;

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

  private void validateBeliefPoints(Set<Distribution<State>> beliefPoints) {
    if (beliefPoints.isEmpty()) {
      throw new IllegalArgumentException("Belief points must not be empty.");
    }
    ;
  }
}
