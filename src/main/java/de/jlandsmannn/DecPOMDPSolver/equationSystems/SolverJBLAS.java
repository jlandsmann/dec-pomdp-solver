package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.equationSystems.exceptions.SolvingFailedException;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;

public class SolverJBLAS {
    private DoubleMatrix a;
    private DoubleMatrix b;
    private int numberOfEquations = 0;
    private int numberOfVariables = 0;

    public static void main(String[] args) {
        test();
    }

    public static void benchmark(int numberOfVariables) {
        SolverJBLAS solver = new SolverJBLAS()
                .setDimensions(numberOfVariables, numberOfVariables)
                .setA(DoubleMatrix.rand(numberOfVariables, numberOfVariables))
                .setB(DoubleMatrix.rand(numberOfVariables, 1));
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
        var size = 2000;
        SolverJBLAS solver = new SolverJBLAS()
                .setDimensions(size, size)
                .setA(DoubleMatrix.rand(size, size))
                .setB(DoubleMatrix.rand(size, 1));
        var start = System.currentTimeMillis();
        var success = true;
        try {
            var result = solver.solve();
            var multiplicationResult = solver.a.mmul(result);
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

    public SolverJBLAS setDimensions(int numberOfEquations, int numberOfVariables) {
        this.numberOfEquations = numberOfEquations;
        this.numberOfVariables = numberOfVariables;
        return this;
    }

    public SolverJBLAS setA(DoubleMatrix a) {
        if (this.numberOfEquations == 0) this.numberOfEquations = a.rows;
        if (this.numberOfVariables == 0) this.numberOfVariables = a.columns;
        if (this.numberOfEquations != a.rows || this.numberOfVariables != a.columns) {
            throw new IllegalArgumentException("Matrix doesnt match required dimensions");
        }
        this.a = a;
        return this;
    }

    public SolverJBLAS setB(DoubleMatrix b) {
        if (this.numberOfEquations == 0) this.numberOfEquations = b.rows;
        if (this.numberOfEquations != b.rows || b.columns != 1) {
            throw new IllegalArgumentException("Vector doesnt match required dimensions");
        }
        this.b = b;
        return this;
    }

    public DoubleMatrix solve() throws SolvingFailedException {
        try {
            return Solve.solveSymmetric(a, b);
        } catch (Throwable e) {
            throw new SolvingFailedException(e);
        }
    }
}
