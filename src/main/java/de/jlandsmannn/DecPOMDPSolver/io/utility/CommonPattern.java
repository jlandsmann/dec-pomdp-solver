package de.jlandsmannn.DecPOMDPSolver.io.utility;

public final class CommonPattern {
  public static final String POSITIVE_INTEGER_PATTERN = "[0-9][0-9]*";
  public static final String NEGATIVE_INTEGER_PATTERN = "-" + POSITIVE_INTEGER_PATTERN;
  public static final String INTEGER_PATTERN = "-?" + POSITIVE_INTEGER_PATTERN;

  public static final String POSITIVE_NUMBER_PATTERN = "[0-9]+(\\.[0-9]+)?";
  public static final String NEGATIVE_NUMBER_PATTERN = "-" + POSITIVE_NUMBER_PATTERN;
  public static final String NUMBER_PATTERN = "-?" + POSITIVE_NUMBER_PATTERN;

  public static final String NAME_PATTERN = "[a-zA-Z][a-zA-Z0-9]*";
}
