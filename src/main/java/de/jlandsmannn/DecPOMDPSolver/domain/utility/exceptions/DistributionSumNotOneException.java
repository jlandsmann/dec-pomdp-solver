package de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions;

/**
 * This exception is thrown if the sum of a distribution
 * does not have a sum of probabilities equals one.
 */
public class DistributionSumNotOneException extends RuntimeException {
  public DistributionSumNotOneException(double sumOfDistributions) {
    super("Sum is currently " + sumOfDistributions + " but should be 1");
  }
}
