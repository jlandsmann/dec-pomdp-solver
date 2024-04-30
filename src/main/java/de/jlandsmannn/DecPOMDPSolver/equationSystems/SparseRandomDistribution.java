package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import org.ojalgo.function.NullaryFunction;

import java.util.Random;

public class SparseRandomDistribution implements NullaryFunction<Double> {

  private final Random random = new Random();
  private final double origin;
  private final double bound;
  private final double zeroPercentage;

  SparseRandomDistribution() {
    this(0, 1, 0.2);
  }

  SparseRandomDistribution(double origin, double bound, double zeroPercentage) {
    this.origin = origin;
    this.bound = bound;
    this.zeroPercentage = zeroPercentage;
  }

  public static SparseRandomDistribution standard() {
    return new SparseRandomDistribution();
  }

  public static SparseRandomDistribution of(double origin, double bound, double zeroPercentage) {
    return new SparseRandomDistribution(origin, bound, zeroPercentage);
  }

  @Override
  public double doubleValue() {
    return invoke();
  }

  @Override
  public Double invoke() {
    if (random.nextDouble() <= zeroPercentage) {
      return 0D;
    }
    return random.nextDouble(origin, bound);
  }
}
