package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.*;

/**
 * This parser covers the "actions" section of the .dpomdp file format.
 * It is used to get the names of the actions with respect to their agent.
 * In case only a number of actions are defined,
 * it uses the names of the agents to create generic agent-specific action names.
 * Therefore, it needs to know the agent's names.
 */
public class ActionsSectionParser extends BaseSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(ActionsSectionParser.class);

  protected List<String> agentNames = new ArrayList<>();
  protected List<List<Action>> agentActions = new ArrayList<>();

  public ActionsSectionParser() {
    super(DPOMDPSectionKeyword.ACTIONS,
      DPOMDPSectionKeyword.ACTIONS + ": ?\\R" +
        "(?<agentActions>" + ROWS_OF(OR(COUNT_PATTERN, LIST_OF(IDENTIFIER_PATTERN))) + ")"
    );
  }

  public ActionsSectionParser setAgentNames(List<String> agentNames) {
    this.agentNames = agentNames;
    return this;
  }

  public List<List<Action>> getAgentActions() {
    return agentActions;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'actions' section.");
    assertAllDependenciesSet();

    var match = getMatchOrThrow(section);
    var rawActionsPerAgent = match.getGroupAsStringArrayOrThrow("agentActions", "\\R");
    if (rawActionsPerAgent.length != agentNames.size()) {
      throw new ParsingFailedException("'actions' does not have same number of agents as 'agents' section.");
    }

    for (int i = 0; i < rawActionsPerAgent.length; i++) {
      var rawActionsForAgent = rawActionsPerAgent[i];
      var agentName = agentNames.get(i);
      if (rawActionsForAgent.matches(CommonPattern.INDEX_PATTERN)) {
        var numberOfActions = CommonParser.parseIntegerOrThrow(rawActionsForAgent);
        if (numberOfActions == 0) throw new ParsingFailedException("Number of actions must be greater than 0.");
        var actions = generateActionsForAgent(agentName, numberOfActions);
        agentActions.add(i, actions);
      } else {
        var actionNames = rawActionsForAgent.split(" ");
        var actions = Action.listOf(actionNames);
        agentActions.add(i, actions);
      }
    }
  }

  private void assertAllDependenciesSet() {
    if (agentNames.isEmpty()) {
      throw new ParsingFailedException("'actions' section was parsed, before agents have been initialized.");
    }
  }

  private List<Action> generateActionsForAgent(String agentName, int numberOfActions) {
    return IntStream
      .range(0, numberOfActions)
      .mapToObj(idx -> agentName + "-A" + idx)
      .map(Action::from)
      .toList();
  }
}
