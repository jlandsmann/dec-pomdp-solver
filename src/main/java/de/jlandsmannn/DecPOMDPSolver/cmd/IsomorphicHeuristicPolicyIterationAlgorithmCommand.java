package de.jlandsmannn.DecPOMDPSolver.cmd;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicAgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateControllerBuilder;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.io.DPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.IsomorphicDPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration.IsomorphicHeuristicPolicyIterationConfig;
import de.jlandsmannn.DecPOMDPSolver.isomorphicPolicyIteration.IsomorphicHeuristicPolicyIterationSolver;
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
import java.util.stream.Collectors;

import static org.springframework.shell.command.CommandRegistration.OptionArity;

/**
 * This command class contains all commands regarding the heuristic policy iteration algorithm.
 * There are three main commands: init, load and solve.
 * For more detailed information, have a look at the commands themselves.
 */
@Command(command = "isomorphic", group = "Isomorphic Heuristic Policy Iteration", alias = "i")
@Component
public class IsomorphicHeuristicPolicyIterationAlgorithmCommand {
  private final static Logger LOG = LoggerFactory.getLogger(IsomorphicHeuristicPolicyIterationAlgorithmCommand.class);

  private final IsomorphicHeuristicPolicyIterationSolver solver;
  private final IsomorphicHeuristicPolicyIterationConfig defaultConfig;

  private IsomorphicHeuristicPolicyIterationConfig config;
  private IsomorphicDecPOMDPWithStateController decPOMDP;

  private boolean initialized = false;
  private boolean loaded = false;

