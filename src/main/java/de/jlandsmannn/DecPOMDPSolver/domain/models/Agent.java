package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.BeliefState;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;

import java.util.Set;

public abstract class Agent {
    private final Set<Action> actions = Set.of();
    private final Set<Observation> observations = Set.of();

    public abstract Action chooseAction(BeliefState beliefState);
    public abstract void observe(Observation observation, Double reward);
}
