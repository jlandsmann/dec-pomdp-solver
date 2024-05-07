package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.EquationSystemSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.policyIteration.ValueFunctionEvaluater;
import org.ojalgo.matrix.store.MatrixStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OJAValueFunctionEvaluater extends ValueFunctionEvaluater<DecPOMDPWithStateController, MatrixStore<Double>> {
  @Autowired
  public OJAValueFunctionEvaluater(ValueFunctionTransformer<DecPOMDPWithStateController, MatrixStore<Double>> transformer, EquationSystemSolver<MatrixStore<Double>> solver) {
    super(transformer, solver);
  }
}
