package de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization;

public interface LinearOptimizationSolver {
  void solve();

  <T> T getResult(String variable);
}
