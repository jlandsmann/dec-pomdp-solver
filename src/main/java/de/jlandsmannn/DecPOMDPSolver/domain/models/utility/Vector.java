package de.jlandsmannn.DecPOMDPSolver.domain.models.utility;

import java.util.Arrays;

public class Vector<T> {
    private final T[] values;
    private final int size;

    public Vector(T[] values) {
        this.values = values;
        this.size = values.length;
        if (this.size == 0) {
            throw new IllegalArgumentException("Vector is empty");
        }
    }

    public T get(int index) {
        return values[index];
    }

    public int getSize() {
        return size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector<?>) {
            return Arrays.equals(values, ((Vector<?>) obj).values);
        }
        return super.equals(obj);
    }
}
