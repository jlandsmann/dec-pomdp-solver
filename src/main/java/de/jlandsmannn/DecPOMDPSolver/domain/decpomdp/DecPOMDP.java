package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

public abstract class DecPOMDP {
    protected final int agentCount;
    protected final int stateCount;
    protected final List<Agent> agents;
    protected final Set<State> states;

    public DecPOMDP(List<Agent> agents, Set<State> states) {
        this.agentCount = agents.size();
        this.agents = agents;
        this.stateCount = states.size();
        this.states = states;
    }

    public Distribution<State> getTransition(Distribution<State> currentState, Vector<Action> agentActions) {
        Map<Distribution<State>, Double> probabilities = new HashMap<>();
        for (State state : currentState.keySet()) {
            var probability = currentState.getProbability(state);
            var nextState = getTransition(state, agentActions);
            probabilities.put(nextState, probability);
        }
        return Distribution.createWeightedDistribution(probabilities);
    }

    public abstract Distribution<State> getTransition(State currentState, Vector<Action> agentActions);

    public Double getReward(Distribution<State> currentState, Vector<Action> agentActions) {
        var reward = 0D;
        for (State state : currentState.keySet()) {
            var probability = currentState.getProbability(state);
            var rewardForState = getReward(state, agentActions);
            reward += probability * rewardForState;
        }
        return reward;
    }

    public abstract Double getReward(State currentState, Vector<Action> agentActions);

    public Vector<Distribution<Observation>> getObservations(Vector<Action> agentActions, Distribution<State> nextBeliefState) {
        ArrayList<Map<Distribution<Observation>, Double>> resultingObservations = new ArrayList<>();
        for (State state : nextBeliefState.keySet()) {
            var probability = nextBeliefState.getProbability(state);
            var observations = getObservations(agentActions, state);
            for (var i = 0; i < observations.size(); i++) {
                if (i == resultingObservations.size()) {
                    resultingObservations.add(new HashMap<>());
                }
                var map = resultingObservations.get(i);
                var observation = observations.get(i);
                var prevValue = map.getOrDefault(observation, 0D);
                map.put(observation, prevValue + probability);
            }
        }
        return new Vector<>(resultingObservations.stream().map(Distribution::createWeightedDistribution).toList());

    }

    public abstract Vector<Distribution<Observation>> getObservations(Vector<Action> agentActions, State nextState);

    public Distribution<State> transition(Distribution<State> currentState) {
        var agentActions = getActionsFromAgents(currentState);
        var nextBeliefState = getTransition(currentState, agentActions);
        var reward = getReward(currentState, agentActions);
        var observations = getObservations(agentActions, nextBeliefState);
        observe(agentActions, observations, reward);
        return nextBeliefState;
    }

    public Vector<Action> getActionsFromAgents(Distribution<State> currentState) {
        var agentsStream = agents.stream();
        var agentsActionsStream = agentsStream.map(a -> a.chooseAction(currentState));
        return new Vector<>(agentsActionsStream.toList());
    }

    public void observe(Vector<Action> actions, Vector<Distribution<Observation>> observations, Double reward) {
        for (int i = 0; i < this.agentCount; i++) {
            var agent = this.agents.get(i);
            agent.observe(actions.get(i), observations.get(i).getMax(), reward);
        }
    }
}
