package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.BeliefState;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Vector;

import java.util.*;

public class DecPOMDPBuilder {
    private final LinkedHashSet<Agent> agents = new LinkedHashSet<>();
    private final LinkedHashSet<State> states = new LinkedHashSet<>();
    private final Map<State, Map<Vector<Action>, BeliefState>> transitionFunction = new HashMap<>();
    private final Map<State, Map<Vector<Action>, Double>> rewardFunction = new HashMap<>();
    private final Map<Vector<Action>, Map<State, Vector<Distribution<Observation>>>> observationFunction = new HashMap<>();

    public DecPOMDPBuilder addAgent(Agent agent) {
        this.agents.add(agent);
        return this;
    }

    public DecPOMDPBuilder addState(State state) {
        this.states.add(state);
        return this;
    }

    public DecPOMDPBuilder addTransition(State state, Vector<Action> actions, BeliefState beliefState) {
        this.transitionFunction.putIfAbsent(state, new HashMap<>());
        this.transitionFunction.get(state).put(actions, beliefState);
        return this;
    }

    public DecPOMDPBuilder addReward(State state, Vector<Action> actions, Double reward) {
        this.rewardFunction.putIfAbsent(state, new HashMap<>());
        this.rewardFunction.get(state).put(actions, reward);
        return this;
    }

    public DecPOMDPBuilder addObservation(Vector<Action> actions, State targetState, Vector<Distribution<Observation>> observations) {
        this.observationFunction.putIfAbsent(actions, new HashMap<>());
        this.observationFunction.get(actions).put(targetState, observations);
        return this;
    }

    public DecPOMDP createDecPOMDP() {
        var agentCount = agents.size();
        Agent[] agentArray = agents.toArray(new Agent[agentCount]);
        return new DecPOMDP(agentArray, states, transitionFunction, rewardFunction, observationFunction);
    }
}