package de.jlandsmannn.DecPOMDPSolver.domain.models.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Vector<T> {
    private final ArrayList<T> values;

    public Vector(Collection<T> values) {
        this.values = new ArrayList<>(values);
        if (this.values.isEmpty()) {
            throw new IllegalArgumentException("Vector is empty");
        }
    }

    public T get(int index) {
        return values.get(index);
    }

    public int getSize() {
        return values.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector<?>) {
            return values.equals(((Vector<?>) obj).values);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            T value = values.get(i);
            sb.append(value);
            if (i+1 < values.size()) sb.append(", ");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash("Vector", values);
    }
}
