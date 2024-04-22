package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.BeliefState;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;

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

    public abstract Action chooseAction(BeliefState beliefState);

    public abstract void observe(Action action, Observation observation, Double reward);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Agent) {
            return name.equals(((Agent) obj).name);
        }
        return super.equals(obj);
    }
}
