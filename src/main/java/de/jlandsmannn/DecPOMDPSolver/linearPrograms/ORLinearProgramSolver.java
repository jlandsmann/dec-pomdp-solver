package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.modelbuilder.LinearArgument;
import com.google.ortools.modelbuilder.ModelBuilder;
import com.google.ortools.modelbuilder.ModelSolver;
import com.google.ortools.modelbuilder.SolveStatus;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.CustomCollectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class ORLinearProgramSolver implements LinearOptimizationSolver<ModelBuilder, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(ORLinearProgramSolver.class);

  protected ModelSolver solver;
  protected ModelBuilder linearProgram;

  ORLinearProgramSolver() {
    Loader.loadNativeLibraries();
    solver = new ModelSolver("GLOP");
  }

  @Override
  public void setLinearProgram(ModelBuilder linearProgram) {
    this.linearProgram = linearProgram;
  }

  @Override
  public Optional<Map<String, Double>> maximise() {
    linearProgram.getHelper().setMaximize(true);
    var result = solver.solve(linearProgram);
    return transformResultIntoMap(result);
  }

  @Override
  public Optional<Map<String, Double>> minimise() {
    linearProgram.getHelper().setMaximize(false);
    var result = solver.solve(linearProgram);
    return transformResultIntoMap(result);
  }

  private Optional<Map<String, Double>> transformResultIntoMap(SolveStatus result) {
    if (!result.equals(SolveStatus.OPTIMAL)) {
      LOG.debug("Linear program was not successful: {}", result.name());
      return Optional.empty();
    }
    LOG.debug("Linear program has feasible solution: {}", result);
    var mappedResults = createMapOfVariables();
    mappedResults.put("objective", solver.getObjectiveValue());
    return Optional.of(mappedResults);
  }

  private Map<String, Double> createMapOfVariables() {
    return IntStream.range(0, linearProgram.numVariables())
      .mapToObj(i -> linearProgram.varFromIndex(i))
      .map(v -> Map.entry(v.getName(), solver.getValue(v)))
      .collect(CustomCollectors.toMap())
      ;
  }
}
