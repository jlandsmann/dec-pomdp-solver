package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
