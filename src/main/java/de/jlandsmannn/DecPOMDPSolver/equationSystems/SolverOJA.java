package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.domain.equationSystems.exceptions.SolvingFailedException;
import org.ojalgo.RecoverableCondition;
import org.ojalgo.matrix.decomposition.LU;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

public class SolverOJA {
  private MatrixStore<Double> a;
  private MatrixStore<Double> b;
  private int numberOfEquations = 0;
  private int numberOfVariables = 0;

  public static void main(String[] args) {
    benchmark(14_000);
  }

  public static void benchmark(int numberOfVariables) {
    SolverOJA solver = new SolverOJA()
      .setDimensions(numberOfVariables, numberOfVariables)
      .setA(Primitive64Store.FACTORY.makeFilled(numberOfVariables, numberOfVariables, Uniform.of(0, 50)))
      .setB(Primitive64Store.FACTORY.makeFilled(numberOfVariables, 1, Uniform.of(0, 10)));
    var start = System.currentTimeMillis();
    var success = true;
    try {
      solver.solve();
    } catch (SolvingFailedException e) {
      success = false;
    } finally {
      var end = System.currentTimeMillis();
      System.out.printf("%5d : %8d ms. Success: %b%n", numberOfVariables, end - start, success);
    }
  }

  public static void test() {
    var size = 20_000;
    SolverOJA solver = new SolverOJA()
      .setDimensions(size, size)
      .setA(Primitive64Store.FACTORY.makeFilled(size, size, SparseRandomDistribution.of(0, 100, 0.8)))
      .setB(Primitive64Store.FACTORY.makeFilled(size, 1, Uniform.of(0, 100)));
    System.out.println(solver.a);
    var start = System.currentTimeMillis();
    var success = true;
    try {
      MatrixStore<Double> result = solver.solve();
      var multiplicationResult = solver.a.multiply(result);
      System.out.println("Diff between result and multiplication");
      double maxDiff = 0;
      for (int i = 0; i < size; i++) {
        var resultRowI = solver.b.get(i, 0);
        var multiRowI = multiplicationResult.get(i, 0);
        maxDiff = Math.max(maxDiff, Math.abs(resultRowI - multiRowI));
      }
      System.out.printf("Max diff: %16.12f%n", maxDiff);
      success = maxDiff < 0.1e-6;
    } catch (SolvingFailedException e) {
      success = false;
      System.err.println(e.getMessage());
    } finally {
      var end = System.currentTimeMillis();
      System.out.printf("%5d : %8d ms. Success: %b%n", size, end - start, success);
    }
  }

  public SolverOJA setDimensions(int numberOfEquations, int numberOfVariables) {
    this.numberOfEquations = numberOfEquations;
    this.numberOfVariables = numberOfVariables;
    return this;
  }

  public SolverOJA setA(MatrixStore<Double> a) {
    if (this.numberOfEquations == 0) this.numberOfEquations = a.getRowDim();
    if (this.numberOfVariables == 0) this.numberOfVariables = a.getColDim();
    if (this.numberOfEquations != a.getRowDim() || this.numberOfVariables != a.getColDim()) {
      throw new IllegalArgumentException("Matrix doesnt match required dimensions");
    }
    this.a = a;
    return this;
  }

  public SolverOJA setB(MatrixStore<Double> b) {
    if (this.numberOfEquations == 0) this.numberOfEquations = b.getRowDim();
    if (this.numberOfEquations != b.getRowDim() || b.getColDim() != 1) {
      throw new IllegalArgumentException("Vector doesnt match required dimensions");
    }
    this.b = b;
    return this;
  }

  public MatrixStore<Double> solve() throws SolvingFailedException {
    final var solver = LU.R064.make(a);
    try {
      return solver.solve(a, b);
    } catch (RecoverableCondition e) {
      throw new SolvingFailedException("Not solvable solution");
    }
  }
}
