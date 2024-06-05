package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BeliefPointGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(BeliefPointGenerator.class);

  private final Random random;
  private final int maxGenerationRuns;
  private final double beliefPointDistanceThreshold;
  private DecPOMDPWithStateController decPOMDP;
  private Distribution<State> currentBeliefState;
  private Map<Agent, Map<State, Distribution<Action>>> policies;
  private int numberOfBeliefPoints;

  @Autowired
  BeliefPointGenerator(HeuristicPolicyIterationConfig config) {
    random = new Random();
    maxGenerationRuns = config.beliefPointGenerationMaxRuns();
    beliefPointDistanceThreshold = config.beliefPointDistanceThreshold();
    if (config.beliefPointGenerationSeed() != 0) {
      random.setSeed(config.beliefPointGenerationSeed());
    }
  }

  public BeliefPointGenerator setDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    LOG.debug("Retrieving DecPOMDP: {}", decPOMDP);
    this.decPOMDP = decPOMDP;
    this.currentBeliefState = decPOMDP.getInitialBeliefState();
    return this;
  }

  public BeliefPointGenerator setDesiredNumberOfBeliefPoints(int numberOfBeliefPoints) {
    LOG.debug("Retrieving desired number of belief-points: {}", numberOfBeliefPoints);
    this.numberOfBeliefPoints = numberOfBeliefPoints;
    return this;
  }

  public BeliefPointGenerator setPolicies(Map<Agent, Map<State, Distribution<Action>>> policies) {
    if (policies == null) return setPolicies();
    this.policies = policies;
    return this;
  }

  public BeliefPointGenerator setPolicies() {
    policies = generateUniformPolicies();
    return this;
  }

  public Set<Distribution<State>> generateBeliefPoints() {
    assertAllDependenciesAreSet();
    var generatedBeliefPoints = new HashSet<Distribution<State>>();
    generatedBeliefPoints.add(currentBeliefState);
    var enoughBeliefPointsGenerated = false;
    for (int i = 0; i < maxGenerationRuns && !enoughBeliefPointsGenerated; i++) {
      if (i > 0) randomizePoliciesAndBeliefState();
      var pointsNeeded = numberOfBeliefPoints - generatedBeliefPoints.size();
      var newBeliefPoints = doGenerateBeliefPoints(pointsNeeded);
      addOnlyDiversePoints(generatedBeliefPoints, newBeliefPoints);
      enoughBeliefPointsGenerated = generatedBeliefPoints.size() >= numberOfBeliefPoints;
    }
    LOG.info("Generated {} belief points.", generatedBeliefPoints.size());
    return generatedBeliefPoints;
  }

  protected Set<Distribution<State>> doGenerateBeliefPoints(int numberOfBeliefPoints) {
    var generatedBeliefPoints = new HashSet<Distribution<State>>();
    for (int i = 0; i < numberOfBeliefPoints; i++) {
      var agent = decPOMDP.getAgents().get(i % decPOMDP.getAgents().size());
      var newBeliefPoint = getFollowUpBeliefStateForAgent(agent, currentBeliefState);
      generatedBeliefPoints.add(newBeliefPoint);
      currentBeliefState = newBeliefPoint;
    }
    return generatedBeliefPoints;
  }

  protected void addOnlyDiversePoints(Set<Distribution<State>> alreadyFound, Set<Distribution<State>> pointsToAdd) {
    for (Distribution<State> pointToAdd : pointsToAdd) {
      var closeBeliefStateExists = alreadyFound.stream().anyMatch(pointAdded -> pointAdded.closeTo(pointToAdd, beliefPointDistanceThreshold));
      if (!closeBeliefStateExists) alreadyFound.add(pointToAdd);
    }
  }

  protected void assertAllDependenciesAreSet() {
    if (decPOMDP == null || policies == null || numberOfBeliefPoints == 0) {
      throw new IllegalStateException("DecPOMDP, initialBeliefState, policies and numberOfBeliefPoints must be set, to generate belief points for agents.");
    }
  }

  protected void randomizePoliciesAndBeliefState() {
    LOG.info("Generating further belief points to increase diversity.");
    policies = generateRandomPolicies();
    currentBeliefState = Distribution.createRandomDistribution(decPOMDP.getStates(), random);
  }

  protected Map<Agent, Map<State, Distribution<Action>>> generateRandomPolicies() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set, to generate random policies.");
    LOG.info("Generating random policies for {} agents", decPOMDP.getAgents().size());

    return decPOMDP.getAgents()
      .stream()
      .map(agent -> {
        var policy = generateRandomPolicy(agent);
        return Map.entry(agent, policy);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Map<State, Distribution<Action>> generateRandomPolicy(AgentWithStateController agent) {
    LOG.info("Generating random policies for {}", agent);
    return decPOMDP.getStates()
      .stream()
      .map(state -> {
        var distribution = Distribution.createRandomDistribution(agent.getActions(), random);
        return Map.entry(state, distribution);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  protected Map<Agent, Map<State, Distribution<Action>>> generateUniformPolicies() {
    if (decPOMDP == null) throw new IllegalStateException("DecPOMDP must be set, to generate uniform policies.");
    LOG.info("Generating uniform policies for {} agents", decPOMDP.getAgents().size());

    return decPOMDP.getAgents()
      .stream()
      .map(agent -> {
        var policy = generateUniformPolicy(agent);
        return Map.entry(agent, policy);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Map<State, Distribution<Action>> generateUniformPolicy(AgentWithStateController agent) {
    LOG.info("Generating uniform policies for {}", agent);
    return decPOMDP.getStates()
      .stream()
      .map(state -> {
        var distribution = Distribution.createUniformDistribution(agent.getActions());
        return Map.entry(state, distribution);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

  private Double getProbabilityForAgentTransition(AgentWithStateController agent, Distribution<State> beliefState, Action action, Observation observation, State followState) {
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
          probability += stateProbability * actionVectorProbability * transitionProbability * observationVectorProbability;
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
      var policy = policies.get(agent);
      var actionDistribution = policy.get(state);
      var actionProbability = actionDistribution.getProbability(action);
      probability *= actionProbability;
    }
    return probability;
  }

  private double getTransitionProbability(State state, Vector<Action> actionVector, State followState) {
    return decPOMDP.getTransition(state, actionVector).getProbability(followState);
  }

  private double getObservationProbability(Vector<Action> actionVector, State followState, Vector<Observation> observationVector) {
    return decPOMDP.getObservations(actionVector, followState).getProbability(observationVector);
  }

  private List<Vector<Action>> getAllActionCombinationsWithFixedActionForAgent(Action action, Agent agent) {
    var rawCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (agent.equals(a)) return List.of(action);
      else return a.getActions();
    }).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  private List<Vector<Observation>> getAllObservationCombinationsWithFixedObservationForAgent(Observation observation, Agent agent) {
    var rawCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (agent.equals(a)) return List.of(observation);
      else return a.getObservations();
    }).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }
}
