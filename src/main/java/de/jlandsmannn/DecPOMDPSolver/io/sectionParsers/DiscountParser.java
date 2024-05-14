package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.CommonParser;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscountParser {
  private static final Logger LOG = LoggerFactory.getLogger(DiscountParser.class);

  protected double discountFactor = 0D;

  public double getDiscountFactor() {
    return discountFactor;
  }

  public void parseDiscount(String section) {
    LOG.debug("Parsing 'discount' section.");
    var match = DPOMDPSectionPattern.DISCOUNT
      .getMatch(section)
      .orElseThrow(() -> new ParsingFailedException("Trying to parse discount section, but found invalid format."));
    if (match.group("discount") != null) {
      var discountString = match.group("discount");
      var discount = CommonParser.parseDoubleOrThrow(discountString);
      LOG.debug("Found discount factor: {}", discount);
      if (discount < 0) throw new ParsingFailedException("discount must be greater than or equal to zero.");
      else if (discount > 1) throw new ParsingFailedException("discount must be less than or equal to one.");
      discountFactor = discount;
    } else {
      throw new ParsingFailedException("'discount' section was parsed successfully, but discountFactor could not be found.");
    }
  }
}
