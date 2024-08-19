package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import com.google.ortools.linearsolver.MPSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.CombinatorialNodePruningTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization.LinearOptimizationSolver;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.CombinatorialNodePruner;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * This is just a proxy for the {@link CombinatorialNodePruner}
 * to provide a concretely typed instance to the DI.
 * It connects {@link ORCombinatorialNodePruningTransformer} and {@link ORLinearProgramSolver}.
 */

@Primary
@Qualifier("ORTools")
@Service
public class ORCombinatorialNodePruner extends CombinatorialNodePruner<IDecPOMDPWithStateController<?>, MPSolver, Map<String, Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(ORCombinatorialNodePruner.class);

  @Autowired
  public ORCombinatorialNodePruner(CombinatorialNodePruningTransformer<IDecPOMDPWithStateController<?>, MPSolver, Map<String, Double>> transformer,
                                   LinearOptimizationSolver<MPSolver, Map<String, Double>> solver) {
    super(transformer, solver);
  }
}
