package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class DiscountParserTest {

  private DiscountParser parser;

  @BeforeEach
  void setUp() {
    parser = new DiscountParser();
  }

  @ParameterizedTest
  @ValueSource(doubles = {0, 0.1, 0.25, 0.5, 0.9, 0.99, 1.0})
  void parseDiscount_ShouldSetDiscountFactor(double discountFactor) {
    var section = "discount: " + discountFactor;
    parser.parseDiscount(section);
    var expectedDiscount = discountFactor;
    var actualDiscount = parser.getDiscountFactor();
    assertEquals(expectedDiscount, actualDiscount, 0.0);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "discounts: 1.0",
    "discount: 2",
    "discount: -0.1",
    "discount: -0.2",
    "discount: 1.1"
  })
  void parseDiscount_ShouldThrowIfInvalidSectionGiven(String invalidSection) {
    assertThrows(
      ParsingFailedException.class,
      () -> parser.parseDiscount(invalidSection)
    );
  }

}