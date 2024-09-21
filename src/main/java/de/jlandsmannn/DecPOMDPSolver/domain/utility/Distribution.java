package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionEmptyException;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.exceptions.DistributionSumNotOneException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A (probability) distribution is a rich-feature map,
 * where each element has a probability given.
 * The sum of probabilities in this distribution has to be equals 1.
 *
 * @param <T> the data type of the elements
 */
public class Distribution<T> implements Iterable<T> {
  private static final double ROUNDING_ERROR_THRESHOLD = 1e-6;
  private final Map<T, Double> distribution;
  private final T currentMax;

  /**
   * This constructor can be used to create a distribution with probabilities based on
   * a map where the keys are the element and the values are their probabilities.
   *
   * @param distribution A non-empty map of distributions, where the values must sum up to 1
   * @throws DistributionEmptyException     is thrown if distribution is empty
   * @throws DistributionSumNotOneException is thrown if sum of values in distribution is not one
   */
  protected Distribution(Map<T, Double> distribution) {
    this.distribution = new ConcurrentHashMap<>(distribution);
    removeObsoleteElements();
    validateDistribution();
    this.currentMax = calculateMax();
  }

  /**
   * This method creates a random distribution with all elements from the given collection.
   *
   * @param entries the elements to include in the distribution
   * @param <T>     the data type of the elements
   * @return a distribution with (pseudo) random probabilities
   */
  public static <T> Distribution<T> createRandomDistribution(Collection<T> entries) {
    return createRandomDistribution(entries, new Random());
  }

  /**
   * This method creates a random distribution with all elements from the given collection.
   * The required generation of random numbers is done by the given random object.
   *
   * @param entries the elements to include in the distribution
   * @param random  the random object used to generate probabilities
   * @param <T>     the data type of the elements
   * @return a distribution with (pseudo) random probabilities
   */
  public static <T> Distribution<T> createRandomDistribution(Collection<T> entries, Random random) {
    if (entries.size() == 1) {
      return Distribution.createSingleEntryDistribution(entries.stream().findFirst().get());
    } else if (entries.isEmpty()) {
      throw new IllegalArgumentException("Collection must not be empty");
    }
    try {
      var distribution = new HashMap<T, Double>();
      var total = 0.0;
      for (T entry : entries) {
        var probability = random.nextDouble(0D, 1D);
        distribution.put(entry, probability);
        total += probability;
      }
      var totalConst = total;
      distribution.replaceAll((k,v) -> v / totalConst);
      return new Distribution<>(distribution);
    } catch (DistributionEmptyException | DistributionSumNotOneException e) {
      throw new IllegalStateException("Sum of distributions not one", e);
    }
  }

  /**
   * This method creates a uniform distribution with all elements from the given collection.
   *
   * @param entries the elements to include in the distribution
   * @param <T>     the data type of the elements
   * @return a distribution with equal probabilities
   */
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