  @Autowired
  public IsomorphicHeuristicPolicyIterationAlgorithmCommand(IsomorphicHeuristicPolicyIterationSolver solver, IsomorphicHeuristicPolicyIterationConfig defaultConfig) {
    this.solver = solver;
    this.defaultConfig = defaultConfig;
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
   * The init command initializes some configuration properties needed for the algorithm.
   * This includes the number of belief points to generate,
   * as well as the maximum number of iterations of the policy iteration to execute.
   * After this command, the load command should be called.
   */
  @Command(command = "init", alias = "i", description = "Initialize the heuristic policy iteration solver.")
  public String init(
    @Option(shortNames = 'k', defaultValue = "0") int numberOfBeliefPoints,
    @Option(shortNames = 'l', defaultValue = "0") int maxIterations
  ) {
    LOG.info("Command 'init' was called with numberOfBeliefPoints={}, maxIterations={}.", numberOfBeliefPoints, maxIterations);
    var heuristicConfig = defaultConfig.policyIterationConfig().withNumberOfBeliefPoints(numberOfBeliefPoints).withMaxIterations(maxIterations);
    config = defaultConfig.withPolicyIterationConfig(heuristicConfig);
    initialized = true;

    return new StringBuilder()
      .append("Initialized heuristic policy iteration with ")
      .append(config.policyIterationConfig().beliefPointDesiredNumber())
      .append(" belief points and ")
      .append(config.policyIterationConfig().maxIterations())
      .append(" max iterations.")
      .toString();
  }

  /**
   * The load command needs to be executed, after the algorithm was initialized.
   * The load command accepts a path to a file, as well as a (custom) discountFactor.
   * The file will be loaded and parsed.
   * It will be expected to follow the DPOMDP file format,
   * which is described in the MADP Toolbox.
   * The file must be located either inside the resources folder of this application,
   * or the file path must be given relative to the execution directory.
   * If the file does not exist or is not a valid DPOMDP file, the parsing will be aborted.
   * The (custom) discount factor can be used to override the discount factor defined in the loaded file.
   */
  @Command(command = "load", alias = "l", description = "Load a problem instance to solve.")
  public String loadDecPOMDP(
    @Option(shortNames = 'f', required = true) String filename,
    @Option(shortNames = 'd', defaultValue = "-1") double discountFactor
  ) {
    LOG.info("Command 'load' was called with filename={}.", filename);
    if (!initialized) throw new IllegalStateException("Heuristic policy iteration is not initialized yet.");
    var optionalBuilder = IsomorphicDPOMDPFileParser.parseDecPOMDP(filename);
    if (optionalBuilder.isEmpty()) {
      LOG.warn("Parsing failed for file {}", filename);
      return "Could not parse " + filename + ". Make sure the file exists.";
    }
    var builder = optionalBuilder.get();
    if (discountFactor >= 0) {
      LOG.info("Custom discountFactor={} set, applying to DecPOMDP.", discountFactor);
      builder.setDiscountFactor(discountFactor);
    }
    if (builder.getDiscountFactor() == 1) {
      LOG.info("Overwriting discountFactor to 0.9 because 1 is not supported for infinite horizon planning.");
      builder.setDiscountFactor(0.9);
    }
    decPOMDP = builder.createDecPOMDP();
    loaded = true;
    LOG.info("Successfully loaded DecPOMDP from file {}.", filename);
    return "Successfully loaded DecPOMDP";
  }

  /**
   * This command needs to be executed, after a DecPOMDP has been loaded.
   * It can be used to set initial policies for the generation of belief points.
   * Therefore, a space-separated list of probabilities for each action of each agent has to be defined,
   * and those have to be enclosed by quotes.
   * There must be such a list for each agent, separated by a space.
   * <p>
   * `heuristic initialPolicy "0.8 0.1 0.1" "0.8 0.1 0.1"`
   * Is an example for the DecTiger problem,
   * where listening happens by a chance of 80% and hearing left or right each by a chance of 10%.
   * Because both agents have same actions and policies, we repeat it.
   */
  @Command(command = "initialPolicy", alias = "p", description = "Set initial policies for belief point generation.")
  public String setInitialPolicies(
    @Option(shortNames = 'a', required = true, arity = OptionArity.ONE_OR_MORE) String actionDistributions
  ) {
    LOG.info("Command 'initialPolicy' was called with actionDistributions={}.", actionDistributions);
    if (!initialized) throw new IllegalStateException("Heuristic policy iteration is not initialized yet.");
    if (!loaded) throw new IllegalStateException("Heuristic policy iteration is not loaded yet.");

    var actionDistributionsPerAgent = actionDistributions.trim().split(",");
    if (actionDistributionsPerAgent.length != decPOMDP.getAgents().size()) {
      throw new IllegalArgumentException("Number of given initial policies does not match the number of agents.");
    }

    Map<IAgent, Map<State, Distribution<Action>>> initialPolicies = new HashMap<>();
    for (int i = 0; i < decPOMDP.getAgents().size(); i++) {
      var agent = decPOMDP.getAgents().get(i);
      var rawActionDistribution = CommonParser.parseActionsAndTheirDistributions(agent.getActions(), actionDistributionsPerAgent[i]);
      var actionDistribution = Distribution.of(rawActionDistribution);
      var stateActionDistributions = decPOMDP.getStates()
        .stream()
        .map(state -> Map.entry(state, actionDistribution))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      initialPolicies.put(agent, stateActionDistributions);
    }
    var heuristicConfig = defaultConfig.policyIterationConfig().withInitialPolicies(initialPolicies);
    config = defaultConfig.withPolicyIterationConfig(heuristicConfig);
    return "Successfully set initial policies";
  }

  /**
   * The solve command needs to be executed, after a DecPOMDP has been loaded.
   * It starts the solving process and does not accept any arguments.
   * It does not print information about interim steps,
   * but prints the final result of the algorithm.
   * For detailed information about the execution, have a look at the logs.
   */
  @Command(command = "solve", alias = "s", description = "Solve the loaded problem instance.")
  public String solve() {
    LOG.info("Command 'solve' was called.");
    if (!initialized) {
      LOG.warn("Aborting solving because Heuristic policy iteration is not initialized yet.");
      throw new IllegalStateException("Heuristic policy iteration is not initialized yet.");
    } else if (!loaded) {
      LOG.warn("Aborting solving because no DecPOMDP is loaded yet.");
      throw new IllegalStateException("Heuristic policy iteration is not loaded yet.");
    }
    var result = solver.setDecPOMDP(decPOMDP).setConfig(config).solve();
    LOG.info("Successfully solved DecPOMDP with value of {}", result);
    return "Heuristic policy iteration finished. Result: " + result;
  }
}
