package de.jlandsmannn.DecPOMDPSolver.domain.models.primitives;

public record Action(String name) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Action) {
            return name.equals(((Action) obj).name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Action " + name;
    }
}
