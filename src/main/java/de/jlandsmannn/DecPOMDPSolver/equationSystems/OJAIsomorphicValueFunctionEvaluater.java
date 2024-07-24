package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.EquationSystemSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.ValueFunctionEvaluater;
import org.ojalgo.matrix.store.MatrixStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This is just a proxy for the {@link ValueFunctionEvaluater}
 * to provide a concretely typed instance to the DI.
 * It connects {@link OJAValueFunctionTransformer} and {@link OJAEquationSystemSolver}.
 */
@Service
public class OJAIsomorphicValueFunctionEvaluater extends ValueFunctionEvaluater<IsomorphicDecPOMDPWithStateController, MatrixStore<Double>> {
  @Autowired
  public OJAIsomorphicValueFunctionEvaluater(ValueFunctionTransformer<IsomorphicDecPOMDPWithStateController, MatrixStore<Double>> transformer,
                                             EquationSystemSolver<MatrixStore<Double>> solver) {
    super(transformer, solver);
  }
}
