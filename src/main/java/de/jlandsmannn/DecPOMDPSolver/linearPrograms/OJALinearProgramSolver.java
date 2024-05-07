package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class OJALinearProgramSolver implements LinearOptimizationSolver<ExpressionsBasedModel, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJALinearProgramSolver.class);

  protected ExpressionsBasedModel linearProgram;

  @Override
  public void setLinearProgram(ExpressionsBasedModel linearProgram) {
    this.linearProgram = linearProgram;
  }

  @Override
  public Optional<Map<String, Double>> maximise() {
    var result = linearProgram.maximise();
    if (!result.getState().isSuccess()) {
      LOG.info("Maximising linear program was not successful: {}", result.getState());
      return Optional.empty();
    }
    LOG.info("Maximising linear program has feasible solution: {}", result);
    var mappedResults = getMapFromResults(result);
    return Optional.of(mappedResults);
  }

  @Override
  public Optional<Map<String, Double>> minimise() {
    var result = linearProgram.minimise();
    if (!result.getState().isSuccess()) {
      LOG.info("Minimising linear program was not successful: {}", result.getState());
      return Optional.empty();
    }
    LOG.info("Minimising linear program has feasible solution: {}", result);
    var mappedResults = getMapFromResults(result);
    return Optional.of(mappedResults);
  }

  private Map<String, Double> getMapFromResults(Optimisation.Result result) {
    var map = new HashMap<String, Double>();
    linearProgram.getVariables()
      .stream()
      .map(v -> Map.entry(v.getName(), v.getValue().doubleValue()))
      .forEach(e -> map.put(e.getKey(), e.getValue()))
    ;
    return map;
  }
}
