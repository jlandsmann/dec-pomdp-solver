package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ActionsParser {
  private static final Logger LOG = LoggerFactory.getLogger(ActionsParser.class);

  protected List<String> agentNames = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();

  public ActionsParser setAgentNames(List<String> agentNames) {
    this.agentNames = agentNames;
    return this;
  }

  public List<List<Action>> getAgentActions() {
    return agentActions;
  }

  public void parseActions(String section) {
    LOG.debug("Parsing 'actions' section.");
    if (agentNames.isEmpty()) {
      throw new ParsingFailedException("'actions' section was parsed, before agents have been initialized.");
    }

    var match = DPOMDPSectionPattern.ACTIONS
      .getMatch(section)
      .orElseThrow(() -> new ParsingFailedException("Trying to parse 'actions' section, but found invalid format."));
    var rawActions = match.group("agentActions");
    var rawActionsPerAgent = rawActions.split("\n");
    if (rawActionsPerAgent.length != agentNames.size()) {
      throw new ParsingFailedException("'actions' does not have same number of agents as 'agents' section.");
    }
    for (int i = 0; i < rawActionsPerAgent.length; i++) {
      var rawActionsForAgent = rawActionsPerAgent[i];
      var agentName = agentNames.get(i);
      if (rawActionsForAgent.matches(CommonPattern.INDEX_PATTERN)) {
        var numberOfActions = CommonParser.parseIntegerOrThrow(rawActionsForAgent);
        if (numberOfActions == 0) throw new ParsingFailedException("Number of actions must be greater than 0.");
        var actions = IntStream.range(0, numberOfActions).mapToObj(idx -> agentName + "-A" + idx).map(Action::from).toList();
        agentActions.add(i, actions);
      } else {
        var actionNames = rawActionsForAgent.split(" ");
        var actions = Action.listOf(actionNames);
        agentActions.add(i, actions);
      }
    }
  }
}
