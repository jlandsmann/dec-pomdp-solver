package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import com.google.ortools.modelbuilder.LinearExpr;
import com.google.ortools.modelbuilder.ModelBuilder;
import org.ojalgo.optimisation.ExpressionsBasedModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.stream.Stream;

public class LinearProgramTester {

  Random random = new Random();

  ORLinearProgramSolver solverA = new ORLinearProgramSolver();
  OJALinearProgramSolver solverB = new OJALinearProgramSolver();

  public static void main(String[] args) {
    Stream.of("30n20b8.mps").forEach(fileName -> {
      var tester = new LinearProgramTester();
      tester.createLPFromFile(fileName);
      tester.maximize();
    });
    Stream.of(10, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 50000, 100000).forEach(n -> {
      var tester = new LinearProgramTester();
      tester.createRandomLP(n,n);
      // tester.maximize();
    });
  }

  ModelBuilder lpA;
  ExpressionsBasedModel lpB;


  public void createLPFromFile(String filename) {
    lpA = new ModelBuilder();
    var successA = lpA.importFromMpsFile(filename);
    var file = new File(filename);
    lpB = ExpressionsBasedModel.parse(file);
  }

  public void createRandomLP(int numberOfVariables, int numberOfConstraints) {
    lpA = new ModelBuilder();
    lpB = new ExpressionsBasedModel();

    for (int i = 0; i < numberOfVariables; i++) {
      var lowerBound = random.nextDouble(-100, 0);
      var upperBound = random.nextDouble(lowerBound, 100);
      var objectiveCoefficient = random.nextDouble(0, 1);
      var varA = lpA.newNumVar(lowerBound, upperBound, "V" + i);
      varA.setObjectiveCoefficient(objectiveCoefficient);
      var varB = lpB.addVariable("V" + i).lower(lowerBound).upper(upperBound);
      lpB.objective().set(varB, objectiveCoefficient);
    }

    for (int i = 0; i < numberOfConstraints; i++) {
      var numberOfVars = random.nextInt(1, numberOfVariables);
      var lowerBound = random.nextDouble(-100, 0);
      var upperBound = random.nextDouble(lowerBound, 100);

      var conA = lpA.addLinearConstraint(LinearExpr.newBuilder(), lowerBound, upperBound).withName("C" + i);
      var conB = lpB.addExpression("C" + i).lower(lowerBound).upper(upperBound);
      for (int j = 0; j < numberOfVars; j++) {
        var index = random.nextInt(0, numberOfVariables);
        var coefficient = random.nextDouble(0, 1);
        var varA = lpA.varFromIndex(index);
        var varB = lpB.getVariable(index);
        conA.setCoefficient(varA, coefficient);
        conB.set(varB, coefficient);
      }
    }
  }

  public void maximize() {
    System.out.println("OR: Solving LP with " + lpA.numVariables() + " variables and " + lpA.numConstraints() + " constraints");
    var startTimeA = System.currentTimeMillis();
    solverA.setLinearProgram(lpA);
    var resultA = solverA.maximise();
    var timeUsedA = System.currentTimeMillis() - startTimeA;
    System.out.println("OR result: " + resultA.isPresent());
    System.out.println("OR time: " + (timeUsedA) + " ms");

    System.out.println("OJA: Solving LP with " + lpB.countVariables() + " variables and " + lpB.countExpressions() + " constraints");
    var startTimeB = System.currentTimeMillis();
    solverB.setLinearProgram(lpB);
    var resultB = solverB.maximise();
    var timeUsedB = System.currentTimeMillis() - startTimeB;
    System.out.println("OJA result: " + resultB.isPresent());
    System.out.println("OJA time: " + (timeUsedB) + " ms");
  }

}
