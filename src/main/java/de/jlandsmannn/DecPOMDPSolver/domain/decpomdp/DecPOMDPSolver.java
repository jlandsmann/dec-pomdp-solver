package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DecPOMDPSolver<T extends DecPOMDP<? extends Agent>> {
  private static final Logger LOG = LoggerFactory.getLogger(DecPOMDPSolver.class);
  protected T decPOMDP;
  protected Distribution<State> initialBeliefState;

  public void setDecPOMDP(T decPOMDP) {
    this.decPOMDP = decPOMDP;
  }

  public void setInitialBeliefState(Distribution<State> initialBeliefState) {
    this.initialBeliefState = initialBeliefState;
  }

  public abstract double solve();

  protected double getValueOfDecPOMDP() {
    double value = decPOMDP.getValue(initialBeliefState);
    LOG.info("Retrieving value for initialBeliefState: {}", value);
    return value;
  }

}
