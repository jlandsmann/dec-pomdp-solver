package de.jlandsmannn.DecPOMDPSolver.equationSystems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.ojalgo.matrix.store.MatrixStore;
import org.ojalgo.matrix.store.Primitive64Store;
import org.ojalgo.random.Uniform;

import static org.junit.jupiter.api.Assertions.*;

class OJAEquationSystemSolverTest {

  OJAEquationSystemSolver solver;

  @BeforeEach
  void setUp() {
    solver = new OJAEquationSystemSolver();
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -2})
  void setDimension_ShouldThrowIfRowsIsSmallerThan1(int rows) {
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setDimensions(rows, 1);
    });
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -2})
  void setDimension_ShouldThrowIfColsIsSmallerThan1(int cols) {
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setDimensions(1, cols);
    });
  }

  @ParameterizedTest
  @CsvSource(value = {"1,1", "1,2", "2,1", "3,3", "4,8", "8,4"})
  void setMatrix_ShouldThrowErrorIfMatrixHasOtherDimensionIfDefinedPreviously(int rows, int columns) {
    solver.setDimensions(rows, columns);
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setMatrix(generateRandomMatrix(rows + 1, columns));
    }, "should throw error if matrix has more rows than expected");
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setMatrix(generateRandomMatrix(rows - 1, columns));
    }, "should throw error if matrix has less rows than expected");
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setMatrix(generateRandomMatrix(rows, columns + 1));
    }, "should throw error if matrix has more cols than expected");
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setMatrix(generateRandomMatrix(rows, columns - 1));
    }, "should throw error if matrix has less cols than expected");
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 7, 13, 16, 21})
  void setMatrix_ShouldThrowIfMatrixHasNoRows(int columns) {
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setMatrix(generateRandomMatrix(0, columns));
    });
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 2, 3, 7, 13, 16, 21})
  void setMatrix_ShouldThrowIfMatrixHasNoCols(int rows) {
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setMatrix(generateRandomMatrix(rows, 0));
    });
  }

  @ParameterizedTest
  @CsvSource(value = {"1,1", "1,2", "2,1", "8,8", "5,3", "3,7"})
  void setVector_shouldThrowErrorIfVectorHasOtherDimensionIfDefinedPreviously(int rows, int columns) {
    solver.setDimensions(rows, columns);
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setVector(generateRandomMatrix(rows + 1, 1));
    }, "should throw error if matrix has more rows than expected");
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setVector(generateRandomMatrix(rows - 1, 1));
    }, "should throw error if matrix has less rows than expected");
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setVector(generateRandomMatrix(rows, 2));
    }, "should throw error if matrix has more cols than expected");
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setVector(generateRandomMatrix(rows, 0));
    }, "should throw error if matrix has less cols than expected");
  }

  @Test
  void setVector_ShouldThrowIfVectorHasNoRows() {
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setVector(generateRandomMatrix(0, 1));
    });
  }

  @Test
  void setVector_ShouldThrowIfVectorHasNoCols() {
    assertThrows(IllegalArgumentException.class, () -> {
      solver.setVector(generateRandomMatrix(4, 0));
    });
  }

  @Test
  void solve_ShouldReturnCorrectResultForGivenEquationSystem() {
    var matrix = Primitive64Store.FACTORY.make(4, 4);
    var vector = Primitive64Store.FACTORY.make(4, 1);
    // a + b + c + d = 13.2
    matrix.set(0, 0, 1);
    matrix.set(0, 1, 1);
    matrix.set(0, 2, 1);
    matrix.set(0, 3, 1);
    vector.set(0, 0, 13.2);
    // 2a + b + d = 17.8
    matrix.set(1, 0, 2);
    matrix.set(1, 1, 1);
    matrix.set(1, 2, 0);
    matrix.set(1, 3, 1);
    vector.set(1, 0, 17.8);
    // a + 3b + c + 5d = 28.4
    matrix.set(2, 0, 1);
    matrix.set(2, 1, 3);
    matrix.set(2, 2, 1);
    matrix.set(2, 3, 5);
    vector.set(2, 0, 28.4);
    // 2b + 3c + 6d = 16
    matrix.set(3, 0, 0);
    matrix.set(3, 1, 2);
    matrix.set(3, 2, 3);
    matrix.set(3, 3, 6);
    vector.set(3, 0, 16);

    var result = solver.setMatrix(matrix).setVector(vector).solve();
    assertTrue(result.isPresent());

    var resultVector = result.get();
    // a = 5, b = 8, c = 0.4, d = -0.2
    // for some reason, the result diverges at a factor of less than 10^-7
    assertEquals(resultVector.get(0, 0), 5, 1e-7);
    assertEquals(resultVector.get(1, 0), 8, 1e-7);
    assertEquals(resultVector.get(2, 0), 0.4, 1e-7);
    assertEquals(resultVector.get(3, 0), -0.2, 1e-7);

  }

  @Test
  void solve_ShouldReturnNoResultForNonSolvableEquationSystem() {
    var matrix = Primitive64Store.FACTORY.make(3, 3);
    var vector = Primitive64Store.FACTORY.make(3, 1);
    // x + y = 0
    matrix.set(0, 0, 1);
    matrix.set(0, 1, 1);
    matrix.set(0, 2, 0);
    vector.set(0, 0, 0);
    // x = 17.8
    matrix.set(1, 0, 1);
    matrix.set(1, 1, 0);
    matrix.set(1, 2, 0);
    vector.set(1, 0, 17.8);
    // y = -3
    matrix.set(2, 0, 0);
    matrix.set(2, 1, 1);
    matrix.set(2, 2, 0);
    vector.set(2, 0, -3);

    var result = solver.setMatrix(matrix).setVector(vector).solve();
    assertTrue(result.isEmpty());
  }

  private MatrixStore<Double> generateRandomMatrix(int rows, int columns) {
    return Primitive64Store.FACTORY.makeFilled(rows, columns, Uniform.of(-10, 10));
  }
}