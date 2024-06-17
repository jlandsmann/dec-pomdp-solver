package de.jlandsmannn.DecPOMDPSolver.io.exceptions;

/**
 * This exception is raised,
 * if a file could not be parsed,
 * for any reason.
 */
public class ParsingFailedException extends RuntimeException {

  /**
   * This is the default constructors,
   * which accepts a message containing
   * some details, why the parsing failed.
   *
   * @param message a message with some reason why the parsing failed
   */
  public ParsingFailedException(String message) {
    super(message);
  }

}
