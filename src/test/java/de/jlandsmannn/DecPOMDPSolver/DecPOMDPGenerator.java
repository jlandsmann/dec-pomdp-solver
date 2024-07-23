package de.jlandsmannn.DecPOMDPSolver;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.DecPOMDPBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.*;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.ILiftedAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicAgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import de.jlandsmannn.DecPOMDPSolver.io.DPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.IDPOMDPFileParser;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class DecPOMDPGenerator {
  private static final List<Node> correlationNodes = Node.listOf("C1");

  public static DecPOMDPWithStateController getDecTigerPOMDP() {
    var builder = DPOMDPFileParser.parseDecPOMDP("problems/DecTiger.dpomdp").orElseThrow();
    return builder.setDiscountFactor(0.8).createDecPOMDP();
  }

  public static DecPOMDPWithStateController getDecTigerPOMDPWithLargeFSC() {
    var decPOMDP = getDecTigerPOMDP();
    for (var agent : decPOMDP.getAgents()) {
      var controller = FiniteStateControllerBuilder.createArbitraryController(
        agent.getName(),
        correlationNodes,
        4,
        agent.getActions(),
        agent.getObservations()
      );
      agent.setController(controller);
    }
    return decPOMDP;
  }

  public static IsomorphicDecPOMDPWithStateController getIsomorphicDecPOMDP() {
    return getIsomorphicDecPOMDP(2);
  }

  public static IsomorphicDecPOMDPWithStateController getIsomorphicDecPOMDP(int partitionSize) {
    var builder = IDPOMDPFileParser.parseDecPOMDP("problems/MedicalNanoscale2.idpomdp").orElseThrow();
    for (var agent : builder.getAgents()) {
      agent.setPartitionSize(partitionSize);
    }
    return builder.setDiscountFactor(0.8).createDecPOMDP();
  }
}
