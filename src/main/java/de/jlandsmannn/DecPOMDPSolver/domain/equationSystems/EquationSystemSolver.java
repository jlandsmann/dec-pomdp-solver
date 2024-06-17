package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import java.util.Optional;

/**
 * This interface describes a library and data type independent class
 * which can solve a system of linear equations in the form of A*x=B.
 *
 * @param <MATRIX> the data type of matrices and vectors
 */
public interface EquationSystemSolver<MATRIX> {
  EquationSystemSolver<MATRIX> setDimensions(long numberOfEquations, long numberOfVariables);

  EquationSystemSolver<MATRIX> setMatrix(MATRIX a);

  EquationSystemSolver<MATRIX> setVector(MATRIX a);

  Optional<MATRIX> solve();
}
