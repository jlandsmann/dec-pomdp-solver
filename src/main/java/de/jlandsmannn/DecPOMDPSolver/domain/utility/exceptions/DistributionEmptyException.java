package de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions;

/**
 * This exception is thrown if a distribution consists of zero elements.
 * This state is problematic because such a distribution cannot have
 * a sum of probabilities equals one, nor a maximum element.
 */
public class DistributionEmptyException extends RuntimeException {
}
