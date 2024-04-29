package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives;

import java.util.Objects;

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

    @Override
    public int hashCode() {
        return Objects.hash("Action", name);
    }
}
