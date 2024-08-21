package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.*;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.CustomCollectors;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

public class IsomorphicToGroundDecPOMDPTransformer {
  private static final Logger LOG = LoggerFactory.getLogger(IsomorphicToGroundDecPOMDPTransformer.class);

  public static DecPOMDPWithStateController transform(IsomorphicDecPOMDPWithStateController model) {
    var transformer = new IsomorphicToGroundDecPOMDPTransformer();
    return transformer.transformDecPOMDP(model);
  }

  public DecPOMDPWithStateController transformDecPOMDP(IsomorphicDecPOMDPWithStateController model) {
    var builder = new DecPOMDPWithStateControllerBuilder();
    var agents = model.getAgents().stream().flatMap(this::transformIsomorphicAgentToGroundAgents).toList();
    builder
      .addStates(model.getStates())
      .addAgents(agents)
      .setDiscountFactor(model.getDiscountFactor())
      .setInitialBeliefState(model.getInitialBeliefState())
    ;

    for (var state : model.getStates()) {
      for (var actionVector : model.getActionCombinations()) {
        var reward = model.getReward(state, actionVector);
        builder.addReward(state, actionVector, reward);

        var followState = calculateTransition(model, state, actionVector);
        builder.addTransition(state, actionVector, followState);

        var observations = calculateObservation(model, actionVector, state);
        builder.addObservation(actionVector, state, observations);
      }
    }
    return builder.createDecPOMDP();
  }

  private Distribution<State> calculateTransition(IDecPOMDP<?> model, State state, Vector<Action> actionVector) {
    return model.getStates().stream().map(followState -> {
      var probability = model.getTransitionProbability(state, actionVector, followState);
      return Map.entry(followState, probability);
    }).collect(CustomCollectors.toNormalizedDistribution());
  }

  private Distribution<Vector<Observation>> calculateObservation(IDecPOMDP<?> model, Vector<Action> actionVector, State followState) {
    return model.getObservationCombinations().stream().map(observation -> {
      var probability = model.getObservationProbability(actionVector, followState, observation);
      return Map.entry(observation, probability);
    }).collect(CustomCollectors.toNormalizedDistribution());
  }
  
  private Stream<IAgentWithStateController> transformIsomorphicAgentToGroundAgents(IsomorphicAgentWithStateController agent) {
    var agents = new ArrayList<IAgentWithStateController>();
    for (int i = 0; i < agent.getPartitionSize(); i++) {
      var agentBuilder = new AgentWithStateControllerBuilder();
      var groundAgent = agentBuilder
        .setName(agent.getName() + "#" + i)
        .setActions(agent.getActions())
        .setObservations(agent.getObservations())
        .createAgent();
      agents.add(groundAgent);
    }
    return agents.stream();
  }

}
