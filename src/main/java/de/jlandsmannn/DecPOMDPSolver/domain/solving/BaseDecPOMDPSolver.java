package de.jlandsmannn.DecPOMDPSolver.domain.solving;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;

/**
 * This is an abstract class representing a solver for DecPOMDP.
 *
 * @param <DECPOMDP>    the DecPOMDP class to solve
 * @param <THIS> identity class, for better return values
 */
public abstract class BaseDecPOMDPSolver<DECPOMDP extends IDecPOMDP<?>, THIS extends BaseDecPOMDPSolver<DECPOMDP, ?>> implements IDecPOMDPSolver<DECPOMDP, THIS> {

  protected DECPOMDP decPOMDP;

  public THIS setDecPOMDP(DECPOMDP decPOMDP) {
    this.decPOMDP = decPOMDP;
    return (THIS) this;
  }

  public abstract double solve();

  public double getValue() {
    return decPOMDP.getValue();
  }

}
