package de.jlandsmannn.DecPOMDPSolver.domain.solving;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;

/**
 * This is an abstract class representing a solver for DecPOMDP.
 *
 * @param <DECPOMDP>    the DecPOMDP class to solve
 * @param <CONFIG>    the config class to configure the solver
 * @param <THIS> identity class, for better return values
 */
public abstract class BaseDecPOMDPSolverWithConfig<DECPOMDP extends IDecPOMDP<?>, CONFIG, THIS extends BaseDecPOMDPSolverWithConfig<DECPOMDP, CONFIG, ?>>
  extends BaseDecPOMDPSolver<DECPOMDP, THIS>
  implements IDecPOMDPSolverWithConfig<DECPOMDP, CONFIG, THIS> {

  protected CONFIG config;

  @Override
  public THIS setConfig(CONFIG config) {
    this.config = config;
    return (THIS) this;
  }
}
