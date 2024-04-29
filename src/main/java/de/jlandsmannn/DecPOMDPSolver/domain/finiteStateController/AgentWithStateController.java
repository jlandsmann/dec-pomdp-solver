package de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.Agent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

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
    public Action chooseAction(Distribution<State> beliefState) {
        return getNextAction().getRandom();
    }

    @Override
    public void observe(Action action, Observation observation, Double reward) {
        controller.observe(action, observation);
    }
}
