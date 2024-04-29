package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

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

    public abstract Action chooseAction(Distribution<State> beliefState);

    public abstract void observe(Action action, Observation observation, Double reward);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Agent) {
            return name.equals(((Agent) obj).name) && actions.equals(((Agent) obj).actions) && observations.equals(((Agent) obj).observations);
        }
        return super.equals(obj);
    }
}
