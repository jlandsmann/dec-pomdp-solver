package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ojalgo.optimisation.ExpressionsBasedModel;

import static org.junit.jupiter.api.Assertions.*;

class OJALinearProgramSolverTest {

  OJALinearProgramSolver solver;
  ExpressionsBasedModel solvableLP;
  ExpressionsBasedModel nonSolvableLP;

  @BeforeEach
  void setUp() {
    solver = new OJALinearProgramSolver();
    solvableLP = createSolvableModel();
    nonSolvableLP = createNonSolvableModel();
  }

  @Test
  void setLinearProgram_ShouldNotThrowForAnyLP() {
    assertDoesNotThrow(() -> solver.setLinearProgram(solvableLP));
    assertDoesNotThrow(() -> solver.setLinearProgram(nonSolvableLP));
  }

  @Test
  void maximise_ShouldReturnSolutionForSolvableLP() {
    solver.setLinearProgram(solvableLP);
    var result = solver.maximise();
    assertTrue(result.isPresent());
    assertFalse(result.get().isEmpty());
  }

  @Test
  void maximise_ShouldNotReturnSolutionForNonSolvableLP() {
    solver.setLinearProgram(nonSolvableLP);
    var result = solver.maximise();
    assertFalse(result.isPresent());
  }

  @Test
  void maximise_ShouldReturnMapWithAllVariables() {
    solver.setLinearProgram(solvableLP);
    var result = solver.maximise().get();

    assertTrue(result.containsKey("x"));
    assertTrue(result.containsKey("y"));
  }

  @Test
  void maximise_ShouldReturnValidSolution() {
    solver.setLinearProgram(solvableLP);
    var result = solver.maximise().get();

    assertTrue(0 <= result.get("x"));
    assertTrue(result.get("y") <= 0);
    assertTrue(2 * result.get("x") + result.get("y") == 10);
  }

  @Test
  void minimise_ShouldReturnSolutionForSolvableLP() {
    solver.setLinearProgram(solvableLP);
    var result = solver.minimise();
    assertTrue(result.isPresent());
    assertFalse(result.get().isEmpty());
  }

  @Test
  void minimise_ShouldNotReturnSolutionForNonSolvableLP() {
    solver.setLinearProgram(nonSolvableLP);
    var result = solver.minimise();
    assertFalse(result.isPresent());
  }

  @Test
  void minimise_ShouldReturnMapWithAllVariables() {
    solver.setLinearProgram(solvableLP);
    var result = solver.minimise().get();

    assertTrue(result.containsKey("x"));
    assertTrue(result.containsKey("y"));
  }

  @Test
  void minimise_ShouldReturnValidSolution() {
    solver.setLinearProgram(solvableLP);
    var result = solver.minimise().get();

    assertTrue(0 <= result.get("x"));
    assertTrue(result.get("y") <= 0);
    assertTrue(2 * result.get("x") + result.get("y") == 10);
  }

  private ExpressionsBasedModel createSolvableModel() {
    var model = new ExpressionsBasedModel();
    // 0 <= x
    var x = model.newVariable("x").lower(0);
    //      y <= 0
    var y = model.newVariable("y").upper(0);
    // 2x + y = 10
    model.addExpression().add(x, 2).add(y, 1).level(10);
    return model;
  }

  private ExpressionsBasedModel createNonSolvableModel() {
    var model = new ExpressionsBasedModel();
    // 0 <= x <= 2
    var x = model.newVariable("x").lower(0).upper(2);
    //      y <= 0
    var y = model.newVariable("y").upper(0);
    // 2x + y = 10
    model.addExpression().add(x, 2).add(y, 1).level(10);
    return model;
  }
}