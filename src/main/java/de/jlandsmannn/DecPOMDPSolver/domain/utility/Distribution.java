package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionEmptyException;

import java.util.*;
import java.util.stream.Collectors;

public class Distribution<T> {
    private final Map<T, Double> distribution;
    private final T currentMax;

    public static <T> Distribution<T> createUniformDistribution(Set<T> entries) {
        try {
            var distribution = entries.stream()
                    .map(e -> Map.entry(e, 1D / entries.size()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new Distribution<T>(distribution);
        } catch (DistributionEmptyException | DistributionSumNotOneException e) {
            throw new IllegalStateException("Sum of distributions not one", e);
        }
    }

    public static <T> Distribution<T> createSingleEntryDistribution(T entry) {
        try {
            return new Distribution<>(Map.of(entry, 1D));
        } catch (DistributionEmptyException | DistributionSumNotOneException e) {
            throw new IllegalStateException(e);
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
        this.distribution = new HashMap<>(distribution);
        this.currentMax = calculateMax();
    }

    public int size() {
        return distribution.size();
    }

    public T getMax() {
        return currentMax;
    }

    public Set<T> getItems() {
        return distribution.keySet();
    }

    public Set<Map.Entry<T, Double>> entrySet() {
        return distribution.entrySet();
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

    public void replaceEntryWithDistribution(T item, Distribution<T> replacement) {
        var probabilityOfItemToReplace = getProbability(item);
        if (probabilityOfItemToReplace <= 0) return;
        if (replacement.getProbability(item) > 0) throw new IllegalStateException("Replacement distribution cant contain item to replace.");
        distribution.remove(item);
        for (var entry : replacement.entrySet()) {
            var currentProbability = getProbability(entry.getKey());
            var probabilityOfReplacementEntry = entry.getValue();
            distribution.put(entry.getKey(), currentProbability + (probabilityOfItemToReplace * probabilityOfReplacementEntry));
        }
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

    @Override
    public int hashCode() {
        return Objects.hash("Distribution", distribution);
    }


    private void validateDistribution(Map<T, Double> distribution) throws DistributionEmptyException, DistributionSumNotOneException {
        if (distribution.isEmpty()) {
            throw new DistributionEmptyException();
        }
        var sumOfDistributions = distribution.values().stream().reduce(Double::sum).orElse(0D);
        if (sumOfDistributions < 0.9999999999999999D) {
            throw new DistributionSumNotOneException(sumOfDistributions);
        }
    }

    private T calculateMax() {
        var entryStream = distribution.entrySet().stream();
        var maxEntry = entryStream.max(Comparator.comparingDouble(Map.Entry::getValue)).orElseThrow(IllegalStateException::new);
        return maxEntry.getKey();
    }
}
