package de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions;

public class DistributionSumNotOneException extends RuntimeException {
  public DistributionSumNotOneException(Double sumOfDistributions) {
    super("Sum is currently " + sumOfDistributions + " but should be 1");
  }
}
