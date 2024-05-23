package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.jlandsmannn.DecPOMDPSolver.io.utility.CommonPattern.PROBABILITY_PATTERN;

public class DiscountSectionParser extends BaseSectionParser {
  private static final Logger LOG = LoggerFactory.getLogger(DiscountSectionParser.class);

  protected double discountFactor = 0D;

  public DiscountSectionParser() {
    super(
      DPOMDPSectionKeyword.DISCOUNT,
      DPOMDPSectionKeyword.DISCOUNT + ": ?" +
        "(?<discount>" + PROBABILITY_PATTERN + ")"
    );
  }

  public double getDiscountFactor() {
    return discountFactor;
  }

  public void parseSection(String section) {
    LOG.debug("Parsing 'discount' section.");
    var match = getMatchOrThrow(section);
    if (match.hasGroup("discount")) {
      var discount = match.getGroupAsDoubleOrThrow("discount");
      LOG.debug("Found discount factor: {}", discount);
      if (discount < 0) throw new ParsingFailedException("discount must be greater than or equal to zero.");
      else if (discount > 1) throw new ParsingFailedException("discount must be less than or equal to one.");
      discountFactor = discount;
    } else {
      throw new ParsingFailedException("'discount' section was parsed successfully, but discountFactor could not be found.");
    }
  }
}
