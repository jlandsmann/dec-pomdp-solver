package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorStreamBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class BeliefPointGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(BeliefPointGenerator.class);

  private DecPOMDPWithStateController decPOMDP;
  private Distribution<State> initialBeliefState;
  private Map<Agent, Map<State, Distribution<Action>>> randomPolicies = new HashMap<>();
  private int numberOfBeliefPoints;

  public BeliefPointGenerator setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    LOG.debug("Retrieving DecPOMDP: {}", decPOMDP);
    this.decPOMDP = decPOMDP;
    return this;
  }

  public BeliefPointGenerator setDesiredNumberOfBeliefPoints(int numberOfBeliefPoints) {
    LOG.debug("Retrieving desired number of belief-points: {}", numberOfBeliefPoints);
    this.numberOfBeliefPoints = numberOfBeliefPoints;
    return this;
  }

  public BeliefPointGenerator setInitialBeliefState(Distribution<State> initialBeliefState) {
    LOG.debug("Retrieving initial belief state: {}", initialBeliefState);
    this.initialBeliefState = initialBeliefState;
    return this;
  }

  public BeliefPointGenerator generateRandomPolicies() {
    LOG.info("Generating random policies for {} agents", decPOMDP.getAgents().size());
    for (var agent : decPOMDP.getAgents()) {
      var policy = generateRandomPolicy(agent);
      randomPolicies.put(agent, policy);
    }
    return this;
  }

  private Map<State, Distribution<Action>> generateRandomPolicy(AgentWithStateController agent) {
    LOG.info("Generating random policies for {}", agent);
    Map<State, Distribution<Action>> policy = new HashMap<>();
    for (var state : decPOMDP.getStates()) {
      var distribution = Distribution.createRandomDistribution(agent.getActions());
      policy.put(state, distribution);
    }
    return policy;
  }

  public Set<Distribution<State>> generateBeliefPointsForAgent(AgentWithStateController agent) {
    LOG.info("Generating {} belief points for {} starting from {}.", numberOfBeliefPoints, agent, initialBeliefState);
    var currentBeliefPoint = initialBeliefState;
    var collection = new HashSet<Distribution<State>>();
    collection.add(currentBeliefPoint);
    for (int i = 1; i < numberOfBeliefPoints; i++) {
      var generatedBeliefPoint = generateBeliefPoint(agent, currentBeliefPoint);
      collection.add(generatedBeliefPoint);
      currentBeliefPoint = generatedBeliefPoint;
    }
    return collection;
  }

  private Distribution<State> generateBeliefPoint(AgentWithStateController agent, Distribution<State> currentBeliefPoint) {
    LOG.info("Generating belief point for {} starting from {}.", agent, currentBeliefPoint);
    Map<State, Double> beliefStateMap = new HashMap<>();
    var sumOfProbabilities = 0D;
    for (var followState : decPOMDP.getStates()) {
      var probability = 0D;
      for (var action : agent.getActions()) {
        for (var observation : agent.getObservations()) {
          probability += getProbabilityForAgentTransition(agent, currentBeliefPoint, action, observation, followState);
        }
      }
      sumOfProbabilities += probability;
      beliefStateMap.put(followState, probability);
    }

    for (var state : beliefStateMap.keySet()) {
      beliefStateMap.put(state, beliefStateMap.get(state) / sumOfProbabilities);
    }
    var newBeliefPoint = Distribution.of(beliefStateMap);
    LOG.debug("New belief point generated: {}", newBeliefPoint);
    return newBeliefPoint;
  }

  private Double getProbabilityForAgentTransition(AgentWithStateController agent, Distribution<State> beliefState, Action action, Observation observation, State followState) {
    LOG.debug("Get Probability For Agent Transition for Agent {} with Action {} and Observation {} starting from BeliefState {} targeting State {}", agent, action, observation, beliefState, followState);
    var rawActionCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (agent.equals(a)) return Set.of(action);
      else return a.getActions();
    }).toList();
    var actionCombinations = VectorStreamBuilder.forEachCombination(rawActionCombinations).toList();
    var rawObservationCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (agent.equals(a)) return Set.of(observation);
      else return a.getObservations();
    }).toList();
    var observationCombinations = VectorStreamBuilder.forEachCombination(rawObservationCombinations).toList();

    var probability = 0D;
    for (var state : beliefState.keySet()) {
      var stateProbability = beliefState.getProbability(state);

      for (var actionVector : actionCombinations) {
        var actionVectorProbability = getProbabilityForActionVector(state, actionVector, agent);
        var transitionProbability = decPOMDP.getTransition(state, actionVector).getProbability(followState);

        for (var observationVector : observationCombinations) {
          var observationVectorProbability = decPOMDP.getObservations(actionVector, followState).getProbability(observationVector);
          probability += stateProbability * actionVectorProbability *  transitionProbability * observationVectorProbability;
        }
      }
    }
    return probability;
  }

  private double getProbabilityForActionVector(State state, Vector<Action> actionVector, Agent agentToIgnore) {
    var probability = 1D;
    for (int i = 0; i < actionVector.size(); i++) {
      var agent = decPOMDP.getAgents().get(i);
      if (agent.equals(agentToIgnore)) continue;
      var action = actionVector.get(i);
      var policy = randomPolicies.get(agent);
      var actionDistribution = policy.get(state);
      var actionProbability = actionDistribution.getProbability(action);
      probability *= actionProbability;
    }
    return probability;
  }
}
