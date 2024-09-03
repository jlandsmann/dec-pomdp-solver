package de.jlandsmannn.DecPOMDPSolver.cmd;

import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.DPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.IDPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationConfig;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This command class contains all commands regarding the heuristic policy iteration algorithm
 * for ground DecPOMDPs.
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

  /**
   * This function parses a ground DecPOMDP from the given file.
   * If the given file describes an isomorphic DecPOMDP,
   * it transforms the isomorphic DecPOMDP to an equivalent ground DecPOMDP.
   * @param filename The file from where the DecPOMDP should be loaded.
   * @return A ground DecPOMDP
   */
  @Override
  protected Optional<IDecPOMDPWithStateController<?>> loadDecPOMDP(String filename) {
    if (filename.endsWith(".idpomdp")) {
      return IDPOMDPFileParser.parseDecPOMDP(filename).map(IsomorphicDecPOMDPWithStateControllerBuilder::createGroundDecPOMDP);
    }
    return DPOMDPFileParser.parseDecPOMDP(filename).map(DecPOMDPWithStateControllerBuilder::createDecPOMDP);
  }

  /**
   * Initializes the heuristic policy iteration algorithm and order it to solve the given DecPOMDP.
   * @param decPOMDP The DecPOMDP to solve.
   * @return The expected reward for the initial belief state of the DecPOMDP
   */
  @Override
  protected double doSolve(IDecPOMDPWithStateController<?> decPOMDP) {
    return solver.setDecPOMDP(decPOMDP).setConfig(config).solve();
  }
}
