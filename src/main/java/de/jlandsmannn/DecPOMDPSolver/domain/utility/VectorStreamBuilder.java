package de.jlandsmannn.DecPOMDPSolver.domain.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class VectorStreamBuilder<T> {

    private List<List<T>> possibleValues;
    private final List<Long> takeNthElement = new ArrayList<>();

    public Stream<Vector<T>> getStreamForEachCombination(Collection<? extends Collection<T>> pPossibleValues) {
        possibleValues = new ArrayList<>(pPossibleValues.stream().map(ArrayList::new).toList());
        int size = possibleValues.size();
        if (size == 0) return Stream.empty();
        long numberOfCombinations = possibleValues.stream()
                .map(Collection::size)
                .mapToLong(Integer::longValue)
                .reduce(Math::multiplyExact)
                .orElse(0);
        if (numberOfCombinations == 0) return Stream.empty();

        takeNthElement.add(1L);
        var scannedItems = 1L;
        for (int i = possibleValues.size() - 2; i >= 0; i--) {
            var collectionSize = possibleValues.get(i + 1).size();
            scannedItems *= collectionSize;
            takeNthElement.addFirst(scannedItems);
        }
        AtomicLong idx = new AtomicLong();
        return Stream
                .generate(() -> iterate(idx.getAndIncrement()))
                .limit(numberOfCombinations);
    }

    private Vector<T> iterate(long idx) {
        var list = new ArrayList<T>();
        for (int i = 0; i < possibleValues.size(); i++) {
            var collection = possibleValues.get(i);
            var takeNthElement = this.takeNthElement.get(i);
            var idxToSelect = (int) Math.floorDiv(idx, takeNthElement) % collection.size();
            list.add(collection.get(idxToSelect));
        }
        return new Vector<>(list);
    }
}
