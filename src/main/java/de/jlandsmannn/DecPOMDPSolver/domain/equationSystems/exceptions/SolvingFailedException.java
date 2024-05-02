package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.exceptions;

public class SolvingFailedException extends RuntimeException {
  public SolvingFailedException(String m) {
    super(m);
  }

  public SolvingFailedException(Throwable t) {
    super(t);
  }

  public SolvingFailedException() {
    super();
  }
}
