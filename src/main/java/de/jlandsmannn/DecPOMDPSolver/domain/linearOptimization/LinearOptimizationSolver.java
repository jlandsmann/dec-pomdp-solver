package de.jlandsmannn.DecPOMDPSolver.domain.linearOptimization;

import java.util.Optional;

/**
 * This interface describes a library and data type independent class
 * which can solve a linear optimization program by minimizing or maximizing.
 *
 * @param <LP>     the data type of linear program
 * @param <RESULT> the data type of the result
 */
public interface LinearOptimizationSolver<LP, RESULT> {

  void setLinearProgram(LP linearProgram);

  Optional<RESULT> maximise();

  Optional<RESULT> minimise();
}
