package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.Optional;

public interface EquationSystemSolver<MATRIX> {
  EquationSystemSolver<MATRIX> setDimensions(int numberOfEquations, int numberOfVariables);

  EquationSystemSolver<MATRIX> setMatrix(MATRIX a);

  EquationSystemSolver<MATRIX> setVector(MATRIX a);

  Optional<MATRIX> solve();
}
