package de.jlandsmannn.DecPOMDPSolver.domain.models.utility;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DistributionTest {

    @RepeatedTest(5)
    public void shouldReturnWeightedRandomElement() throws DistributionSumNotOneException, DistributionEmptyException {
        long totalCount = 1_000_000L;
        Map<String, Long> counts = new HashMap<>();
        Distribution<String> distribution = new Distribution<>(Map.of(
                "A", 0.1D,
                "B", 0.2D,
                "C", 0.5D,
                "D", 0.1D,
                "E", 0.1D
        ));

        // repeatedly get random element
        for (long i = 0; i < totalCount; i++) {
            var randomElement = distribution.getRandom();
            var currentCount = counts.getOrDefault(randomElement, 0L);
            counts.put(randomElement, currentCount + 1);
        }

        for (var entry : counts.entrySet()) {
            var expected = distribution.getProbability(entry.getKey());
            var relative = (double) Math.round((float) (100L * entry.getValue()) / totalCount) / 100D;
            assertEquals(expected, relative, entry.getKey() + " was not selected correctly");
        }


    }

}