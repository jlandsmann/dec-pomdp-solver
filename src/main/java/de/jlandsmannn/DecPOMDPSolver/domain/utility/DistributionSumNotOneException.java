package de.jlandsmannn.DecPOMDPSolver.domain.utility;

public class DistributionSumNotOneException extends Exception {
    DistributionSumNotOneException(Double sumOfDistributions) {
        super("Sum is currently " + sumOfDistributions + " but should be 1");
    }
}
