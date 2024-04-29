package de.jlandsmannn.DecPOMDPSolver.domain.decpomdp.primitives;

import java.util.Objects;

public record State(String name) {
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof State) {
            return name.equals(((State) obj).name);
        }
        return false;
    }

    @Override
    public String toString() {
        return "State " + name;
    }

    @Override
    public int hashCode() {
        return Objects.hash("State", name);
    }
}
