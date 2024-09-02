package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.VectorCombinationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * This class generates belief points for a DecPOMDP.
 * A belief point is a belief state used to prevent overfitting.
 * It is generated by starting from a belief state
 * (often the initial belief state or last belief point),
 * selecting an action based on the given (random) policy,
 * and simulating the transition, while receiving a new belief state.
 * That belief state is the new belief point.
 */
@Service
public class BeliefPointGenerator {
  private static final Logger LOG = LoggerFactory.getLogger(BeliefPointGenerator.class);

  private final Random random;
  private final int maxGenerationRuns;
  private final double beliefPointDistanceThreshold;
  private IDecPOMDP<?> decPOMDP;
  private Distribution<State> currentBeliefState;
  private Map<IAgent, Map<State, Distribution<Action>>> policies;
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

  public BeliefPointGenerator setDecPOMDP(IDecPOMDP<?> decPOMDP) {
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

  public BeliefPointGenerator setPolicies(Map<IAgent, Map<State, Distribution<Action>>> policies) {
    if (policies == null) return setPolicies();
    this.policies = policies;
    return this;
  }

  public BeliefPointGenerator setPolicies() {
    policies = generateUniformPolicies();
    return this;
  }

  public Map<IAgent, Set<Distribution<State>>> generateBeliefPoints() {
    assertAllDependenciesAreSet();
    var beliefPoints = new ConcurrentHashMap<IAgent, Set<Distribution<State>>>(decPOMDP.getAgents().size());
    var agent = decPOMDP.getAgents().stream().parallel().findAny().orElseThrow();
    var newBeliefPoints = generateBeliefPointsForAgent(agent);
    decPOMDP.getAgents().forEach(a -> beliefPoints.put(a, newBeliefPoints));
    return beliefPoints;
  }

  public Set<Distribution<State>> generateBeliefPointsForAgent(IAgent agent) {
    assertAllDependenciesAreSet();
    var generatedBeliefPoints = new HashSet<Distribution<State>>();
    var beliefPoint = currentBeliefState;
    generatedBeliefPoints.add(currentBeliefState);

    for (int generation = 0; generation < maxGenerationRuns; generation++) {
      if (generatedBeliefPoints.size() >= numberOfBeliefPoints) break;
      if (generation > 0) randomizePoliciesAndBeliefState();
      for (int i = 0; i < numberOfBeliefPoints; i++) {
        if (generatedBeliefPoints.size() >= numberOfBeliefPoints) break;
        var newBeliefPoint = getFollowUpBeliefStateForAgent(agent, beliefPoint);
        addPointOnlyIfDiverse(generatedBeliefPoints, newBeliefPoint);
        beliefPoint = newBeliefPoint;
      }
    }
    LOG.info("Generated {} belief points for {}.", generatedBeliefPoints.size(), agent);
    return generatedBeliefPoints;
  }

  protected void addPointOnlyIfDiverse(Set<Distribution<State>> alreadyFound, Distribution<State> pointToAdd) {
    var closeBeliefStateExists = alreadyFound.stream().anyMatch(pointAdded -> pointAdded.closeTo(pointToAdd, beliefPointDistanceThreshold));
    if (!closeBeliefStateExists) alreadyFound.add(pointToAdd);
  }

  protected void assertAllDependenciesAreSet() {
    if (decPOMDP == null || policies == null || numberOfBeliefPoints == 0) {
      throw new IllegalStateException("DecPOMDP, initialBeliefState, policies and numberOfBeliefPoints must be set, to generate belief points for agents.");
    }
  }

  protected void randomizePoliciesAndBeliefState() {
    LOG.info("Generating further belief points to increase diversity.");
    policies = generateRandomPolicies();
  }

  protected Map<IAgent, Map<State, Distribution<Action>>> generateRandomPolicies() {
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

  private Map<State, Distribution<Action>> generateRandomPolicy(IAgent agent) {
    LOG.info("Generating random policies for {}", agent);
    return decPOMDP.getStates()
      .stream()
      .map(state -> {
        var distribution = Distribution.createRandomDistribution(agent.getActions(), random);
        return Map.entry(state, distribution);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  protected Map<IAgent, Map<State, Distribution<Action>>> generateUniformPolicies() {
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

  private Map<State, Distribution<Action>> generateUniformPolicy(IAgent agent) {
    LOG.info("Generating uniform policies for {}", agent);
    return decPOMDP.getStates()
      .stream()
      .map(state -> {
        var distribution = Distribution.createUniformDistribution(agent.getActions());
        return Map.entry(state, distribution);
      })
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  protected Distribution<State> getFollowUpBeliefStateForAgent(IAgent agent, Distribution<State> beliefState) {
    LOG.debug("Calculating follow-up belief state for {} starting from {}.", agent, beliefState);
    Map<State, Double> beliefStateMap = new HashMap<>();
    AtomicReference<Double> sumOfProbabilities = new AtomicReference<>(0D);

    decPOMDP.getStates().stream().parallel().forEach(followState -> {
      var probability = 0D;
      for (var action : agent.getActions()) {
        for (var observation : agent.getObservations()) {
          probability += getProbabilityForAgentTransition(agent, beliefState, action, observation, followState);
        }
      }
      final var probability2 = probability;
      sumOfProbabilities.updateAndGet(v -> v + probability2);
      beliefStateMap.put(followState, probability);
    });
    beliefStateMap.replaceAll((s, v) -> v / sumOfProbabilities.get());
    return Distribution.of(beliefStateMap);
  }

  private double getProbabilityForAgentTransition(IAgent agent, Distribution<State> beliefState, Action action, Observation observation, State followState) {
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

  private double getProbabilityForActionVector(State state, Vector<Action> actionVector, IAgent agentToIgnore) {
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
    return decPOMDP.getTransitionProbability(state, actionVector, followState);
  }

  private double getObservationProbability(Vector<Action> actionVector, State followState, Vector<Observation> observationVector) {
    return decPOMDP.getObservationProbability(actionVector, followState, observationVector);
  }

  private List<Vector<Action>> getAllActionCombinationsWithFixedActionForAgent(Action action, IAgent agent) {
    var rawCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (agent.equals(a)) return List.of(action);
      else return a.getActions();
    }).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }

  private List<Vector<Observation>> getAllObservationCombinationsWithFixedObservationForAgent(Observation observation, IAgent agent) {
    var rawCombinations = decPOMDP.getAgents().stream().map(a -> {
      if (agent.equals(a)) return List.of(observation);
      else return a.getObservations();
    }).toList();
    return VectorCombinationBuilder.listOf(rawCombinations);
  }
}
