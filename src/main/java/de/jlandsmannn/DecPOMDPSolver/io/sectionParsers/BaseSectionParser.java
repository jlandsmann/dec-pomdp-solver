package de.jlandsmannn.DecPOMDPSolver.io.sectionParsers;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;
import de.jlandsmannn.DecPOMDPSolver.io.utility.DPOMDPSectionKeyword;
import de.jlandsmannn.DecPOMDPSolver.io.utility.SectionMatchResult;

import java.util.Optional;
import java.util.regex.Pattern;

public abstract class BaseSectionParser {
  protected final DPOMDPSectionKeyword keyword;
  protected final Pattern pattern;

  protected BaseSectionParser(DPOMDPSectionKeyword keyword, String pattern) {
    this(keyword, Pattern.compile(pattern));
  }

  protected BaseSectionParser(DPOMDPSectionKeyword keyword, Pattern pattern) {
    this.keyword = keyword;
    this.pattern = pattern;
  }

  protected Optional<SectionMatchResult> getMatch(String section) {
    var matcher = pattern.matcher(section);
    if (!matcher.find() || !matcher.matches()) return Optional.empty();
    return Optional.of(matcher.toMatchResult()).map(SectionMatchResult::new);
  }

  protected SectionMatchResult getMatchOrThrow(String section) {
    return getMatch(section).orElseThrow(() ->
      new ParsingFailedException("Trying to parse '" + keyword.getKeyword() + "' section, but found invalid format.")
    );
  }

  public abstract void parseSection(String section);
}
