package de.jlandsmannn.DecPOMDPSolver.domain.models;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.*;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;

import java.util.Map;
import java.util.Set;

public class FiniteStateController {
    public final Set<Node> nodes = Set.of();
    public final Map<Node, Distribution<ActionNode>> actionFunction = Map.of();
    public final Map<ActionNode, Map<Observation, Distribution<Node>>> transitionFunction = Map.of();
}
