package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.List;
import java.util.Map;

public class TestDecPOMDP extends BasicDecPOMDP<TestAgent> {

  public TestDecPOMDP(List<TestAgent> testAgents,
                      List<State> states,
                      double discountFactor,
                      Distribution<State> initialBeliefState,
                      Map<State, Map<Vector<Action>, Distribution<State>>> transitionFunction,
                      Map<State, Map<Vector<Action>, Double>> rewardFunction,
                      Map<Vector<Action>, Map<State, Distribution<Vector<Observation>>>> observationFunction) {
    super(testAgents, states, discountFactor, initialBeliefState, transitionFunction, rewardFunction, observationFunction);
  }

  @Override
  public double getValue(Distribution<State> beliefSate) {
    return 0;
  }
}
