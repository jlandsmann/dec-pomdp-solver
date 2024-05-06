package de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization;

import java.util.Optional;

public interface LinearOptimizationSolver<LP, RESULT> {

  void setLinearProgram(LP linearProgram);

  Optional<RESULT> maximise();

  Optional<RESULT> minimise();
}
