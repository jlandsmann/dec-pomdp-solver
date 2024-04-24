package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.equationSystems.exceptions.SolvingFailedException;
import org.ejml.simple.SimpleMatrix;

public class Solver {
    private SimpleMatrix a;
    private SimpleMatrix b;
    private int numberOfEquations = 0;
    private int numberOfVariables = 0;

    public static void main(String[] args) {
        benchmark(100);
        benchmark(500);
        benchmark(1_000);
        benchmark(2_000);
        benchmark(4_000);
        benchmark(6_000);
        benchmark(8_000);
        benchmark(10_000);
        benchmark(12_000);
        benchmark(14_000);
    }

    public static void benchmark(int numberOfVariables) {
        Solver solver = new Solver()
                .setDimensions(numberOfVariables, numberOfVariables)
                .setA(SimpleMatrix.random(numberOfVariables, numberOfVariables))
                .setB(SimpleMatrix.random(numberOfVariables, 1));
        var start = System.currentTimeMillis();
        var success = true;
        try {
            SimpleMatrix result = solver.solve();
            // System.out.println("Result exists with " + result.getNumCols() + " columns and " + result.getNumRows() + " rows");
        } catch (SolvingFailedException e) {
            // System.err.println(e.getMessage());
            success = false;
        } finally {
            var end = System.currentTimeMillis();
            System.out.printf("%5d : %8d ms. Success: %b%n", numberOfVariables, end - start, success);
        }
    }

    public Solver setDimensions(int numberOfEquations, int numberOfVariables) {
        this.numberOfEquations = numberOfEquations;
        this.numberOfVariables = numberOfVariables;
        return this;
    }

    public Solver setA(SimpleMatrix a) {
        if (this.numberOfEquations == 0) this.numberOfEquations = a.getNumRows();
        if (this.numberOfVariables == 0) this.numberOfVariables = a.getNumCols();
        if (this.numberOfEquations != a.getNumRows() || this.numberOfVariables != a.getNumCols()) {
            throw new IllegalArgumentException("Matrix doesnt match required dimensions");
        }
        this.a = a;
        return this;
    }

    public Solver setB(SimpleMatrix b) {
        if (this.numberOfEquations == 0) this.numberOfEquations = b.getNumRows();
        if (this.numberOfEquations != b.getNumRows() || b.getNumCols() != 1) {
            throw new IllegalArgumentException("Vector doesnt match required dimensions");
        }
        this.b = b;
        return this;
    }

    public SimpleMatrix solve() throws SolvingFailedException {
        try {
            return a.solve(b);
        } catch (Throwable t) {
            throw new SolvingFailedException(t);
        }
    }
}
