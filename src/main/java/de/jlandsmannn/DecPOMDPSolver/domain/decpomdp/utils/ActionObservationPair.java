package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.utils;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;

public record ActionObservationPair(Action action, Observation observation) {
}
