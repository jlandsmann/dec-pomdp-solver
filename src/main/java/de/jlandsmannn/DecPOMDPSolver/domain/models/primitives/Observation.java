package de.jlandsmannn.DecPOMDPSolver.domain.models.primitives;

import java.util.Objects;

public record Observation(String name) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Observation) {
            return name.equals(((Observation) obj).name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Observation " + name;
    }

    @Override
    public int hashCode() {
        return Objects.hash("Observation", name);
    }
}
