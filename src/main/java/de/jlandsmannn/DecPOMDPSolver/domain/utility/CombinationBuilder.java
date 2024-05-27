package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public abstract class CombinationBuilder<C, T> {

  protected List<C> getListForEachCombination(List<? extends List<T>> pPossibleValues) {
    return getStreamForEachCombination(pPossibleValues).toList();
  }

  protected Stream<C> getStreamForEachCombination(List<? extends List<T>> pPossibleValues) {
    List<List<T>> possibleValues = pPossibleValues.stream().map(ArrayList::new).collect(toCollection(ArrayList::new));
    int size = possibleValues.size();
    if (size == 0) return Stream.empty();
    long numberOfCombinations = possibleValues.stream()
      .map(Collection::size)
      .mapToLong(Integer::longValue)
      .reduce(Math::multiplyExact)
      .orElse(0);
    if (numberOfCombinations == 0) return Stream.empty();
    List<Long> takeNthElement = new ArrayList<>();

    takeNthElement.add(1L);
    var scannedItems = 1L;
    for (int i = possibleValues.size() - 2; i >= 0; i--) {
      var collectionSize = possibleValues.get(i + 1).size();
      scannedItems *= collectionSize;
      takeNthElement.addFirst(scannedItems);
    }
    AtomicLong idx = new AtomicLong();
    return Stream
      .generate(() -> iterate(possibleValues, takeNthElement, idx.getAndIncrement()))
      .sequential()
      .limit(numberOfCombinations);
  }

  protected C iterate(List<List<T>> possibleValues, List<Long> takeNthElement, long idx) {
    var list = new ArrayList<T>();
    for (int i = 0; i < possibleValues.size(); i++) {
      var collection = possibleValues.get(i);
      var elementToTake = takeNthElement.get(i);
      var idxToSelect = (int) Math.floorDiv(idx, elementToTake) % collection.size();
      list.add(collection.get(idxToSelect));
    }
    return transformListToCombination(list);
  }

  protected abstract C transformListToCombination(List<T> combinationAsList);
}
