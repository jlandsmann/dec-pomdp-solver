package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.Optional;

public interface EquationSystemSolver {
    Optional<Vector<Double>> solve();
}
