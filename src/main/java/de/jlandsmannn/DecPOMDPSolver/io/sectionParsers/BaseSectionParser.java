package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import de.jlandsmannn.DecPOMDPSolver.io.utility.SectionKeyword;
import de.jlandsmannn.DecPOMDPSolver.io.utility.SectionMatchResult;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is an abstract section parser,
 * which covers basic tasks like matching the section's regex.
 * It also provides a standard interface for parsing the section,
 * which is then extended by the concrete parsers,
 * to their respective needs.
 */
public abstract class BaseSectionParser {
  protected final SectionKeyword keyword;
  protected final Pattern pattern;

  protected BaseSectionParser(SectionKeyword keyword, String pattern) {
    this(keyword, Pattern.compile(pattern));
  }

  protected BaseSectionParser(SectionKeyword keyword, Pattern pattern) {
    this.keyword = keyword;
    this.pattern = pattern;
  }

  protected Optional<SectionMatchResult> getMatch(String section) {
    var matcher = pattern.matcher(section);
    if (!matcher.find() || !matcher.matches()) return Optional.empty();
    return Optional.of(matcher).map(SectionMatchResult::new);
  }

  protected SectionMatchResult getMatchOrThrow(String section) {
    return getMatch(section).orElseThrow(() ->
      new ParsingFailedException("Trying to parse '" + keyword.getKeyword() + "' section, but found invalid format.")
    );
  }

  public abstract void parseSection(String section);
}
