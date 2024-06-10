package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class implements the {@link LinearOptimizationSolver}
 * by using the OJAlgo library's optimization package.
 */
@Service
public class OJALinearProgramSolver implements LinearOptimizationSolver<ExpressionsBasedModel, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJALinearProgramSolver.class);

  protected ExpressionsBasedModel linearProgram;

  @Override
  public void setLinearProgram(ExpressionsBasedModel linearProgram) {
    LOG.debug("Receiving linear program: {}", linearProgram);
    this.linearProgram = linearProgram;
  }

  @Override
  public Optional<Map<String, Double>> maximise() {
    var result = linearProgram.maximise();
    return transformResultIntoMap(result);
  }

  @Override
  public Optional<Map<String, Double>> minimise() {
    var result = linearProgram.minimise();
    return transformResultIntoMap(result);
  }

  private Optional<Map<String, Double>> transformResultIntoMap(Optimisation.Result result) {
    if (!result.getState().isOptimal()) {
      LOG.debug("Minimising linear program was not successful: {}", result.getState());
      return Optional.empty();
    }
    LOG.debug("Minimising linear program has feasible solution: {}", result);
    var mappedResults = createMapOfVariables();
    return Optional.of(mappedResults);
  }

  private Map<String, Double> createMapOfVariables() {
    return linearProgram.getVariables()
      .stream()
      .map(v -> Map.entry(v.getName(), v.getValue().doubleValue()))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
    ;
  }
}
