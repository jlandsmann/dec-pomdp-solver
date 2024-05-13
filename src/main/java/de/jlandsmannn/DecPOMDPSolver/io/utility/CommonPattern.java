package de.jlandsmannn.DecPOMDPSolver.io.utility;

import java.util.Arrays;

public final class CommonPattern {
  public static final String INDEX_PATTERN = "[0-9]+";
  public static final String COUNT_PATTERN = "[1-9][0-9]*";
  public static final String NUMBER_PATTERN = "-?[0-9]+(\\.[0-9]+)?";
  public static final String PROBABILITY_PATTERN = "(?:1\\.0|0\\.[0-9]+)";
  public static final String IDENTIFIER_PATTERN = "[a-zA-Z][a-zA-Z0-9\\-\\_]*";

  public static String OR(String ...patterns) {
    var joined = Arrays.stream(patterns).reduce((a,b) -> a + "|" + b).orElse("");
    return GROUP(joined);
  }

  public static String ROWS_OF(String pattern) {
    return LIST_OF(pattern, "\n");
  }

  public static String LIST_OF(String pattern) {
    return LIST_OF(pattern, " ");
  }

  private static String LIST_OF(String pattern, String separator) {
    return  GROUP(GROUP(pattern + separator) + "*" + GROUP(pattern));
  }

  public static String NAMED_GROUP(String name, String pattern) {
    return "(?<" + name + ">" + pattern + ")";
  }

  public static String GROUP(String pattern) {
    return "(?:" + pattern + ")";
  }
}
