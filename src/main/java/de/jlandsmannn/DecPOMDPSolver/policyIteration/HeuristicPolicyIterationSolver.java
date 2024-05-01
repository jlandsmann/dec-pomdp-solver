package de.jlandsmannn.DecPOMDPSolver.policyIteration;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class HeuristicPolicyIterationSolver extends DecPOMDPSolver<DecPOMDPWithStateController, AgentWithStateController> {
  protected int numberOfBeliefPoints;
  protected Set<Distribution<State>> beliefPoints;
  protected int decPOMDPHash = 0;

  public void setNumberOfBeliefPoints(int numberOfBeliefPoints) {
    this.numberOfBeliefPoints = numberOfBeliefPoints;
  }

  @Override
  public double solve() {
    generateBeliefPoints();
    do {
      saveControllerHash();
      evaluateValueFunction();
      performExhaustiveBackup();
      retainDominatingNodes();
      pruneCombinatorialDominatedNodes();
    } while (hasControllerChanged());
    return getValue();
  }

  protected void saveControllerHash() {
    this.decPOMDPHash = decPOMDP.hashCode();
  }

  protected void generateBeliefPoints() {

  }

  protected void evaluateValueFunction() {

  }

  protected void performExhaustiveBackup() {

  }

  protected void retainDominatingNodes() {

  }

  protected void pruneCombinatorialDominatedNodes() {

  }

  protected boolean hasControllerChanged() {
    return decPOMDPHash != decPOMDP.hashCode();
  }

  protected double getValue() {
    return decPOMDP.getValue(initialBeliefState);
  }
}
