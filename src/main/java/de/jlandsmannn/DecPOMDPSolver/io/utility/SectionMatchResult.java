package de.jlandsmannn.DecPOMDPSolver.io.utility;

import de.jlandsmannn.DecPOMDPSolver.io.exceptions.ParsingFailedException;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

/**
 * This class wraps the {@link MatchResult} class from java,
 * to provide a more convenient interface for getting and extracting groups,
 * as various data types.
 */
public class SectionMatchResult {

  private final Matcher result;

  public SectionMatchResult(Matcher result) {
    this.result = result;
  }

  public boolean hasGroup(String name) {
    return result.group(name) != null;
  }

  public Optional<String> getGroupAsString(String name) {
    return Optional.ofNullable(result.group(name));
  }

  public Optional<String[]> getGroupAsStringArray(String name, String delimiter) {
    return Optional.ofNullable(result.group(name)).map(s -> s.split(delimiter));
  }

  public Optional<Double> getGroupAsDouble(String name) {
    return getGroupAsString(name).flatMap(CommonParser::parseDouble);
  }

  public Optional<Integer> getGroupAsInt(String name) {
    return getGroupAsString(name).flatMap(CommonParser::parseInteger);
  }

  public String getGroupAsStringOrThrow(String name) {
    return getGroupAsString(name).orElseThrow(() ->
      new ParsingFailedException("Tried to parse '" + name + "' group, but it does not exist.")
    );
  }

  public String[] getGroupAsStringArrayOrThrow(String name, String delimiter) {
    return getGroupAsStringArray(name, delimiter).orElseThrow(() ->
      new ParsingFailedException("Tried to parse '" + name + "' group, but it does not exist.")
    );
  }

  public double getGroupAsDoubleOrThrow(String name) {
    return getGroupAsDouble(name).orElseThrow(() ->
      new ParsingFailedException("Tried to parse '" + name + "' group, but it does not exist or is not a valid number.")
    );
  }

  public int getGroupAsIntOrThrow(String name) {
    return getGroupAsInt(name).orElseThrow(() ->
      new ParsingFailedException("Tried to parse '" + name + "' group, but it does not exist or is not a valid number.")
    );
  }
}
