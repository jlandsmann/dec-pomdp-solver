package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import java.util.Optional;

public interface EquationSystemSolver<MATRIX> {
  EquationSystemSolver<MATRIX> setDimensions(long numberOfEquations, long numberOfVariables);

  EquationSystemSolver<MATRIX> setMatrix(MATRIX a);

  EquationSystemSolver<MATRIX> setVector(MATRIX a);

  Optional<MATRIX> solve();
}
