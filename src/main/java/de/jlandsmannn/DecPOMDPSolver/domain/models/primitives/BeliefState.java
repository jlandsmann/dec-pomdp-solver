package de.jlandsmannn.DecPOMDPSolver.domain.models.primitives;

import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.DistributionEmptyException;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;

import java.util.Map;

public class BeliefState extends Distribution<State> {
    public BeliefState(Map<State, Double> distribution) throws DistributionEmptyException {
        super(distribution);
    }
}
