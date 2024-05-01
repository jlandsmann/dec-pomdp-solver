package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;

public abstract class DecPOMDPSolver<T extends DecPOMDP<A>, A extends Agent> {
  protected T decPOMDP;
  protected Distribution<State> initialBeliefState;

  public void setDecPOMDP(T decPOMDP) {
    this.decPOMDP = decPOMDP;
  }

  public void setInitialBeliefState(Distribution<State> initialBeliefState) {
    this.initialBeliefState = initialBeliefState;
  }

  public abstract double solve();

}
