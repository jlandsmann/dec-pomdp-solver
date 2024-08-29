package de.jlandsmannn.DecPOMDPSolver.cmd;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.RepresentativeObservationsDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.io.IDPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration.IsomorphicHeuristicPolicyIterationConfig;
import de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration.IsomorphicHeuristicPolicyIterationSolver;
import de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration.RepresentativeObservationsHeuristicPolicyIterationSolver;
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
 * This command class contains all commands regarding the heuristic policy iteration algorithm
 * for isomorphic DecPOMDPs with representative observations.
 * There are three main commands: init, load and solve.
 * For more detailed information, have a look at the commands themselves.
 */
@Command(command = "representativeObservations", group = "Representative Observations Heuristic Policy Iteration", alias = "r")
@Component
public class RepresentativeObservationsHeuristicPolicyIterationAlgorithmCommand extends BaseHeuristicPolicyIterationAlgorithmCommand<RepresentativeObservationsDecPOMDPWithStateController> {
  private final static Logger LOG = LoggerFactory.getLogger(RepresentativeObservationsHeuristicPolicyIterationAlgorithmCommand.class);

  private final RepresentativeObservationsHeuristicPolicyIterationSolver solver;
  private final IsomorphicHeuristicPolicyIterationConfig myDefaultConfig;

  @Autowired
  public RepresentativeObservationsHeuristicPolicyIterationAlgorithmCommand(RepresentativeObservationsHeuristicPolicyIterationSolver solver,
                                                                            IsomorphicHeuristicPolicyIterationConfig defaultConfig) {
    super(defaultConfig.policyIterationConfig());
    this.solver = solver;
    this.myDefaultConfig = defaultConfig;
  }

  /**
   * The help command prints some information about the used algorithm.
   */
  @Override
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
   * It parses the DecPOMDP as isomorphic DecPOMDP with representative observations.
   * @param filename The file from where the DecPOMDP should be loaded.
   * @return An optional containing the successfully parsed DecPOMDP or nothing otherwise
   */
  @Override
  protected Optional<RepresentativeObservationsDecPOMDPWithStateController> loadDecPOMDP(String filename) {
    return IDPOMDPFileParser
      .parseDecPOMDP(filename)
      .map(IsomorphicDecPOMDPWithStateControllerBuilder::createRepresentativeObservationsDecPOMDP);
  }

  /**
   * Performs the adjusted heuristic policy iteration algorithm on the given DecPOMDP.
   * @param decPOMDP The DecPOMDP to solve.
   * @return The expected reward for the initial belief state of the DecPOMDP.
   */
  @Override
  protected double doSolve(RepresentativeObservationsDecPOMDPWithStateController decPOMDP) {
    return solver
      .setDecPOMDP(decPOMDP)
      .setConfig(myDefaultConfig.withPolicyIterationConfig(config))
      .solve();
  }
}
