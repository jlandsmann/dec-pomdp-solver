package de.jlandsmannn.DecPOMDPSolver.equationSystems.exceptions;

public class SolvingFailedException extends Exception {
  public SolvingFailedException(String m) {
    super(m);
  }

  public SolvingFailedException(Throwable t) {
    super(t);
  }
}
