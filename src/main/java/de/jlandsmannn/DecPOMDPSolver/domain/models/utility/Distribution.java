package de.jlandsmannn.DecPOMDPSolver.domain.models.utility;

import java.util.Comparator;
import java.util.Map;

public class Distribution<T> {
    private final Map<T, Double> distribution;
    private final T currentMax;

    public Distribution(Map<T, Double> distribution) throws DistributionEmptyException, DistributionSumNotOneException {
        this.distribution = distribution;
        this.currentMax = calculateMax();

        var sumOfDistributions = this.distribution.values().stream().reduce(Double::sum).orElse(0D);
        if (sumOfDistributions != 1) throw new DistributionSumNotOneException(sumOfDistributions);
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

    private T calculateMax() throws DistributionEmptyException {
        var entryStream = distribution.entrySet().stream();
        var maxEntry = entryStream.max(Comparator.comparingDouble(Map.Entry::getValue));
        return maxEntry.orElseThrow(DistributionEmptyException::new).getKey();
    }
}
