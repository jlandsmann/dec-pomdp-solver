package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.equationSystems.exceptions.SolvingFailedException;
import org.ejml.data.DMatrixRMaj;
import org.ejml.data.Matrix;
import org.ejml.dense.row.factory.LinearSolverFactory_DDRM;
import org.ejml.interfaces.linsol.LinearSolver;
import org.ejml.simple.SimpleMatrix;

import java.util.Random;

public class SolverEJML {
    private DMatrixRMaj a;
    private DMatrixRMaj b;
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
        var random = new Random();
        SolverEJML solver = new SolverEJML()
                .setDimensions(numberOfVariables, numberOfVariables)
                .setA(SimpleMatrix.random_DDRM(numberOfVariables, numberOfVariables, 0, 100, random).getDDRM())
                .setB(SimpleMatrix.random_DDRM(numberOfVariables, 1, 0, 50, random).getDDRM());
        var start = System.currentTimeMillis();
        var success = true;
        try {
            Matrix result = solver.solve();
            // System.out.println("Result exists with " + result.getNumCols() + " columns and " + result.getNumRows() + " rows");
        } catch (SolvingFailedException e) {
            // System.err.println(e.getMessage());
            success = false;
        } finally {
            var end = System.currentTimeMillis();
            System.out.printf("%5d : %8d ms. Success: %b%n", numberOfVariables, end - start, success);
        }
    }

    public SolverEJML setDimensions(int numberOfEquations, int numberOfVariables) {
        this.numberOfEquations = numberOfEquations;
        this.numberOfVariables = numberOfVariables;
        return this;
    }

    public SolverEJML setA(DMatrixRMaj a) {
        if (this.numberOfEquations == 0) this.numberOfEquations = a.getNumRows();
        if (this.numberOfVariables == 0) this.numberOfVariables = a.getNumCols();
        if (this.numberOfEquations != a.getNumRows() || this.numberOfVariables != a.getNumCols()) {
            throw new IllegalArgumentException("Matrix doesnt match required dimensions");
        }
        this.a = a;
        return this;
    }

    public SolverEJML setB(DMatrixRMaj b) {
        if (this.numberOfEquations == 0) this.numberOfEquations = b.getNumRows();
        if (this.numberOfEquations != b.getNumRows() || b.getNumCols() != 1) {
            throw new IllegalArgumentException("Vector doesnt match required dimensions");
        }
        this.b = b;
        return this;
    }

    public Matrix solve() throws SolvingFailedException {
        try {
            LinearSolver<DMatrixRMaj, DMatrixRMaj> solver = LinearSolverFactory_DDRM.lu(numberOfEquations);
            DMatrixRMaj result = new DMatrixRMaj(numberOfVariables, 1);
            solver.setA(a);
            solver.solve(b, result);
            return result;
        } catch (Throwable t) {
            throw new SolvingFailedException(t);
        }
    }
}
