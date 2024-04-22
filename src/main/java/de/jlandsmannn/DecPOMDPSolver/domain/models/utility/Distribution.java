package de.jlandsmannn.DecPOMDPSolver.domain.models.utility;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Distribution<T> {
    private final Map<T, Double> distribution;
    private final T currentMax;

    public static <T> Distribution<T> createUniformDistribution(Set<T> entries) throws DistributionEmptyException {
        try {
            var distribution = entries.stream()
                    .map(e -> Map.entry(e, 1D / entries.size()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new Distribution<T>(distribution);
        } catch (DistributionSumNotOneException e) {
            throw new IllegalStateException("Sum of distributions not one", e);
        }
    }

    /**
     * This constructor can be used to create a distribution with probabilities based on distribution
     * @param distribution A non-empty map of distributions, where the values must sum up to 1
     * @throws DistributionEmptyException is thrown if distribution is empty
     * @throws DistributionSumNotOneException is thrown if sum of values in distribution is not one
     */
    public Distribution(Map<T, Double> distribution) throws DistributionEmptyException, DistributionSumNotOneException {
        validateDistribution(distribution);
        this.distribution = distribution;
        this.currentMax = calculateMax();
    }

    public int size() {
        return distribution.size();
    }

    public T getMax() {
        return currentMax;
    }

    public Double getProbability(T item) {
        return distribution.getOrDefault(item, 0D);
    }

    public T getRandom() {
        var rand = Math.random();
        for (var entry : this.distribution.entrySet()) {
            rand -= entry.getValue();
            if (rand <= 0) return entry.getKey();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Distribution<?>) {
            return distribution.equals(((Distribution<?>) obj).distribution);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return distribution.toString();
    }


    private void validateDistribution(Map<T, Double> distribution) throws DistributionEmptyException, DistributionSumNotOneException {
        if (distribution.isEmpty()) {
            throw new DistributionEmptyException();
        }
        var sumOfDistributions = distribution.values().stream().reduce(Double::sum).orElse(0D);
        if (sumOfDistributions != 1) {
            throw new DistributionSumNotOneException(sumOfDistributions);
        }
    }

    private T calculateMax() {
        var entryStream = distribution.entrySet().stream();
        var maxEntry = entryStream.max(Comparator.comparingDouble(Map.Entry::getValue)).orElseThrow(IllegalStateException::new);
        return maxEntry.getKey();
    }
}
