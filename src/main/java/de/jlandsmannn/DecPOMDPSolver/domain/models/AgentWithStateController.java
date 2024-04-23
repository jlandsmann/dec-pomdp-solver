package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.BeliefState;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AgentWithStateController extends Agent {

    protected final FiniteStateController controller;

    public AgentWithStateController(String name, Set<Action> actions, Set<Observation> observations, FiniteStateController controller) {
        super(name, actions, observations);
        this.controller = controller;
    }

    public static AgentWithStateController createArbitraryAgent(String name, int numberOfActions, int numberOfObservations) {
        Set<Action> actions = IntStream.range(1, numberOfActions).mapToObj(i -> new Action(name + "-A" + i)).collect(Collectors.toSet());
        Set<Observation> observations = IntStream.range(1, numberOfObservations).mapToObj(i -> new Observation(name + "-O" + i)).collect(Collectors.toSet());
        var controller = FiniteStateControllerBuilder.createArbitraryController(actions, observations);
        return new AgentWithStateController(name, actions, observations, controller);
    }

    public Distribution<Action> getNextAction() {
        return controller.getNextAction();
    }

    public Distribution<Node> getNextNode(Action action, Observation observation) {
        return controller.getNextNode(action, observation);
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
