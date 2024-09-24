package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives.State;
import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.ValueFunctionTransformer;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.DecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.lifting.IsomorphicDecPOMDPWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.SparseStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

/**
 * This class implements the {@link ValueFunctionTransformer}
 * and transforms a {@link DecPOMDPWithStateController}
 * into a matrix and vector, that work with the OjAlgo library.
 */
@Service
public class OJAIsomorphicValueFunctionTransformer extends OJAValueFunctionTransformer<IsomorphicDecPOMDPWithStateController> implements ValueFunctionTransformer<IsomorphicDecPOMDPWithStateController, MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJAIsomorphicValueFunctionTransformer.class);

  @Override
  protected void calculateMatrixRow(SparseStore<Double> matrixBuilder, State state, Vector<Node> nodeVector, long rowIndex) {
    decPOMDP.getStates().stream().parallel().forEach(newState -> {
      decPOMDP.getNodeCombinations(nodeVector).stream().parallel().forEach(newNodeVector -> {
        var coefficient = getCoefficient(state, nodeVector, newState, newNodeVector);
        var normalizedNewNodeVector = normalizeVector(newNodeVector);
        var columnIndex = indexOfStateAndNodeVector(newState, normalizedNewNodeVector);
        matrixBuilder.add(rowIndex, columnIndex, coefficient);
      });
    });
    matrixBuilder.add(rowIndex, rowIndex, -1);
  }

  protected <U> Vector<U> normalizeVector(Vector<U> vector) {
    var rawNormalizedVector = new ArrayList<U>(vector.size());
    var offset = 0;
    for (var agent : decPOMDP.getAgents()) {
      var elements = new ArrayList<>(vector.toList().subList(offset, offset + agent.getPartitionSize()));
      elements.sort(Comparator.comparing(Objects::toString));
      rawNormalizedVector.addAll(elements);
      offset += agent.getPartitionSize();
    }
    return Vector.of(rawNormalizedVector);
  }
}
