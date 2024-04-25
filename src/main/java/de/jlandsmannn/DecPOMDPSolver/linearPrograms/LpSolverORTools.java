package de.jlandsmannn.DecPOMDPSolver.linearPrograms;

import com.google.ortools.Loader;
import com.google.ortools.init.OrToolsVersion;
import com.google.ortools.linearsolver.*;
import com.google.ortools.modelbuilder.ModelBuilder;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.ExtensionRegistryLite;

import java.io.*;
import java.util.Scanner;

public final class LpSolverORTools {
    public static void main(String[] args) {
        // [START loader]
        Loader.loadNativeLibraries();
        // [END loader]

        System.out.println("Google OR-Tools version: " + OrToolsVersion.getVersionString());

        // [START solver]
        // Create the linear solver with the GLOP backend.
        MPSolver solver = MPSolver.createSolver("GLOP");
        if (solver == null) {
            System.out.println("Could not create solver GLOP");
            return;
        }
        // [END solver]

        try {
            var file = new FileInputStream("./testprob.mps");
            var input = CodedInputStream.newInstance(file);
            var proto = MPModelProto.parseFrom(input);
            solver.loadModelFromProto(proto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // [START solve]
        System.out.println("Solving with " + solver.solverVersion());
        final MPSolver.ResultStatus resultStatus = solver.solve();
        // [END solve]

        // [START print_solution]
        System.out.println("Status: " + resultStatus);
        if (resultStatus != MPSolver.ResultStatus.OPTIMAL) {
            System.out.println("The problem does not have an optimal solution!");
            if (resultStatus == MPSolver.ResultStatus.FEASIBLE) {
                System.out.println("A potentially suboptimal solution was found");
            } else {
                System.out.println("The solver could not solve the problem.");
                return;
            }
        }

        System.out.println("Solution:");
        System.out.println("Objective value = " + solver.objective().value());
        System.out.println("x = " + solver.variable(0).solutionValue());
        System.out.println("y = " + solver.variable(1).solutionValue());
        System.out.println("z = " + solver.variable(2).solutionValue());
        // [END print_solution]

        // [START advanced]
        System.out.println("Advanced usage:");
        System.out.println("Problem solved in " + solver.wallTime() + " milliseconds");
        System.out.println("Problem solved in " + solver.iterations() + " iterations");
        // [END advanced]
    }

    private LpSolverORTools() {}
}