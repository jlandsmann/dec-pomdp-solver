package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.CombinatorialNodePruner;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This is just a proxy for the {@link CombinatorialNodePruner}
 * to provide a concretely typed instance to the DI.
 * It connects {@link OJACombinatorialNodePruningTransformer} and {@link OJALinearProgramSolver}.
 */
@Service
public class OJACombinatorialNodePruner extends CombinatorialNodePruner<ExpressionsBasedModel, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJACombinatorialNodePruner.class);

  @Autowired
  public OJACombinatorialNodePruner(CombinatorialNodePruningTransformer<ExpressionsBasedModel, Map<String, Double>> transformer,
                                    LinearOptimizationSolver<ExpressionsBasedModel, Map<String, Double>> solver) {
    super(transformer, solver);
  }
}
