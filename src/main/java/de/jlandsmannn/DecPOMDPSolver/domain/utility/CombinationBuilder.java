package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is class can be used to create a list or a stream
 * of all possible combinations based on a list of list.
 * "All possible combinations" means here that each element of each list
 * will be combined with each other element of the other lists once.
 *
 * @param <C> the data type of the combination
 * @param <T> the data type of the items within the combinations
 */
public abstract class CombinationBuilder<C, T> implements Collector<List<T>, List<List<T>>, Stream<C>> {

  /**
   * This method creates a list of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a list with all combinations
   */
  protected List<C> getListForEachCombination(List<? extends List<T>> possibleValues) {
    return getStreamForEachCombination(possibleValues).toList();
  }

  /**
   * This method creates a set of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a set with all combinations
   */
  protected Set<C> getSetForEachCombination(List<? extends List<T>> possibleValues) {
    return getStreamForEachCombination(possibleValues).collect(Collectors.toSet());
  }

  /**
   * This method creates a stream of all combinations from the given list of lists.
   *
   * @param possibleValues a list of lists of items
   * @return a stream with all combinations
   */
  protected Stream<C> getStreamForEachCombination(List<? extends List<T>> possibleValues) {
    if (possibleValues.isEmpty()) return Stream.empty();
    long numberOfCombinations = possibleValues.stream()
      .map(Collection::size)
      .mapToLong(Integer::longValue)
      .reduce(Math::multiplyExact)
      .orElse(0);
    if (numberOfCombinations == 0) return Stream.empty();

    List<Long> takeNthElement = new ArrayList<>(List.of(1L));
    var scannedItems = 1L;
    for (int i = possibleValues.size() - 1; 1 <= i; i--) {
      var collectionSize = possibleValues.get(i).size();
      scannedItems *= collectionSize;
      takeNthElement.add(0, scannedItems);
    }
    AtomicLong idx = new AtomicLong(0);
    return Stream
      .generate(() -> iterate(possibleValues, takeNthElement, idx.getAndIncrement()))
      .sequential()
      .limit(numberOfCombinations);
  }

  /**
   * This method creates a specific combination based on the given index.
   *
   * @param possibleValues a list of lists of items
   * @param takeNthElement a list of combinations to skip before heading to the next element of the regarding list
   * @param idx            the idx identifying which combination should be calculated
   * @return the combination for the given index
   */
  protected C iterate(List<? extends List<T>> possibleValues, List<Long> takeNthElement, long idx) {
    var list = new ArrayList<T>();
    for (int i = 0; i < possibleValues.size(); i++) {
      var collection = possibleValues.get(i);
      var elementToTake = takeNthElement.get(i);
      var idxToSelect = (int) Math.floorDiv(idx, elementToTake) % collection.size();
      list.add(collection.get(idxToSelect));
    }
    return transformListToCombination(list);
  }

  /**
   * This abstract method transforms a list of elements to the intended combination data type.
   *
   * @param combinationAsList the combination as a list
   * @return the combination as the specified data type
   */
  protected abstract C transformListToCombination(List<T> combinationAsList);

  @Override
  public Supplier<List<List<T>>> supplier() {
    return ArrayList::new;
  }

  @Override
  public BiConsumer<List<List<T>>, List<T>> accumulator() {
    return List::add;
  }

  @Override
  public BinaryOperator<List<List<T>>> combiner() {
    return (listA, listB) -> {
      listA.addAll(listB);
      return listA;
    };
  }

  @Override
  public Function<List<List<T>>, Stream<C>> finisher() {
    return this::getStreamForEachCombination;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Set.of();
  }
}
