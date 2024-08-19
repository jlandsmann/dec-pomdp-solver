package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.CustomCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ORLinearProgramSolver implements LinearOptimizationSolver<MPSolver, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(ORLinearProgramSolver.class);

  protected MPSolver linearProgram;

  ORLinearProgramSolver() {
    Loader.loadNativeLibraries();
  }

  public static void main(String[] args) {
    var solver = new ORLinearProgramSolver();
    var lp = MPSolver.createSolver("GLOP");
    if (lp == null) {
      LOG.error("Could not create GLOP");
      return;
    }

    var x = lp.makeNumVar(0.0, 1.0, "x");
    var y = lp.makeNumVar(0.0, 2.0, "y");

    System.out.println("Number of variables = " + lp.numVariables());
    // [END variables]

    // [START constraints]
    double infinity = java.lang.Double.POSITIVE_INFINITY;
    var ct = lp.makeConstraint(-infinity, 2.0, "ct");
    ct.setCoefficient(x, 1);
    ct.setCoefficient(y, 1);

    System.out.println("Number of constraints = " + lp.numConstraints());
    // [END constraints]

    // [START objective]
    // Create the objective function, 3 * x + y.
    var objective = lp.objective();
    objective.setCoefficient(x, 3);
    objective.setCoefficient(y, 1);

    solver.setLinearProgram(lp);
    var results = solver.maximise();
    LOG.info(results.toString());
  }

  @Override
  public void setLinearProgram(MPSolver linearProgram) {
    this.linearProgram = linearProgram;
  }

  @Override
  public Optional<Map<String, Double>> maximise() {
    linearProgram.objective().setMaximization();
    var result = linearProgram.solve();
    return transformResultIntoMap(result);
  }

  @Override
  public Optional<Map<String, Double>> minimise() {
    linearProgram.objective().setMinimization();
    var result = linearProgram.solve();
    return transformResultIntoMap(result);
  }

  private Optional<Map<String, Double>> transformResultIntoMap(MPSolver.ResultStatus result) {
    if (!result.equals(MPSolver.ResultStatus.OPTIMAL)) {
      LOG.debug("Minimising linear program was not successful: {}", result.name());
      return Optional.empty();
    }
    LOG.debug("Minimising linear program has feasible solution: {}", result);
    var mappedResults = createMapOfVariables();
    mappedResults.put("objective", linearProgram.objective().value());
    return Optional.of(mappedResults);
  }

  private Map<String, Double> createMapOfVariables() {
    return Stream.of(linearProgram.variables())
      .map(v -> Map.entry(v.name(), v.solutionValue()))
      .collect(CustomCollectors.toMap())
      ;
  }
}
