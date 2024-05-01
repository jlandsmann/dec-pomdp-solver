package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import org.ojalgo.matrix.store.MatrixStore;
import org.springframework.stereotype.Service;

@Service
public class OJATransformer implements ValueFunctionTransformer<DecPOMDPWithStateController, MatrixStore<Double>> {

  @Override
  public MatrixStore<Double> getMatrixFromDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    return null;
  }

  @Override
  public MatrixStore<Double> getVectorFromDecPOMDP(DecPOMDPWithStateController decPOMDP) {
    return null;
  }

  @Override
  public void applyValuesToDecPOMDP(DecPOMDPWithStateController decPOMDP, MatrixStore<Double> values) {

  }
}
