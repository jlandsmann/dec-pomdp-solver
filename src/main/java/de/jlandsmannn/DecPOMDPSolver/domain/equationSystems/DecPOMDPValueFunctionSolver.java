package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDP;

public interface DecPOMDPValueFunctionSolver extends EquationSystemSolver {
    void fromDecPOMDP(DecPOMDP decPOMDP);
}
