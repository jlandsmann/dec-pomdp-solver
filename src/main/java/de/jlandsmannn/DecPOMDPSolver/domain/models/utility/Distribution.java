package de.jlandsmannn.DecPOMDPSolver.domain.models.utility;

import java.util.Comparator;
import java.util.Map;

public class Distribution<T> {
    private final Map<T, Double> distribution;
    private final T currentMax;

    public Distribution(Map<T, Double> distribution) throws DistributionEmptyException {
        this.distribution = distribution;
        this.currentMax = calculateMax();
    }

    public T getMax() {
        return currentMax;
    }

    public Double getProbability(T item) {
        return distribution.getOrDefault(item, 0D);
    }

    public T calculateMax() throws DistributionEmptyException {
        var entryStream = distribution.entrySet().stream();
        var maxEntry = entryStream.max(Comparator.comparingDouble(Map.Entry::getValue));
        return maxEntry.orElseThrow(DistributionEmptyException::new).getKey();
    }
}
