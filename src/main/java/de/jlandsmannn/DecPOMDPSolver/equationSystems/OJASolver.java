package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.EquationSystemSolver;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class OJASolver implements EquationSystemSolver<MatrixStore<Double>, MatrixStore<Double>> {
  private MatrixStore<Double> matrix;
  private MatrixStore<Double> vector;
  private int numberOfEquations = 0;
  private int numberOfVariables = 0;

  @Override
  public OJASolver setDimensions(int numberOfEquations, int numberOfVariables) {
    this.numberOfEquations = numberOfEquations;
    this.numberOfVariables = numberOfVariables;
    return this;
  }

  @Override
  public OJASolver setMatrix(MatrixStore<Double> matrix) {
    if (this.numberOfEquations == 0) this.numberOfEquations = matrix.getRowDim();
    if (this.numberOfVariables == 0) this.numberOfVariables = matrix.getColDim();
    if (this.numberOfEquations != matrix.getRowDim() || this.numberOfVariables != matrix.getColDim()) {
      throw new IllegalArgumentException("Matrix doesnt match required dimensions");
    }
    this.matrix = matrix;
    return this;
  }

  @Override
  public OJASolver setVector(MatrixStore<Double> vector) {
    if (this.numberOfEquations == 0) this.numberOfEquations = vector.getRowDim();
    if (this.numberOfEquations != vector.getRowDim() || vector.getColDim() != 1) {
      throw new IllegalArgumentException("Vector doesnt match required dimensions");
    }
    this.vector = vector;
    return this;
  }

  @Override
  public Optional<Vector<Double>> solve() {
    final var solver = LU.R064.make(matrix);
    try {
      var result = solver.solve(matrix, vector);
      var resultVector = transformMatrixStoreToVector(result);
      return Optional.of(resultVector);
    } catch (RecoverableCondition e) {
      return Optional.empty();
    }
  }

  public Vector<Double> transformMatrixStoreToVector(MatrixStore<Double> matrixStore) {
    return new Vector<>(Arrays.stream(matrixStore.toRawCopy1D()).boxed().toList());
  }
}
