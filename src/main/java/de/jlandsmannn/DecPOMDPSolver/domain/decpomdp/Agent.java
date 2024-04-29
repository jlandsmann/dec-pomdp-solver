package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class Agent {
    protected final String name;
    protected final Set<Action> actions;
    protected final Set<Observation> observations;

    protected Agent(String name, Set<Action> actions, Set<Observation> observations) {
        this.name = name;
        this.actions = actions;
        this.observations = observations;
    }

    public String getName() { return name; }
    public Set<Action> getActions() { return actions; }
    public Set<Observation> getObservations() { return observations; }

    public Distribution<Action> chooseAction(Distribution<State> beliefState) {
        Map<Distribution<Action>, Double> probabilities = new HashMap<>();
        for (State state : beliefState.keySet()) {
            var probability = beliefState.getProbability(state);
            var action = chooseAction(state);
            probabilities.put(action, probability);
        }
        return Distribution.createWeightedDistribution(probabilities);
    }

    public abstract Distribution<Action> chooseAction(State state);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Agent) {
            return name.equals(((Agent) obj).name) && actions.equals(((Agent) obj).actions) && observations.equals(((Agent) obj).observations);
        }
        return super.equals(obj);
    }
}