  /**
   * This method creates a distribution with a single entry, which has a probability of 1.
   *
   * @param entry the element to include in the distribution
   * @param <T>   the data type of the element
   * @return a distribution with a single entry
   */
  public static <T> Distribution<T> createSingleEntryDistribution(T entry) {
    try {
      return new Distribution<>(Map.of(entry, 1D));
    } catch (DistributionEmptyException | DistributionSumNotOneException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * This method creates a distribution which combines multiple distributions,
   * weighted by the given probability inside the map.
   *
   * @param distributionOfDistributions the distributions and their weights
   * @param <T>                         the data type of the elements
   * @return a distribution with all entries of all distributions
   */
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

  /**
   * This method creates a distribution with probabilities based on
   * a map where the keys are the element and the values are their probabilities.
   *
   * @param distribution A non-empty map of distributions, where the values must sum up to 1
   * @throws DistributionEmptyException     is thrown if distribution is empty
   * @throws DistributionSumNotOneException is thrown if the sum of values in distribution is not one
   */
  public static <T> Distribution<T> of(Map<T, Double> distribution) {
    return new Distribution<>(distribution);
  }

  public static <K> Distribution<K> normalizeOf(Map<K, Double> map) {
   return of(normalize(map));
  }

  private static <K> Map<K, Double> normalize(Map<K, Double> map) {
    var sumOfProbabilities = map.values().stream().reduce(Double::sum).orElseThrow();
    if (sumOfProbabilities == 0D) throw new IllegalArgumentException("Sum of probabilities is zero, cannot normalize.");
    map.replaceAll((k, v) -> v / sumOfProbabilities);
    return map;
  }

  /**
   * This method returns the number of elements
   * that have probability which is larger than 0.
   *
   * @return the number of elements with non-zero probability
   */
  public int size() {
    return distribution.size();
  }

  /**
   * This method returns one of the elements
   * with the highest probability.
   *
   * @return the element with the highest probability
   */
  public T getMax() {
    return currentMax;
  }

  /**
   * This method returns a set containing all elements
   * that have probability which is larger than 0.
   *
   * @return all elements with non-zero probability
   */
  public Set<T> keySet() {
    return distribution.keySet();
  }

  /**
   * This method returns a set containing all entries
   * where the key is the element, and the value is the probability
   * that have probability which is larger than 0.
   *
   * @return all entries with non-zero probability
   */
  public Set<Map.Entry<T, Double>> entrySet() {
    return distribution.entrySet();
  }

  /**
   * Returns the probability of the given item.
   * If the item is not part of the distribution, 0 is returned.
   *
   * @param item the item to check for
   * @return the probability of the item
   */
  public double getProbability(T item) {
    return distribution.getOrDefault(item, 0D);
  }


  /**
   * Selects an element (pseudo) randomly weighted by their probabilities.
   *
   * @return a pseudo-random element from the distribution
   */
  public T getRandom() {
    return getRandom(new Random());
  }

  /**
   * Selects an element (pseudo) randomly weighted by their probabilities.
   *
   * @param seed A seed for a {@link Random} instance for selection
   * @return a pseudo-random element from the distribution
   */
  public T getRandom(long seed) {
    var random = new Random();
    if (seed != 0) random.setSeed(seed);
    return getRandom(random);
  }

  /**
   * Selects an element (pseudo) randomly weighted by their probabilities.
   *
   * @param random A {@link Random} object to select an element
   * @return a pseudo-random element from the distribution
   */
  public T getRandom(Random random) {
    var rand = random.nextDouble(0, 1);
    for (var entry : this.distribution.entrySet()) {
      rand -= entry.getValue();
      if (rand <= 0) return entry.getKey();
    }
    throw new IllegalStateException();
  }

  /**
   * Removes an element from the distribution and replaces its probability with given distribution.
   *
   * @param element     the element to replace
   * @param replacement the distribution to weight the elements to replace the element with
   */
  public void replaceEntryWithDistribution(T element, Distribution<T> replacement) {
    var probabilityOfItemToReplace = getProbability(element);
    if (probabilityOfItemToReplace <= 0) return;
    if (replacement.getProbability(element) > 0)
      throw new IllegalStateException("Replacement distribution cant contain element to replace.");
    distribution.remove(element);
    for (var entry : replacement.entrySet()) {
      var currentProbability = getProbability(entry.getKey());
      var probabilityOfReplacementEntry = entry.getValue();
      distribution.put(entry.getKey(), currentProbability + (probabilityOfItemToReplace * probabilityOfReplacementEntry));
    }
    validateDistribution();
    calculateMax();
  }

  /**
   * Returns a readonly map where the keys are the elements
   * and the values are their probabilities.
   *
   * @return readonly map of the distribution's elements
   */
  public Map<T, Double> toMap() {
    return Map.copyOf(distribution);
  }

  @Override
  public Iterator<T> iterator() {
    return keySet().iterator();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    return distribution.equals(((Distribution<?>) obj).distribution);
  }

  @Override
  public String toString() {
    return distribution.toString();
  }

  @Override
  public int hashCode() {
    return Objects.hash("Distribution", distribution);
  }

  /**
   * Removes elements from this distribution
   * with a probability that is not positive.
   */
  private void removeObsoleteElements() {
    distribution.forEach((key, probability) -> {
      if (probability <= 0D) distribution.remove(key);
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
      .max(Double::compareTo)
      .map(maxDistance -> maxDistance < tolerance)
      .orElseThrow();
  }
}
