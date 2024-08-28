package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class implements the {@link LinearOptimizationSolver}
 * by using the OJAlgo library's optimization package.
 */
@Primary
@Qualifier("OJA")
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
    try {
      var result = linearProgram.maximise();
      return transformResultIntoMap(result);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Override
  public Optional<Map<String, Double>> minimise() {
    try {
      var result = linearProgram.minimise();
      return transformResultIntoMap(result);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public Optional<Map<String, Double>> optimize() {
    var sense = linearProgram.getOptimisationSense();
    if (sense == Optimisation.Sense.MAX) {
      return maximise();
    } else {
      return minimise();
    }
  }

  private Optional<Map<String, Double>> transformResultIntoMap(Optimisation.Result result) {
    if (!result.getState().isOptimal() && !result.getState().isFeasible()) {
      LOG.debug("Minimising linear program was not successful: {}", result.getState());
      return Optional.empty();
    }
    LOG.debug("Minimising linear program has feasible solution: {}", result);
    var mappedResults = createMapOfVariables();
    mappedResults.put("objective", result.getValue());
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
