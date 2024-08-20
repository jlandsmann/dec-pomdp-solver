package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.solver.acm.SolverACM;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Qualifier("ACM")
@Service
public class ACMLinearProgramSolver extends OJALinearProgramSolver {

  ACMLinearProgramSolver() {
    ExpressionsBasedModel.addIntegration(SolverACM.INTEGRATION);
  }

}
