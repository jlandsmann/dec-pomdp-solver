package de.jlandsmannn.DecPOMDPSolver.domain.lifting;

import de.jlandsmannn.DecPOMDPSolver.DecPOMDPGenerator;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.AgentWithStateController;
import de.jlandsmannn.DecPOMDPSolver.domain.finiteStateController.primitives.Node;
import de.jlandsmannn.DecPOMDPSolver.domain.utility.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class IsomorphicDecPOMDPWithStateControllerTest {

  IsomorphicDecPOMDPWithStateController decPOMDP;

  @BeforeEach
  void setUp() {
    decPOMDP = DecPOMDPGenerator.getIsomorphicDecPOMDP(2, 2);
  }

  @Test
  void getNodeCombinations_ShouldOutputAsManyVectorsAsNodeHistogramCombinations() {
    var expectedNodeCombinationCount = decPOMDP.getAgents().stream()
      .map(agent -> {
        var nodeCount = agent.getControllerNodes().size();
        var partitionSize = agent.getPartitionSize();
        return numberOfHistograms(nodeCount, partitionSize);
      })
      .reduce((a,b) -> a * b)
      .orElseThrow();
    var actualNodeCombinationCount = decPOMDP.getNodeCombinations().size();

    assertEquals(expectedNodeCombinationCount, actualNodeCombinationCount);
  }

  @Test
  void getNodeCombinations_ShouldOutputOnlyDistinctVectors() {
    var expectedNodeCombinationCount = decPOMDP.getNodeCombinations().size();
    var actualNodeCombinationCount = decPOMDP.getNodeCombinations().stream().distinct().count();

    assertEquals(expectedNodeCombinationCount, actualNodeCombinationCount);
  }

  @Test
  void getNodeCombinations_ShouldOutputVectorsWithNodeForEachAgent() {
    var totalAgentCount = decPOMDP.getTotalAgentCount();
    var nodeCombinations = decPOMDP.getNodeCombinations();

    for (var nodeVector : nodeCombinations) {
      assertEquals(totalAgentCount, nodeVector.size());
    }
  }

  @Test
  void getNodeCombinations_ShouldOutputNormalizedVectors() {
    for (var nodeVector : decPOMDP.getNodeCombinations()) {
      var normalizedVector = normalize(nodeVector);
      assertEquals(normalizedVector, nodeVector);
    }
  }

  protected <U> Vector<U> normalize(Vector<U> vector) {
    var rawNormalizedVector = new ArrayList<U>(vector.size());
    var offset = 0;
    for (var agent : decPOMDP.getAgents()) {
      var elements = new ArrayList<>(vector.toList().subList(offset, offset + agent.getPartitionSize()));
      elements.sort(Comparator.comparing(Objects::toString));
      rawNormalizedVector.addAll(elements);
      offset += agent.getPartitionSize();
    }
    return Vector.of(rawNormalizedVector);
  }

  private long numberOfHistograms(long possibleBuckets, long entryCount) {
    return binomCoeff(entryCount + possibleBuckets - 1, possibleBuckets - 1);
  }

  private long binomCoeff(long n, long k) {
    if (n <= 0) throw new IllegalArgumentException("n must be greater than zero");
    else if (k > n) return 0;
    else if (k  == 0) return 1;
    else if (k == 1) return n;
    return factorial(n) / factorial(k) * factorial(n-k);
  }

  public long factorial(long f) {
    int result = 1;
    for (int i = 1; i <= f; i++) {
      result = result * i;
    }
    return result;
  }
}