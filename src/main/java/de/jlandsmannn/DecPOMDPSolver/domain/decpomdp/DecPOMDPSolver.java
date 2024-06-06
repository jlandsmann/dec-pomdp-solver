package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract class representing a solver for DecPOMDP.
 * @param <T> the DecPOMDP class to solve
 * @param <THIS> identity class, for better return values
 */
public abstract class DecPOMDPSolver<T extends DecPOMDP<? extends Agent>, THIS extends DecPOMDPSolver<T, ?>> {
  private static final Logger LOG = LoggerFactory.getLogger(DecPOMDPSolver.class);
  protected T decPOMDP;

  public THIS setDecPOMDP(T decPOMDP) {
    this.decPOMDP = decPOMDP;
    return (THIS) this;
  }

  public abstract double solve();

  protected double getValueOfDecPOMDP() {
    return decPOMDP.getValue();
  }

}
