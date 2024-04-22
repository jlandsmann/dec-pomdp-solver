package de.jlandsmannn.DecPOMDPSolver.domain.models.primitives;

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
}
