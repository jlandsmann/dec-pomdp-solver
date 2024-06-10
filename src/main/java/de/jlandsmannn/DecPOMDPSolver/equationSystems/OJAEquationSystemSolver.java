package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.EquationSystemSolver;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This class implements the {@link EquationSystemSolver}
 * by using the OjAlgo library, and it's LU decomposition.
 */
@Service
public class OJAEquationSystemSolver implements EquationSystemSolver<MatrixStore<Double>> {
  private static final Logger LOG = LoggerFactory.getLogger(OJAEquationSystemSolver.class);
  private MatrixStore<Double> matrix;
  private MatrixStore<Double> vector;
  private long numberOfEquations = 0;
  private long numberOfVariables = 0;

  @Override
  public OJAEquationSystemSolver setDimensions(long numberOfEquations, long numberOfVariables) {
    if (numberOfEquations <= 0 || numberOfVariables <= 0) {
      throw new IllegalArgumentException("Number of equations and variables must be greater than zero");
    }
    this.numberOfEquations = numberOfEquations;
    this.numberOfVariables = numberOfVariables;
    return this;
  }

  @Override
  public OJAEquationSystemSolver setMatrix(MatrixStore<Double> matrix) {
    if (matrix == null || matrix.getRowDim() == 0 || matrix.getColDim() == 0) {
      throw new IllegalArgumentException("Matrix needs to be at least 1x1.");
    }
    if (this.numberOfEquations == 0) this.numberOfEquations = matrix.getRowDim();
    if (this.numberOfVariables == 0) this.numberOfVariables = matrix.getColDim();
    if (this.numberOfEquations != matrix.getRowDim() || this.numberOfVariables != matrix.getColDim()) {
      throw new IllegalArgumentException("Matrix doesnt match required dimensions");
    }
    this.matrix = matrix;
    return this;
  }

  @Override
  public OJAEquationSystemSolver setVector(MatrixStore<Double> vector) {
    if (vector == null || vector.getRowDim() == 0 || vector.getColDim() == 0) {
      throw new IllegalArgumentException("Vector needs to be at least 1x1.");
    }
    if (this.numberOfEquations == 0) this.numberOfEquations = vector.getRowDim();
    if (this.numberOfEquations != vector.getRowDim() || vector.getColDim() != 1) {
      throw new IllegalArgumentException("Vector doesnt match required dimensions");
    }
    this.vector = vector;
    return this;
  }

  @Override
  public Optional<MatrixStore<Double>> solve() {
    LOG.info("Solving system of equations with {} equations and {} variables", numberOfEquations, numberOfVariables);
    LOG.debug("System consists of matrix {} and vector {}", matrix, vector);
    final var solver = LU.R064.make(matrix);
    try {
      var result = solver.solve(matrix, vector);
      LOG.info("Solving was successful");
      return Optional.of(result);
    } catch (RecoverableCondition e) {
      LOG.info("Solving failed: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
