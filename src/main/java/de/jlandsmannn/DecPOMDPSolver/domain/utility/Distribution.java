package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionEmptyException;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionSumNotOneException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Distribution<T> implements Iterable<T> {
  private static final double ROUNDING_ERROR_THRESHOLD = 1e-14;
  private final Map<T, Double> distribution;
  private final T currentMax;

  /**
   * This constructor can be used to create a distribution with probabilities based on distribution
   *
   * @param distribution A non-empty map of distributions, where the values must sum up to 1
   * @throws DistributionEmptyException     is thrown if distribution is empty
   * @throws DistributionSumNotOneException is thrown if sum of values in distribution is not one
   */
  protected Distribution(Map<T, Double> distribution) {
    this.distribution = new ConcurrentHashMap<>(distribution);
    removeObsoleteKeys();
    validateDistribution();
    this.currentMax = calculateMax();
  }

  public static <T> Distribution<T> createRandomDistribution(Collection<T> entries) {
    if (entries.size() == 1) {
      return Distribution.createSingleEntryDistribution(entries.stream().findFirst().get());
    } else if (entries.isEmpty()) {
      throw new IllegalArgumentException("Collection must not be empty");
    }
    try {
      var random = new Random();
      var distribution = new HashMap<T, Double>();
      var total = 0.0;
      for (T entry : entries) {
        var probability = random.nextDouble(0D, 1D);
        distribution.put(entry, probability);
        total += probability;
      }
      for (T entry : entries) {
        var probability = distribution.get(entry);
        var newProbability = probability / total;
        distribution.put(entry, newProbability);
      }
      return new Distribution<>(distribution);
    } catch (DistributionEmptyException | DistributionSumNotOneException e) {
      throw new IllegalStateException("Sum of distributions not one", e);
    }
  }

  public static <T> Distribution<T> createUniformDistribution(Collection<T> entries) {
    var entriesAsSet = Set.copyOf(entries);
    try {
      var distribution = entriesAsSet.stream()
        .map(e -> Map.entry(e, 1D / entriesAsSet.size()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      return new Distribution<>(distribution);
    } catch (DistributionEmptyException | DistributionSumNotOneException e) {
      throw new IllegalStateException("Sum of distributions not one", e);
    }
  }

  public static <T> Distribution<T> createSingleEntryDistribution(T entry) {
    try {
      return new Distribution<>(Map.of(entry, 1D));
    } catch (DistributionEmptyException | DistributionSumNotOneException e) {
      throw new IllegalStateException(e);
    }
  }

  public static <T> Distribution<T> createWeightedDistribution(Map<Distribution<T>, Double> distributionOfDistributions) {
    Map<T, Double> probabilities = new HashMap<>();
    for (var distribution : distributionOfDistributions.keySet()) {
      var probability = distributionOfDistributions.getOrDefault(distribution, 0D);
      if (probability == 0) continue;

      for (var entry : distribution.entrySet()) {
        var currentProbability = probabilities.getOrDefault(entry.getKey(), 0D);
        var scaledProbability = entry.getValue() * probability;
        probabilities.put(entry.getKey(), currentProbability + scaledProbability);
      }
    }
    try {
      return Distribution.of(probabilities);
    } catch (DistributionEmptyException e) {
      throw new IllegalStateException("Distribution is empty, but shouldn't be.", e);
    } catch (DistributionSumNotOneException e) {
      throw new IllegalStateException("Distribution's sum is not 1, but should be.", e);
    }
  }

  public static <T> Distribution<T> of(Map<T, Double> distribution) {
    return new Distribution<>(distribution);
  }

  public int size() {
    return distribution.size();
  }

  public T getMax() {
    return currentMax;
  }

  public Set<T> keySet() {
    return distribution.keySet();
  }

  public Set<Map.Entry<T, Double>> entrySet() {
    return distribution.entrySet();
  }

  public Double getProbability(T item) {
    return distribution.getOrDefault(item, 0D);
  }

  public T getRandom() {
    var rand = Math.random();
    for (var entry : this.distribution.entrySet()) {
      rand -= entry.getValue();
      if (rand <= 0) return entry.getKey();
    }
    throw new IllegalStateException();
  }

  public void removeEntry(T nodeToPrune) throws DistributionEmptyException {
    if (!distribution.containsKey(nodeToPrune)) return;
    distribution.remove(nodeToPrune);
    validateDistribution();
    calculateMax();
  }

  public void replaceEntryWithDistribution(T item, Distribution<T> replacement) {
    var probabilityOfItemToReplace = getProbability(item);
    if (probabilityOfItemToReplace <= 0) return;
    if (replacement.getProbability(item) > 0)
      throw new IllegalStateException("Replacement distribution cant contain item to replace.");
    distribution.remove(item);
    for (var entry : replacement.entrySet()) {
      var currentProbability = getProbability(entry.getKey());
      var probabilityOfReplacementEntry = entry.getValue();
      distribution.put(entry.getKey(), currentProbability + (probabilityOfItemToReplace * probabilityOfReplacementEntry));
    }
    validateDistribution();
    calculateMax();
  }

  public Map<T, Double> toMap() {
    return Map.copyOf(distribution);
  }

  @Override
  public Iterator<T> iterator() {
    return keySet().iterator();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Distribution<?>) {
      return distribution.equals(((Distribution<?>) obj).distribution);
    }
    return super.equals(obj);
  }

  @Override
  public String toString() {
    return distribution.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash("Distribution", distribution);
  }


  private void removeObsoleteKeys() {
    distribution.forEach((key, probability) -> {
      if (probability == 0D) distribution.remove(key);
    });
  }

  private void validateDistribution() throws DistributionEmptyException, DistributionSumNotOneException {
    if (distribution.isEmpty()) {
      throw new DistributionEmptyException();
    }
    var sumOfDistributions = distribution.values().stream().reduce(Double::sum).orElse(0D);
    if (Math.abs(1D - sumOfDistributions) > ROUNDING_ERROR_THRESHOLD) {
      throw new DistributionSumNotOneException(sumOfDistributions);
    }
  }

  private T calculateMax() {
    var entryStream = distribution.entrySet().stream();
    var maxEntry = entryStream.max(Comparator.comparingDouble(Map.Entry::getValue)).orElseThrow(IllegalStateException::new);
    return maxEntry.getKey();
  }

  public boolean closeTo(Distribution<T> other, double tolerance) {
    return distribution.keySet()
      .stream()
      .map(key -> other.getProbability(key) - getProbability(key))
      .map(Math::abs)
      .allMatch(diff -> diff <= tolerance);
  }
}
