package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import org.ojalgo.netio.BasicLogger;
import org.ojalgo.optimisation.Expression;
import org.ojalgo.optimisation.ExpressionsBasedModel;
import org.ojalgo.optimisation.Optimisation.Result;
import org.ojalgo.optimisation.Variable;

import java.io.File;

/**
 * A program that shows how to use an MPS file with ojAlgo
 *
 * @author apete
 */
public class LPSolverOja {

  public static void main(final String[] args) {
    File mpsFile = new File("./testprob.mps");
    ExpressionsBasedModel model = ExpressionsBasedModel.parse(mpsFile);

    // Optionally validate the model
    if (model.validate()) {
      BasicLogger.debug("MPS-model ok!");
    } else {
      BasicLogger.debug("MPS-model problem!");
    }
    BasicLogger.debug(model);

    // The MPS format does not include a standard way to specify if the
    // model/problem is meant to be minimised or maximised. There is a convention
    // to include an OBJSENSE section where this would be specified, but this
    // is not a requirement.
    Result minimiseResult = model.minimise();

    // Print the result
    BasicLogger.debug("Minimised => " + minimiseResult);
    // The solution variable values are returned in the order the columns/variables
    // were defined in the MPS-file.

    Result maximiseResult = model.maximise();

    BasicLogger.debug("Maximised => " + maximiseResult);

    /*
     * Reading an MPS creates an ExpressionsBasedModel just as if you'd created it programatically, and
     * you may continue work on that model.
     */

    BasicLogger.debug();
    BasicLogger.debug("=== Variables ===");
    for (Variable var : model.getVariables()) {
      BasicLogger.debug(var);
    }

    BasicLogger.debug();
    BasicLogger.debug("=== Expressions ===");
    for (Expression exp : model.getExpressions()) {
      BasicLogger.debug(exp);
    }
    BasicLogger.debug();

    // Alter the two inequalities to allow a slightly wider range
    model.getExpression("LIM1").upper(5.5); // Change from 5.0 to 5.5
    model.getExpression("LIM2").lower(9.5); // Change from 10.0 to 9.5
    BasicLogger.debug("Modified => " + model.minimise());
    // Now the solution is no longer integer valued (as it happened to be before),
    // but we can add that requirement to each of the variables...
    for (Variable var : model.getVariables()) {
      var.integer(true);
    }
    // We get the old solution back
    BasicLogger.debug("Integer constrained => " + model.minimise());

    File modifiedModel = new File("./testprob.ebm");
    /*
     * We can also write the model to file, but now we have to use the EBM file format.
     */
    model.writeTo(modifiedModel);
    /*
     * That model can be read back the same way we read the MPS file before.
     */
    model = ExpressionsBasedModel.parse(modifiedModel);

    BasicLogger.debug();
    BasicLogger.debug("Modified model");
    BasicLogger.debug(model);
  }

}