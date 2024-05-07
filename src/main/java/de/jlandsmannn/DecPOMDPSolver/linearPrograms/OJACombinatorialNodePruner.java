package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruner;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OJACombinatorialNodePruner extends CombinatorialNodePruner<ExpressionsBasedModel, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJACombinatorialNodePruner.class);

  @Autowired
  public OJACombinatorialNodePruner(CombinatorialNodePruningTransformer<ExpressionsBasedModel, Map<String, Double>> transformer,
                                    LinearOptimizationSolver<ExpressionsBasedModel, Map<String, Double>> solver) {
    super(transformer, solver);
  }
}
