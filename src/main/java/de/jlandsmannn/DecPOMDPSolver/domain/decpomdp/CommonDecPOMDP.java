package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonDecPOMDP extends DecPOMDP {
    private final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction;
    private final Map<State, Map<Vector<Action>, Double>> rewardFunction;
    private final Map<Vector<Action>, Map<State, Vector<Distribution<Observation>>>> observationFunction;

    CommonDecPOMDP(List<Agent> agents, Set<State> states, Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction, Map<State, Map<Vector<Action>, Double>> rewardFunction, Map<Vector<Action>, Map<State, Vector<Distribution<Observation>>>> observationFunction) {
        super(agents, states);
        this.transitionFunction = transitionFunction;
        this.rewardFunction = rewardFunction;
        this.observationFunction = observationFunction;

        validateTransitionFunction();
        validateRewardFunction();
        validateObservationFunction();
    }

    @Override
    public Distribution<State> getTransition(State currentState, Vector<Action> agentActions) {
        return transitionFunction.get(currentState).get(agentActions);
    }

    @Override
    public Double getReward(State currentState, Vector<Action> agentActions) {
        return rewardFunction.get(currentState).get(agentActions);
    }

    @Override
    public Vector<Distribution<Observation>> getObservations(Vector<Action> agentActions, State nextState) {
        return observationFunction.get(agentActions).get(nextState);
    }

    private void validateTransitionFunction() {
        if (transitionFunction.size() != stateCount) {
            throw new IllegalArgumentException("Transition function does not match state count");
        }
        for (var state : transitionFunction.keySet()) {
            var innerMap = transitionFunction.get(state);
            for (var actionVector : innerMap.keySet()) {
                if (actionVector.size() != agentCount) {
                    throw new IllegalArgumentException("Some action vector of transition function does not match agent count.");
                }
            }
        }
    }

    private void validateRewardFunction() {
        for (var state : rewardFunction.keySet()) {
            var innerMap = rewardFunction.get(state);
            for (var actionVector : innerMap.keySet()) {
                if (actionVector.size() != agentCount) {
                    throw new IllegalArgumentException("Some action vector of reward function does not match agent count.");
                }
            }
        }
    }

    private void validateObservationFunction() {
        for (var actionVector : observationFunction.keySet()) {
            if (actionVector.size() != agentCount) {
                throw new IllegalArgumentException("Some action vector of observation function does not match agent count.");
            } else if (observationFunction.get(actionVector).size() != stateCount) {
                throw new IllegalArgumentException("For some action vector of observation function not every state is matched." + "Action vector: " + actionVector);
            }
            var innerMap = observationFunction.get(actionVector);
            for (var state : innerMap.keySet()) {
                if (innerMap.get(state).size() != agentCount) {
                    throw new IllegalArgumentException("For some action vector of observation function observations does not match agent count.");
                }
            }
        }
    }
}
