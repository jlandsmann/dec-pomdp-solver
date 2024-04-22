package de.jlandsmannn.DecPOMDPSolver.domain.models.primitives;

import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.Distribution;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.DistributionEmptyException;
import de.jlandsmannn.DecPOMDPSolver.domain.models.utility.DistributionSumNotOneException;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class BeliefState {
    private final Distribution<State> distribution;

    public static BeliefState createUniformDistribution(Set<State> states) {
        return new BeliefState(Distribution.createUniformDistribution(states));
    }

    public static BeliefState createSingleEntryDistribution(String stateString) {
        return BeliefState.createSingleEntryDistribution(new State(stateString));
    }

    public static BeliefState createSingleEntryDistribution(State state) {
        return new BeliefState(Distribution.createSingleEntryDistribution(state));
    }

    public BeliefState(Map<State, Double> distribution) throws DistributionSumNotOneException, DistributionEmptyException {
        this.distribution = new Distribution<>(distribution);
    }

    public BeliefState(Distribution<State> distribution) {
        this.distribution = distribution;
    }

    public int size() {
        return distribution.size();
    }

    public State getMax() {
        return distribution.getMax();
    }

    public Set<State> getEntries() {
        return distribution.getEntries();
    }

    public Double getProbability(State state) {
        return distribution.getProbability(state);
    }

    public State getRandom() {
        return distribution.getRandom();
    }

    @Override
    public boolean equals(Object obj) {
        return distribution.equals(obj);
    }

    @Override
    public String toString() {
        return distribution.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash("BeliefState", distribution);
    }
}
