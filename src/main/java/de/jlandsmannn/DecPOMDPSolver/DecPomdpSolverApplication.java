package de.jlandsmannn.DecPOMDPSolver;

import de.jlandsmannn.DecPOMDPSolver.domain.models.DecPOMDPBuilder;
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
		var builder = new DecPOMDPBuilder();
	}

}
