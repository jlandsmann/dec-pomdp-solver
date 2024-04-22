package de.jlandsmannn.DecPOMDPSolver.domain.models.primitives;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash("Node", name);
    }
}

