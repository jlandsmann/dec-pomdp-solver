package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.*;
import java.util.stream.Collectors;

public abstract class DecPOMDP<AGENT extends Agent> {
    protected final int agentCount;
    protected final int stateCount;
    protected final List<AGENT> agents;
    protected final Set<State> states;
    protected final double discountFactor;

    public DecPOMDP(List<AGENT> agents, Set<State> states, double discountFactor) {
        this.agentCount = agents.size();
        this.agents = agents;
        this.stateCount = states.size();
        this.states = states;
        this.discountFactor = discountFactor;
        validateDiscountFactor(discountFactor);
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

    public double getReward(Distribution<State> currentState, Vector<Action> agentActions) {
        var reward = 0D;
        for (State state : currentState.keySet()) {
            var probability = currentState.getProbability(state);
            var rewardForState = getReward(state, agentActions);
            reward += probability * rewardForState;
        }
        return reward;
    }

    public abstract double getReward(State currentState, Vector<Action> agentActions);

    public Distribution<Vector<Observation>> getObservations(Vector<Action> agentActions, Distribution<State> nextBeliefState) {
        var map = nextBeliefState.entrySet().stream()
                .map(entry -> {
                    var state = entry.getKey();
                    var probability = entry.getValue();
                    var distribution = getObservations(agentActions, state);
                    return Map.entry(distribution, probability);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return Distribution.createWeightedDistribution(map);
    }

    public abstract Distribution<Vector<Observation>> getObservations(Vector<Action> agentActions, State nextState);

    public abstract double getValue(Distribution<State> beliefState);

    public Vector<Distribution<Action>> getActionsFromAgents(Distribution<State> currentState) {
        var agentsStream = agents.stream();
        var agentsActionsStream = agentsStream.map(a -> a.chooseAction(currentState));
        return new Vector<>(agentsActionsStream.toList());
    }

    private void validateDiscountFactor(double discountFactor) {
        if (discountFactor <= 0 || 1 <= discountFactor) {
            throw new IllegalArgumentException("discountFactor must be a positive number between 0 and 1");
        }
    }
}
