package de.jlandsmannn.DecPOMDPSolver.domain.solving;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;

/**
 * This is an abstract class representing a solver for DecPOMDP.
 *
 * @param <DECPOMDP>    the DecPOMDP class to solve
 * @param <THIS> identity class, for better return values
 */
public interface IDecPOMDPSolverWithConfig<DECPOMDP extends IDecPOMDP<?>, CONFIG, THIS extends IDecPOMDPSolverWithConfig<DECPOMDP, CONFIG, ?>> extends IDecPOMDPSolver<DECPOMDP, THIS> {

  THIS setConfig(CONFIG config);

}
