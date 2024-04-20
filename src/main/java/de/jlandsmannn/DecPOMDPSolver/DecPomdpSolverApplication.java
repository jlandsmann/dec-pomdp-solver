package de.jlandsmannn.DecPOMDPSolver;

import de.jlandsmannn.DecPOMDPSolver.domain.models.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.DistributionEmptyException;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.DistributionSumNotOneException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DecPomdpSolverApplication {

	public static void main(String[] args) throws DistributionSumNotOneException, DistributionEmptyException {
		// SpringApplication.run(DecPomdpSolverApplication.class, args);

		try {
			Distribution<String> distribution = new Distribution<>(Map.of(
					"A", 0.2,
					"B", 0.2,
					"C", 0.4,
					"D", 0.1,
					"E", 0.1
			));

			Map<String, Long> counts = new HashMap<>();
			long totalCount = 100_000_000L;

			for (long i = 0; i < totalCount; i++) {
				var randomElement = distribution.getRandom();
				var currentCount = counts.getOrDefault(randomElement, 0L);
				counts.put(randomElement, currentCount + 1);
			}

			for (var entry : counts.entrySet()) {
				var relative = Math.round((float) (100L * entry.getValue()) / totalCount);
				System.out.println(entry.getKey() + ": " + entry.getValue() + "(" + relative + "%)");
			}
		} catch (Throwable e) {
			System.out.println(e.getMessage());
		}
	}

}
