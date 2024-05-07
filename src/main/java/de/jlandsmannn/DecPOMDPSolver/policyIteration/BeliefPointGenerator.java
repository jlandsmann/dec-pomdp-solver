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

import java.util.*;

@Service
public class BeliefPointGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(BeliefPointGenerator.class);
  private static final int MAX_GENERATION_RUNS = 100;

  private DecPOMDPWithStateController decPOMDP;
  private Distribution<State> initialBeliefState;
  private Distribution<State> currentBeliefState;
  private Map<Agent, Map<State, Distribution<Action>>> policies;
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
    this.initialBeliefState = this.currentBeliefState = initialBeliefState;
    return this;
  }

  public BeliefPointGenerator setPolicies(Map<Agent, Map<State, Distribution<Action>>> policies) {
    if (policies == null) policies = generateRandomPolicies();
    this.policies = policies;
    return this;
  }

  public Set<Distribution<State>> generateBeliefPointsForAgent(AgentWithStateController agent) {
    assertAllDependenciesAreSet();

    var generatedBeliefPoints = new HashSet<Distribution<State>>();
    var enoughBeliefPointsGenerated = false;

    for (int i = 0; i < MAX_GENERATION_RUNS && !enoughBeliefPointsGenerated; i++) {
      if (i > 0) LOG.info("Generating further belief points for {} to increase diversity.", agent);
      var newBeliefPoints = doGenerateBeliefPointsForAgent(agent);
      generatedBeliefPoints.addAll(newBeliefPoints);
      enoughBeliefPointsGenerated = generatedBeliefPoints.size() >= numberOfBeliefPoints;
      randomizePoliciesAndBeliefStateToIncreaseDiversity();
    }

    LOG.info("Generated {} belief points for {}.", generatedBeliefPoints.size(), agent);
    return generatedBeliefPoints;
  }

  protected Set<Distribution<State>> doGenerateBeliefPointsForAgent(AgentWithStateController agent) {
    LOG.debug("Generating {} belief points for {} starting from {}.", numberOfBeliefPoints, agent, currentBeliefState);

    Set<Distribution<State>> generatedBeliefPoints = new HashSet<>();
    generatedBeliefPoints.add(currentBeliefState);

    var hasBeliefPointChanged = true;
    for (int i = 1; i < numberOfBeliefPoints && hasBeliefPointChanged; i++) {
      var generatedBeliefPoint = getFollowUpBeliefStateForAgent(agent, currentBeliefState);
      generatedBeliefPoints.add(generatedBeliefPoint);
      hasBeliefPointChanged = !generatedBeliefPoints.equals(currentBeliefState);
      currentBeliefState = generatedBeliefPoint;
    }
    if (!hasBeliefPointChanged) LOG.debug("Stop generating because belief point did not change.");
    return generatedBeliefPoints;
  }

  protected void assertAllDependenciesAreSet() {
    if (decPOMDP == null || initialBeliefState == null || policies == null || numberOfBeliefPoints == 0) {
      throw new IllegalStateException("DecPOMDP, initialBeliefState, policies and numberOfBeliefPoints must be set, to generate belief points for agents.");
    }
  }

  protected void randomizePoliciesAndBeliefStateToIncreaseDiversity() {
    policies = generateRandomPolicies();
    currentBeliefState = Distribution.createRandomDistribution(decPOMDP.getStates());
  }

  protected Map<Agent, Map<State, Distribution<Action>>> generateRandomPolicies() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set, to generate random policies.");
    LOG.info("Generating random policies for {} agents", decPOMDP.getAgents().size());
    Map<Agent, Map<State, Distribution<Action>>> randomPolicies = new HashMap<>();
    for (var agent : decPOMDP.getAgents()) {
      var policy = generateRandomPolicy(agent);
      randomPolicies.put(agent, policy);
    }
    return randomPolicies;
  }

  protected Map<State, Distribution<Action>> generateRandomPolicy(AgentWithStateController agent) {
    LOG.info("Generating random policies for {}", agent);
    Map<State, Distribution<Action>> policy = new HashMap<>();
    for (var state : decPOMDP.getStates()) {
      var distribution = Distribution.createRandomDistribution(agent.getActions());
      policy.put(state, distribution);
    }
    return policy;
  }

  protected Distribution<State> getFollowUpBeliefStateForAgent(AgentWithStateController agent, Distribution<State> beliefState) {
    LOG.debug("Calculating follow-up belief state for {} starting from {}.", agent, beliefState);
    Map<State, Double> beliefStateMap = new HashMap<>();
    var sumOfProbabilities = 0D;

    for (var followState : decPOMDP.getStates()) {
      var probability = 0D;
      for (var action : agent.getActions()) {
        for (var observation : agent.getObservations()) {
          probability += getProbabilityForAgentTransition(agent, beliefState, action, observation, followState);
        }
      }
      sumOfProbabilities += probability;
      beliefStateMap.put(followState, probability);
    }

    for (var state : beliefStateMap.keySet()) {
      beliefStateMap.put(state, beliefStateMap.get(state) / sumOfProbabilities);
    }

    return Distribution.of(beliefStateMap);
  }

  protected Double getProbabilityForAgentTransition(AgentWithStateController agent, Distribution<State> beliefState, Action action, Observation observation, State followState) {
    LOG.debug("Get Probability For Agent Transition for Agent {} with Action {} and Observation {} starting from BeliefState {} targeting State {}", agent, action, observation, beliefState, followState);

    var actionCombinations = getAllActionCombinationsWithFixedActionForAgent(action, agent);
    var observationCombinations = getAllObservationCombinationsWithFixedObservationForAgent(observation, agent);

    var probability = 0D;
    for (var state : beliefState) {
      var stateProbability = beliefState.getProbability(state);

      for (var actionVector : actionCombinations) {
        var actionVectorProbability = getProbabilityForActionVector(state, actionVector, agent);
        var transitionProbability = getTransitionProbability(state, actionVector, followState);

        for (var observationVector : observationCombinations) {
          var observationVectorProbability = getObservationProbability(actionVector, followState, observationVector);
          probability += stateProbability * actionVectorProbability *  transitionProbability * observationVectorProbability;
        }
      }
    }
    return probability;
  }

  protected double getProbabilityForActionVector(State state, Vector<Action> actionVector, Agent agentToIgnore) {
    var probability = 1D;
    for (int i = 0; i < actionVector.size(); i++) {
      var agent = decPOMDP.getAgents().get(i);
      if (agent.equals(agentToIgnore)) continue;
      var action = actionVector.get(i);
      var policy = policies.get(agent);
      var actionDistribution = policy.get(state);
      var actionProbability = actionDistribution.getProbability(action);
      probability *= actionProbability;
    }
    return probability;
  }

  protected double getTransitionProbability(State state, Vector<Action> actionVector, State followState) {
    return decPOMDP.getTransition(state, actionVector).getProbability(followState);
  }

  protected double getObservationProbability(Vector<Action> actionVector, State followState, Vector<Observation> observationVector) {
    return decPOMDP.getObservations(actionVector, followState).getProbability(observationVector);
  }

  protected List<Vector<Action>> getAllActionCombinationsWithFixedActionForAgent(Action action, Agent agent) {
    var rawCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (agent.equals(a)) return Set.of(action);
      else return a.getActions();
    }).toList();
    return VectorStreamBuilder.forEachCombination(rawCombinations).toList();
  }

  protected List<Vector<Observation>> getAllObservationCombinationsWithFixedObservationForAgent(Observation observation, Agent agent) {
    var rawCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (agent.equals(a)) return Set.of(observation);
      else return a.getObservations();
    }).toList();
    return VectorStreamBuilder.forEachCombination(rawCombinations).toList();
  }
}
