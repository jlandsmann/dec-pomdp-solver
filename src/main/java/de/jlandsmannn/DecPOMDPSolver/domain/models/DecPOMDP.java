package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.BeliefState;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Vector;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class DecPOMDP {
    private final int agentCount;
    private final int stateCount;
    private final ArrayList<Agent> agents;
    private final Set<State> states;
    private final Map<State, Map<Vector<Action>, BeliefState>> transitionFunction;
    private final Map<State, Map<Vector<Action>, Double>> rewardFunction;
    private final Map<Vector<Action>, Map<State, Vector<Distribution<Observation>>>> observationFunction;

    public DecPOMDP(ArrayList<Agent> agents, Set<State> states, Map<State, Map<Vector<Action>, BeliefState>> transitionFunction, Map<State, Map<Vector<Action>, Double>> rewardFunction, Map<Vector<Action>, Map<State, Vector<Distribution<Observation>>>> observationFunction) {
        this.agentCount = agents.size();
        this.agents = agents;
        this.stateCount = states.size();
        this.states = states;
        this.transitionFunction = transitionFunction;
        this.rewardFunction = rewardFunction;
        this.observationFunction = observationFunction;

        validateTransitionFunction();
        validateRewardFunction();
        validateObservationFunction();
    }

    public BeliefState transition(BeliefState currentState) {
        var agentActions = getActionsFromAgents(currentState);
        var nextBeliefState = getNextBeliefState(currentState, agentActions);
        var reward = getReward(currentState, agentActions);
        var observations = getObservations(agentActions, nextBeliefState);
        observe(agentActions, observations, reward);
        return nextBeliefState;
    }

    protected Vector<Action> getActionsFromAgents(BeliefState currentState) {
        var agentsStream = agents.stream();
        var agentsActionsStream = agentsStream.map(a -> a.chooseAction(currentState));
        return new Vector<>(agentsActionsStream.toList());
    }

    protected BeliefState getNextBeliefState(BeliefState currentState, Vector<Action> agentActions) {
        return transitionFunction.get(currentState.getMax()).get(agentActions);
    }

    protected Double getReward(BeliefState currentState, Vector<Action> agentActions) {
        return rewardFunction.get(currentState.getMax()).get(agentActions);
    }

    protected Vector<Distribution<Observation>> getObservations(Vector<Action> agentActions, BeliefState nextState) {
        return observationFunction.get(agentActions).get(nextState.getMax());
    }

    protected void observe(Vector<Action> actions, Vector<Distribution<Observation>> observations, Double reward) {
        for (int i = 0; i < this.agentCount; i++) {
            var agent = this.agents.get(i);
            agent.observe(actions.get(i), observations.get(i).getMax(), reward);
        }
    }

    private void validateTransitionFunction() {
        if (transitionFunction.size() != stateCount) {
            throw new IllegalArgumentException("Transition function does not match state count");
        }
        for (var state : transitionFunction.keySet()) {
            var innerMap = transitionFunction.get(state);
            for (var actionVector : innerMap.keySet()) {
                if (actionVector.getSize() != agentCount) {
                    throw new IllegalArgumentException("Some action vector of transition function does not match agent count.");
                }
            }
        }
    }

    private void validateRewardFunction() {
        for (var state : rewardFunction.keySet()) {
            var innerMap = rewardFunction.get(state);
            for (var actionVector : innerMap.keySet()) {
                if (actionVector.getSize() != agentCount) {
                    throw new IllegalArgumentException("Some action vector of reward function does not match agent count.");
                }
            }
        }
    }

    private void validateObservationFunction() {
        for (var actionVector : observationFunction.keySet()) {
            if (actionVector.getSize() != agentCount) {
                throw new IllegalArgumentException("Some action vector of observation function does not match agent count.");
            } else if (observationFunction.get(actionVector).size() != stateCount) {
                throw new IllegalArgumentException("For some action vector of observation function not every state is matched." + "Action vector: " + actionVector);
            }
            var innerMap = observationFunction.get(actionVector);
            for (var state : innerMap.keySet()) {
                if (innerMap.get(state).getSize() != agentCount) {
                    throw new IllegalArgumentException("For some action vector of observation function observations does not match agent count.");
                }
            }
        }
    }
}
