package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import de.jlandsmannn.DecPOMDPSolver.equationSystems.exceptions.SolvingFailedException;
import org.ojalgo.matrix.decomposition.*;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

public class SolverOJA {
    private MatrixStore<Double> a;
    private MatrixStore<Double> b;
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
        SolverOJA solver = new SolverOJA()
                .setDimensions(numberOfVariables, numberOfVariables)
                .setA(Primitive64Store.FACTORY.makeFilled(numberOfVariables, numberOfVariables, Uniform.of(0, 50)))
                .setB(Primitive64Store.FACTORY.makeFilled(numberOfVariables, 1, Uniform.of(0, 10)));
        var start = System.currentTimeMillis();
        var success = true;
        try {
            MatrixStore<Double> result = solver.solve();
        } catch (SolvingFailedException e) {
            success = false;
        } finally {
            var end = System.currentTimeMillis();
            System.out.printf("%5d : %8d ms. Success: %b%n", numberOfVariables, end - start, success);
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
        final var solver = LDL.R064.make(a);
        final var alloc = solver.preallocate(a, b);
        solver.decompose(a);
        if (solver.isSolvable()) {
            return solver.getSolution(b, alloc);
        }
        throw new SolvingFailedException("Not solvable solution");
    }
}
