package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.BeliefState;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Vector;

import java.util.Arrays;
import java.util.Map;

public class DecPOMDP {
    private final int agentCount = 0;
    private final Agent[] agents = new Agent[agentCount];
    private final Map<State, Map<Vector<Action>, BeliefState>> transitionFunction = Map.of();
    private final Map<State, Map<Vector<Action>, Double>> rewardFunction = Map.of();
    private final Map<Vector<Action>, Map<State, Vector<Distribution<Observation>>>> observationFunction = Map.of();

    public BeliefState transition(BeliefState currentState) {
        var agentActions = getActionsFromAgents(currentState);
        var nextBeliefState = getNextBeliefState(currentState, agentActions);
        var reward = getReward(currentState, agentActions);
        var observations = getObservations(agentActions, nextBeliefState);
        observe(observations, reward);
        return nextBeliefState;
    }

    protected Vector<Action> getActionsFromAgents(BeliefState currentState) {
        var agentsStream = Arrays.stream(agents);
        var agentsActionsStream = agentsStream.map(a -> a.chooseAction(currentState));
        return new Vector<>((Action[]) agentsActionsStream.toArray());
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

    protected void observe(Vector<Distribution<Observation>> observations, Double reward) {
        for (int i = 0; i < this.agents.length; i++) {
            var agent = this.agents[i];
            agent.observe(observations.get(i).getMax(), reward);
        }
    }
}
