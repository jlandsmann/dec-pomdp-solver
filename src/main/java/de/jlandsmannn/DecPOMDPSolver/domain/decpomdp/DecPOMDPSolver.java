package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DecPOMDPSolver<T extends DecPOMDP<? extends Agent>, THIS extends DecPOMDPSolver<T, ?>> {
  private static final Logger LOG = LoggerFactory.getLogger(DecPOMDPSolver.class);
  protected T decPOMDP;
  protected Distribution<State> initialBeliefState;

  public THIS setDecPOMDP(T decPOMDP) {
    this.decPOMDP = decPOMDP;
    return (THIS) this;
  }

  public THIS setInitialBeliefState(Distribution<State> initialBeliefState) {
    this.initialBeliefState = initialBeliefState;
    return (THIS) this;
  }

  public abstract double solve();

  protected double getValueOfDecPOMDP() {
    return decPOMDP.getValue(initialBeliefState);
  }

}
