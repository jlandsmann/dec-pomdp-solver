package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Observation;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.FiniteStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IAgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Histogram;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.HistogramBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.HistogramCombinationBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This is an extension of the base agent,
 * which uses {@link FiniteStateController} to represent its own policy.
 * This agent is used by {@link DecPOMDPWithStateController}.
 */
public class LiftedAgentWithStateController extends AgentWithStateController implements IAgentWithStateController, ILiftedAgent {

  private final int numberOfAgents;

  /**
   * Default constructor with name, actions, observations and controller.
   *
   * @param name         The name of this agent
   * @param actions      The actions of this agent
   * @param observations The observations of this agent
   * @param numberOfAgents The number of agents of this agent
   * @param controller   The controller of this agent
   */
  public LiftedAgentWithStateController(String name, List<Action> actions, List<Observation> observations, int numberOfAgents, FiniteStateController controller) {
    super(name, actions, observations, controller);
    this.numberOfAgents = numberOfAgents;
  }

  public Distribution<Histogram<Action>> getActionSelection(Histogram<Node> nodeHistogram) {
    var listOfHistograms = new ArrayList<List<Map.Entry<Histogram<Action>, Double>>>();
    for (var node : nodeHistogram.keySet()) {
        var numberOfAgents = nodeHistogram.get(node);
        var localActionSelection = getActionSelection(node);
        var actionSelection = new HashMap<Histogram<Action>, Double>();
        var histograms = HistogramBuilder.listOfPeakShaped(List.copyOf(localActionSelection.keySet()), numberOfAgents);
        for (var histogram : histograms) {
          var probability = histogram.entrySet().stream()
            .mapToDouble(entry -> {
              var action = entry.getKey();
              var frequency = entry.getValue();
              var localProbability = localActionSelection.getProbability(action);
              return Math.pow(localProbability, frequency);
            })
            .sum();
          actionSelection.put(histogram, probability);
        }
        listOfHistograms.add(List.copyOf(actionSelection.entrySet()));
    }
    var rawDistribution = HistogramCombinationBuilder
      .streamOf(listOfHistograms)
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    return Distribution.of(rawDistribution);
  }

  public double getActionSelectionProbability(Histogram<Node> nodeHistogram, Histogram<Action> actionHistogram) {
    return getActionSelection(nodeHistogram).getProbability(actionHistogram);
  }

  @Override
  public double getNodeTransitionProbability(Histogram<Node> node, Histogram<Action> action, Histogram<Observation> observation, Histogram<Node> followNode) {
    
  }

  @Override
  public int getNumberOfAgents() {
    return numberOfAgents;
  }
}
