package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;

import java.util.Map;
import java.util.Set;

public class FiniteStateController {
    protected Node currentNode;
    protected final Set<Node> nodes;
    protected final Map<Node, Distribution<Action>> actionFunction;
    protected final Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction;

    public FiniteStateController(Node initialNode, Set<Node> nodes, Map<Node, Distribution<Action>> actionFunction, Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction) {
        this.currentNode = initialNode;
        this.nodes = nodes;
        this.actionFunction = actionFunction;
        this.transitionFunction = transitionFunction;
    }

    public Distribution<Action> getNextAction() {
        return actionFunction.get(currentNode);
    }

    public void observe(Action action, Observation observation) {
        currentNode = getNextNode(action, observation).getRandom();
    }

    public Distribution<Node> getNextNode(Action action, Observation observation) {
        return transitionFunction.get(currentNode).get(action).get(observation);
    }
}
