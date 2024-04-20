package de.jlandsmannn.DecPOMDPSolver.domain.models.utility;

public class DistributionSumNotOneException extends Exception {
    DistributionSumNotOneException(Double sumOfDistributions) {
        super("Sum is currently " + sumOfDistributions + " but should be 1");
    }
}
