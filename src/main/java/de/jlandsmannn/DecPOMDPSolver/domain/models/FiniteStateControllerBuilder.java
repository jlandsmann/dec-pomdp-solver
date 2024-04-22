package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.DistributionEmptyException;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.DistributionSumNotOneException;

import java.util.*;
import java.util.stream.Collectors;

public class FiniteStateControllerBuilder {
    private Node initialNode;
    private final Set<Node> nodes = new HashSet<>();
    private final Map<Node, Distribution<Action>> actionFunction = new HashMap<>();
    private final Map<Node, Map<Action, Map<Observation, Distribution<Node>>>> transitionFunction = new HashMap<>();

    public static FiniteStateController createArbitraryController(Set<Action> actions, Set<Observation> observations) {
        var builder = new FiniteStateControllerBuilder();
        Set<Node> nodes = actions.stream().map(a -> new Node(a.name())).collect(Collectors.toSet());
        for (var node : nodes) {
            var actionDistribution = Distribution.createSingleEntryDistribution(new Action(node.name()));
            builder.addActionSelection(node, actionDistribution);
        }
        for (var node : nodes) {
            for (var action : actions) {
                for (var observation : observations) {
                    builder.addTransition(node, action, observation, Distribution.createUniformDistribution(nodes));
                }
            }
        }
        return builder.addNodes(nodes).createFiniteStateController();
    }

    public FiniteStateControllerBuilder addNode(String nodeString) {
        return addNode(nodeString, false);
    }

    public FiniteStateControllerBuilder addNode(String nodeString, boolean initial) {
        var node = new Node(nodeString);
        return addNode(node, initial);
    }

    public FiniteStateControllerBuilder addNode(Node node) {
        return addNode(node, false);
    }

    public FiniteStateControllerBuilder addNode(Node node, boolean initial) {
        this.nodes.add(node);
        if (initial) this.initialNode = node;
        return this;
    }

    public FiniteStateControllerBuilder addNodes(Collection<Node> nodes) {
        this.nodes.addAll(nodes);
        return this;
    }

    public FiniteStateControllerBuilder setInitialNode(String nodeString) {
        var node = new Node(nodeString);
        return setInitialNode(node);
    }

    public FiniteStateControllerBuilder setInitialNode(Node node) {
        this.initialNode = node;
        return this;
    }

    public FiniteStateControllerBuilder addActionSelection(String nodeString, Distribution<String> actionStringDistribution) {
        var node = new Node(nodeString);
        var distributionMap = new HashMap<Action, Double>();
        for (var actionString : actionStringDistribution.getEntries()) {
            var action = new Action(actionString);
            distributionMap.putIfAbsent(action, actionStringDistribution.getProbability(actionString));
        }
        Distribution<Action> actions;
        try {
            actions = new Distribution<>(distributionMap);
        } catch (DistributionEmptyException | DistributionSumNotOneException e) {
            throw new IllegalStateException(e);
        }
        return addActionSelection(node, actions);
    }

    public FiniteStateControllerBuilder addActionSelection(Node node, Distribution<Action> actions) {
        this.actionFunction.put(node, actions);
        return this;
    }

    public FiniteStateControllerBuilder addTransition(String nodeString, String actionString, String observationString, Distribution<String> nextNodeStringDistribution) {
        var node = new Node(nodeString);
        var action = new Action(actionString);
        var observation = new Observation(observationString);
        var distributionMap = new HashMap<Node, Double>();
        for (var nextNodeString : nextNodeStringDistribution.getEntries()) {
            var nextNode = new Node(nextNodeString);
            distributionMap.putIfAbsent(nextNode, nextNodeStringDistribution.getProbability(nextNodeString));
        }
        Distribution<Node> nextNodeDistribution;
        try {
            nextNodeDistribution = new Distribution<>(distributionMap);
        } catch (DistributionEmptyException | DistributionSumNotOneException e) {
            throw new IllegalStateException(e);
        }
        return addTransition(node, action, observation, nextNodeDistribution);
    }

    public FiniteStateControllerBuilder addTransition(Node node, Action action, Observation observation, Distribution<Node> nextNodeDistribution) {
        this.transitionFunction.putIfAbsent(node, new HashMap<>());
        this.transitionFunction.get(node).putIfAbsent(action, new HashMap<>());
        this.transitionFunction.get(node).get(action).put(observation, nextNodeDistribution);
        return this;
    }

    public FiniteStateController createFiniteStateController() {
        return new FiniteStateController(initialNode, nodes, actionFunction, transitionFunction);
    }
}