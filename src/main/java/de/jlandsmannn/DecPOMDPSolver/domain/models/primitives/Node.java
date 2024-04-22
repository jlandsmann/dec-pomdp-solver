package de.jlandsmannn.DecPOMDPSolver.domain.models.primitives;

public record Node(String name) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            return name.equals(((Node) obj).name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Node " + name;
    }
}

