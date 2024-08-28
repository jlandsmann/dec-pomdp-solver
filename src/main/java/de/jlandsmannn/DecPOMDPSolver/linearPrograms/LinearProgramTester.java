package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import org.ojalgo.optimisation.ExpressionsBasedModel;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.stream.Stream;

public class LinearProgramTester {

  Random random = new Random();
  StringBuilder output = new StringBuilder();

  ORLinearProgramSolver solverA = new ORLinearProgramSolver();
  OJALinearProgramSolver solverB = new OJALinearProgramSolver();
  OJALinearProgramSolver solverC = new ACMLinearProgramSolver();

  public static void main(String[] args) {
    var directory = "./mps/";
    var fileArray = new File(directory).listFiles();
    assert fileArray != null;
    Stream.of(fileArray)
      .filter(file -> !file.isDirectory())
      .map(File::getName)
      .sorted()
      .map(fileName -> directory + fileName)
      .forEach(LinearProgramTester::solveLPFromFile);
  }

  private static void solveLPFromFile(String fileName) {
    try {
      var tester = new LinearProgramTester();
      tester.createLPFromFile(fileName);
      tester.optimize();
    } catch (Throwable t) {
      System.out.print("failed");
    } finally {
      System.out.println();
    }
  }

  ExpressionsBasedModel linearProgram;

  public void createLPFromFile(String filename) {
    System.out.print(fixedLength(filename, 20));
    System.out.print(" : ");
    var file = new File(filename);
    linearProgram = ExpressionsBasedModel.parse(file);
  }

  public void createRandomLP(int numberOfVariables) {
    linearProgram = new ExpressionsBasedModel();

    for (int i = 0; i < numberOfVariables; i++) {
      var lowerBound = random.nextDouble(-100, 0);
      var upperBound = random.nextDouble(lowerBound, 100);
      var objectiveCoefficient = random.nextDouble(0, 1);
      var varB = linearProgram.addVariable("V" + i).lower(lowerBound).upper(upperBound);
      linearProgram.objective().set(varB, objectiveCoefficient);
    }

    var numberOfConstraints = numberOfVariables * 2;
    for (int i = 0; i < numberOfConstraints; i++) {
      var numberOfVars = random.nextInt(2, 20);
      var lowerBound = random.nextDouble(-100, 0);
      var upperBound = random.nextDouble(lowerBound, 100);

      var conB = linearProgram.addExpression("C" + i).lower(lowerBound).upper(upperBound);
      for (int j = 0; j < numberOfVars; j++) {
        var index = random.nextInt(0, numberOfVariables);
        var coefficient = random.nextDouble(0, 1);
        var varB = linearProgram.getVariable(index);
        conB.set(varB, coefficient);
      }
    }
  }

  public void optimize() {
    System.out.print(fixedLength(formatInteger(linearProgram.countVariables()), 7));
    System.out.print(" : ");
    System.out.print(fixedLength(formatInteger(linearProgram.countExpressions()), 7));
    System.out.print(" : ");
    solveLinearProgram(solverA, "OR");
    System.out.print(" : ");
    solveLinearProgram(solverB, "OJA");
    System.out.print(" : ");
    solveLinearProgram(solverC, "ACM");
  }

  private void solveLinearProgram(OJALinearProgramSolver solver, String name) {
    System.out.print(name);
    System.out.print(" : ");
    var startTime = System.currentTimeMillis();
    solver.setLinearProgram(linearProgram.copy());
    // the problems given by NETLIB are to be minimized
    var result = solver.minimise();
    var objectiveValue = result
      .map(x -> x.get("objective"))
      .map(o -> String.format("%.1E", o))
      .orElse("-");
    var timeUsed = System.currentTimeMillis() - startTime;
    System.out.print(fixedLength(formatInteger(timeUsed), 7));
  }

  private static String formatInteger(Number input) {
    var format = new DecimalFormat("###,###,###");
    return format.format(input);
  }

  private static String fixedLength(String input, int length) {
    var fill = "";
    if (input.length() < length) {
      fill = " ".repeat(length - input.length());
    }
    return fill + input;
  }
}
