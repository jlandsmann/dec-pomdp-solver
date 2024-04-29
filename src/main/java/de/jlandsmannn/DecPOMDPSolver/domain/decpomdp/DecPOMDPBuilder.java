package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DecPOMDPBuilder {
    private final Logger logger = LoggerFactory.getLogger("DecPOMDPBuilder");
    private final List<Agent> agents = new ArrayList<>();
    private final Set<State> states = new HashSet<>();
    private final Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction = new HashMap<>();
    private final Map<State, Map<Vector<Action>, Double>> rewardFunction = new HashMap<>();
    private final Map<Vector<Action>, Map<State, Vector<Distribution<Observation>>>> observationFunction = new HashMap<>();

    public DecPOMDPBuilder addAgent(Agent agent) {
        this.agents.remove(agent);
        this.agents.add(agent);
        return this;
    }

    public DecPOMDPBuilder addState(String stateString) {
        var state = new State(stateString);
        this.states.add(state);
        return this;
    }

    public DecPOMDPBuilder addTransition(String stateString, Vector<Action> actions, String targetState) {
        var beliefState = Distribution.createSingleEntryDistribution(new State(targetState));
        return addTransition(stateString, actions, beliefState);
    }

    public DecPOMDPBuilder addTransition(String stateString, Vector<Action> actions, Distribution<State> beliefState) {
        var state = new State(stateString);
        this.transitionFunction.putIfAbsent(state, new HashMap<>());
        this.transitionFunction.get(state).put(actions, beliefState);
        return this;
    }

    public DecPOMDPBuilder addReward(String stateString, Vector<Action> actions, Double reward) {
        var state = new State(stateString);
        this.rewardFunction.putIfAbsent(state, new HashMap<>());
        this.rewardFunction.get(state).put(actions, reward);
        return this;
    }

    public DecPOMDPBuilder addObservation(Vector<Action> actions, String targetStateString, Vector<Distribution<Observation>> observations) {
        State targeState = new State(targetStateString);
        this.observationFunction.putIfAbsent(actions, new HashMap<>());
        this.observationFunction.get(actions).put(targeState, observations);
        return this;
    }

    public DecPOMDP createDecPOMDP() {
        logger.info(
                "Creating CommonDecPOMDP with " +
                agents.size() + " agents, " +
                states.size() + " states, " +
                transitionFunction.size() + " transitions, " +
                rewardFunction.size() + " rewards and " +
                observationFunction.size() + " observations."
        );
        return new CommonDecPOMDP(agents, states, transitionFunction, rewardFunction, observationFunction);
    }
}