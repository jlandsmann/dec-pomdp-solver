package de.jlandsmannn.DecPOMDPSolver.cmd;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IAgent;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.IDecPOMDP;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.Action;
import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.CustomCollectors;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.io.DPOMDPFileParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is an abstract base class for various commands that cover some kind of heuristic policy iteration algorithm.
 * It provides basic sub-commands as help, initialization, loading, set initial policies and solving.
 * @param <DECPOMDP> The type of DecPOMDP this command is used for
 */
public abstract class BaseHeuristicPolicyIterationAlgorithmCommand<DECPOMDP extends IDecPOMDP<?>> {
  private static Logger LOG = LoggerFactory.getLogger(BaseHeuristicPolicyIterationAlgorithmCommand.class);

  protected final HeuristicPolicyIterationConfig defaultConfig;
  protected HeuristicPolicyIterationConfig config;

  protected DECPOMDP decPOMDP;
  protected boolean initialized = false;
  protected boolean loaded = false;

  protected BaseHeuristicPolicyIterationAlgorithmCommand(HeuristicPolicyIterationConfig defaultConfig) {
    this.defaultConfig = defaultConfig;
  }
  /**
   * The help command prints some information about the used algorithm.
   */
  @Command(command = "", alias = {"h"}, description = "Prints information about this algorithm.")
  public abstract String help();

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
    config = defaultConfig.withNumberOfBeliefPoints(numberOfBeliefPoints).withMaxIterations(maxIterations);
    initialized = true;

    return "Initialized heuristic policy iteration with " +
      config.beliefPointDesiredNumber() +
      " belief points and " +
      config.maxIterations() +
      " max iterations.";
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
  public String load(
    @Option(shortNames = 'f', required = true) String filename,
    @Option(shortNames = 'd', defaultValue = "-1") double discountFactor
  ) {
    LOG.info("Command 'load' was called with filename={}.", filename);
    if (!initialized) throw new IllegalStateException("Heuristic policy iteration is not initialized yet.");

    var optionalDecPOMDP = loadDecPOMDP(filename);
    if (optionalDecPOMDP.isEmpty()) {
      LOG.warn("Parsing failed for file {}", filename);
      return "Could not parse " + filename + ". Make sure the file exists.";
    }
    decPOMDP = optionalDecPOMDP.get();
    applyDiscountFactor(decPOMDP, discountFactor);

    loaded = true;
    LOG.info("Successfully loaded DecPOMDP from file {}.", filename);
    return "Successfully loaded DecPOMDP";
  }

  /**
   * This function actually loads the DecPOMDP from the given file.
   * @param filename The file from where the DecPOMDP should be loaded.
   * @return An optional DecPOMDP, which is empty if an error occurred
   */
  protected abstract Optional<DECPOMDP> loadDecPOMDP(String filename);

  /**
   * Since the algorithm is an infinite horizon algorithm,
   * it can not work with a discount factor of 1.
   * Therefore, we need to ensure that 0 <= discountFactor < 1.
   * @param decpomdp The parsed DecPOMDP
   * @param discountFactor The discountFactor to use
   */
  protected void applyDiscountFactor(DECPOMDP decpomdp, double discountFactor) {
    if (discountFactor >= 0) {
      LOG.info("Custom discountFactor={} set, applying to DecPOMDP.", discountFactor);
      decpomdp.setDiscountFactor(discountFactor);
    }
    if (decpomdp.getDiscountFactor() == 1) {
      LOG.info("Overwriting discountFactor to 0.9 because 1 is not supported for infinite horizon planning.");
      decpomdp.setDiscountFactor(0.9);
    }
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
    @Option(shortNames = 'a', required = true, arity = CommandRegistration.OptionArity.ONE_OR_MORE) String actionDistributions
  ) {
    LOG.info("Command 'initialPolicy' was called with actionDistributions={}.", actionDistributions);
    if (!initialized) throw new IllegalStateException("Heuristic policy iteration is not initialized yet.");
    if (!loaded) throw new IllegalStateException("Heuristic policy iteration is not loaded yet.");

    var initialPolicies = parseInitialPolicies(actionDistributions);
    config = config.withInitialPolicies(initialPolicies);
    return "Successfully set initial policies";
  }

  protected Map<IAgent, Map<State, Distribution<Action>>> parseInitialPolicies(String actionDistributions) {
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
        .collect(CustomCollectors.toMap());
      initialPolicies.put(agent, stateActionDistributions);
    }
    return initialPolicies;
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
    var result = doSolve(decPOMDP);
    LOG.info("Successfully solved DecPOMDP with value of {}", result);
    return "Heuristic policy iteration finished. Result: " + result;
  }

  /**
   * This actually solves the DecPOMDP.
   * This way the command logic and the solving logic are separated.
   * @param decPOMDP The DecPOMDP to solve.
   * @return The expected reward for the initial belief state (value) of the given DecPOMDP
   */
  protected abstract double doSolve(DECPOMDP decPOMDP);

}
