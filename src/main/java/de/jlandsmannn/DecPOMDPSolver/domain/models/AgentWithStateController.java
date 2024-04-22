package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.BeliefState;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;

import java.util.Set;

public class AgentWithStateController extends Agent {

    protected final FiniteStateController controller;

    public AgentWithStateController(String name, Set<Action> actions, Set<Observation> observations, FiniteStateController controller) {
        super(name, actions, observations);
        this.controller = controller;
    }

    public Distribution<Action> getNextAction() {
        return controller.getNextAction();
    }

    @Override
    public Action chooseAction(BeliefState beliefState) {
        return getNextAction().getRandom();
    }

    @Override
    public void observe(Action action, Observation observation, Double reward) {
        controller.observe(action, observation);
    }
}
