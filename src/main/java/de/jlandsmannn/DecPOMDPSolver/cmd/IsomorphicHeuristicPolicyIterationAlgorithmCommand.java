package de.jlandsmannn.DecPOMDPSolver.cmd;

import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.io.IDPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration.IsomorphicHeuristicPolicyIterationConfig;
import de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration.IsomorphicHeuristicPolicyIterationSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.command.annotation.Command;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * This command class contains all commands regarding the heuristic policy iteration algorithm
 * for isomorphic DecPOMDPs.
 * There are three main commands: init, load and solve.
 * For more detailed information, have a look at the commands themselves.
 */
@Command(command = "isomorphic", group = "Isomorphic Heuristic Policy Iteration", alias = "i")
@Component
public class IsomorphicHeuristicPolicyIterationAlgorithmCommand extends BaseHeuristicPolicyIterationAlgorithmCommand<IsomorphicDecPOMDPWithStateController> {
  private final static Logger LOG = LoggerFactory.getLogger(IsomorphicHeuristicPolicyIterationAlgorithmCommand.class);

  private final IsomorphicHeuristicPolicyIterationSolver solver;
  private final IsomorphicHeuristicPolicyIterationConfig myDefaultConfig;

  @Autowired
  public IsomorphicHeuristicPolicyIterationAlgorithmCommand(IsomorphicHeuristicPolicyIterationSolver solver, IsomorphicHeuristicPolicyIterationConfig defaultConfig) {
    super(defaultConfig.policyIterationConfig());
    this.solver = solver;
    this.myDefaultConfig = defaultConfig;
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
   * This function actually loads the isomorphic DecPOMDP from the given file.
   * It expects the file to be in the .idpomdp file format.
   * @param filename The file from where the DecPOMDP should be loaded.
   * @return An optional containing the successfully parsed DecPOMDP or nothing otherwise
   */
  @Override
  protected Optional<IsomorphicDecPOMDPWithStateController> loadDecPOMDP(String filename) {
    return IDPOMDPFileParser.parseDecPOMDP(filename).map(IsomorphicDecPOMDPWithStateControllerBuilder::createDecPOMDP);
  }

  /**
   * Performs the adjusted heuristic policy iteration algorithm on the given DecPOMDP.
   * @param decPOMDP The DecPOMDP to solve.
   * @return The expected reward for the initial belief state of the DecPOMDP.
   */
  @Override
  protected double doSolve(IsomorphicDecPOMDPWithStateController decPOMDP) {
    return solver
      .setDecPOMDP(decPOMDP)
      .setConfig(myDefaultConfig.withPolicyIterationConfig(config))
      .solve();
  }


}
