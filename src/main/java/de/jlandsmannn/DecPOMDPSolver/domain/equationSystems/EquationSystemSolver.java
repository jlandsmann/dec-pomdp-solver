package de.jlandsmannn.DecPOMDPSolver.domain.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;

import java.util.Optional;

public interface EquationSystemSolver<A, B> {
  EquationSystemSolver<A, B> setDimensions(int numberOfEquations, int numberOfVariables);

  EquationSystemSolver<A, B> setMatrix(A a);

  EquationSystemSolver<A, B> setVector(B a);

  Optional<Vector<Double>> solve();
}
