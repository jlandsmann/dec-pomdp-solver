package de.jlandsmannn.DecPOMDPSolver.cmd;

import de.jlandsmannn.DecPOMDPSolver.policyIteration.HeuristicPolicyIterationSolver;
import org.hibernate.validator.constraints.CodePointLength;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.shell.command.CommandRegistration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

@Command(command = "heuristic", group = "Heuristic Policy Iteration", alias = "h")
@Component
public class HeuristicPolicyIterationAlgorithm {

  private final HeuristicPolicyIterationSolver solver;
  private boolean initialized = false;
  private boolean loaded = false;

  @Autowired
  public HeuristicPolicyIterationAlgorithm(HeuristicPolicyIterationSolver solver) {
    this.solver = solver;
  }

  @Command(command = "", alias = {"h"}, description = "Prints information about this algorithm.")
  public String help() {
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

  @Command(command = "init", alias = "i", description = "Initialize the heuristic policy iteration solver.")
  public String init(
    @Option(shortNames = 'k', defaultValue = "10") int numberOfBeliefPoints,
    @Option(shortNames = 'l', defaultValue = "20") int maxIterations
  ) {
    solver
      .setNumberOfBeliefPoints(numberOfBeliefPoints)
      .setMaxIterations(maxIterations);

    return new StringBuilder()
      .append("Initialized heuristic policy iteration with ")
      .append(numberOfBeliefPoints)
      .append(" belief points and ")
      .append(maxIterations)
      .append(" max iterations.")
      .toString();
  }

  @Command(command = "load", alias = "l", description = "Load a problem instance to solve.")
  public void loadDecPOMDP(
    @Option(shortNames = 'f', required = true) String filename,
    @Option(shortNames = 'd', defaultValue = "0") double discountFactor
  ) {
  }

  @Command(command = "solve", alias = "s", description = "Solve the loaded problem instance.")
  public String solve() {
    if (!initialized) {
      return "Heuristic Policy Iteration is not initialized yet.";
    } else if (!loaded) {
      return "Heuristic Policy Iteration is not loaded yet.";
    }
    var result = solver.solve();
    return "Heuristic Policy Iteration finished. Result: " + result;
  }

}
