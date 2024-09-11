package de.jlandsmannn.DecPOMDPSolver;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.io.DPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.IDPOMDPFileParser;

public class DecPOMDPGenerator {
  public static DecPOMDPWithStateController getDecTigerPOMDP() {
    var builder = DPOMDPFileParser.parseDecPOMDP("problems/DecTiger.dpomdp").orElseThrow();
    return builder.setDiscountFactor(0.8).createDecPOMDP();
  }

  public static DecPOMDPWithStateController getDecTigerPOMDPWithLargeFSC() {
    var decPOMDP = getDecTigerPOMDP();
    for (var agent : decPOMDP.getAgents()) {
      var controller = FiniteStateControllerBuilder.createArbitraryController(
        agent.getName(),
        agent.getActions(),
        agent.getObservations(),
        4
      );
      agent.setController(controller);
    }
    return decPOMDP;
  }

  public static IsomorphicDecPOMDPWithStateController getIsomorphicDecPOMDP() {
    return getIsomorphicDecPOMDP(2);
  }

  public static IsomorphicDecPOMDPWithStateController getIsomorphicDecPOMDP(int partitionSize) {
    return getIsomorphicDecPOMDP(partitionSize, 1);
  }

  public static IsomorphicDecPOMDPWithStateController getIsomorphicDecPOMDP(int partitionSize, int controllerSize) {
    var builder = IDPOMDPFileParser.parseDecPOMDP("problems/MedicalNanoscale2.idpomdp").orElseThrow();
    for (var agent : builder.getAgents()) {
      agent.setPartitionSize(partitionSize);
      var controller = FiniteStateControllerBuilder.createArbitraryController(
        agent.getName(),
        agent.getActions(),
        agent.getObservations(),
        controllerSize
      );
      agent.setController(controller);
    }
    return builder.setDiscountFactor(0.8).createDecPOMDP();
  }
}
