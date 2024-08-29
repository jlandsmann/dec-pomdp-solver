package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.solver.acm.SolverACM;
import org.ojalgo.optimisation.solver.ortools.SolverORTools;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Qualifier("OR")
@Service
public class ORLinearProgramSolver extends OJALinearProgramSolver {

  ORLinearProgramSolver() {
    super();
    ExpressionsBasedModel.addIntegration(SolverORTools.INTEGRATION);
  }

}
