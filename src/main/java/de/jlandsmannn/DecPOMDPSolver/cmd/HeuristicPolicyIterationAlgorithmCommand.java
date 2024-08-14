package de.jlandsmannn.DecPOMDPSolver.cmd;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.io.DPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.IDPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationConfig;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.shell.command.CommandRegistration.OptionArity;

/**
 * This command class contains all commands regarding the heuristic policy iteration algorithm.
 * There are three main commands: init, load and solve.
 * For more detailed information, have a look at the commands themselves.
 */
@Command(command = "heuristic", group = "Heuristic Policy Iteration", alias = "h")
@Component
public class HeuristicPolicyIterationAlgorithmCommand extends BaseHeuristicPolicyIterationAlgorithmCommand<IDecPOMDPWithStateController<?>> {
  private static Logger LOG = LoggerFactory.getLogger(HeuristicPolicyIterationAlgorithmCommand.class);

  protected final HeuristicPolicyIterationSolver solver;

  @Autowired
  public HeuristicPolicyIterationAlgorithmCommand(HeuristicPolicyIterationSolver solver, HeuristicPolicyIterationConfig defaultConfig) {
    super(defaultConfig);
    this.solver = solver;
  }

  /**
   * The help command prints some information about the used algorithm.
   */
  @Command(command = "", alias = {"h"}, description = "Prints information about this algorithm.")
  public String help() {
    LOG.info("help command called.");
    return new StringBuilder()
      .append("This algorithm is based on the algorithm presented in")
      .append(System.lineSeparator())
      .append("'Policy Iteration for Decentralized Control of Markov Decision Processes'")
      .append(System.lineSeparator())
      .append("by Bernstein et.al. from 2009.")
      .append(System.lineSeparator())
      .append("Stochastic finite state controller are used to represent the agents policies. ")
      .append("Furthermore exhaustive backups are performed for exploration.")
      .append(System.lineSeparator())
      .append("It utilizes more or less random generated so called belief points to direct the pruning of explored policies.")
      .toString();
  }

  @Override
  protected Optional<IDecPOMDPWithStateController<?>> loadDecPOMDP(String filename) {
    return DPOMDPFileParser.parseDecPOMDP(filename).map(DecPOMDPWithStateControllerBuilder::createDecPOMDP);
  }

  @Override
  protected double doSolve(IDecPOMDPWithStateController<?> decPOMDP) {
    return solver.setDecPOMDP(decPOMDP).setConfig(config).solve();
  }
}
