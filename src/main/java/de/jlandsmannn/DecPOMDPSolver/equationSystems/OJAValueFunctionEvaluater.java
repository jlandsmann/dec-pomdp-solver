package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.EquationSystemSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.IDecPOMDPWithStateController;
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
public class OJAValueFunctionEvaluater extends ValueFunctionEvaluater<IDecPOMDPWithStateController<?>, MatrixStore<Double>> {
  @Autowired
  public OJAValueFunctionEvaluater(ValueFunctionTransformer<IDecPOMDPWithStateController<?>, MatrixStore<Double>> transformer,
                                   EquationSystemSolver<MatrixStore<Double>> solver) {
    super(transformer, solver);
  }
}
